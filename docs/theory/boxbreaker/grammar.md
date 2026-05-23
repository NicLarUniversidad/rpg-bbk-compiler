# BoxBreaker (BBK) Syntactic Grammar — tentative proposal

**Status:** draft. Depends on the decisions in [`tokens.md`](tokens.md) — if any change, the grammar adjusts.

**Notation conventions:** the same as in [`../c99-grammar.md`](../c99-grammar.md):

- `<non-terminal>` — syntactic category.
- `literal` — exact terminal (token).
- `a | b` — alternative.
- `a?` — optional.
- `{ a }` — repetition (0 or more).
- `one of:` — simple alternative between terminals.

**Referenced tokens:** defined in [`tokens.md`](tokens.md). In this grammar they appear in their lexical form (e.g., `DCL-S`, `;`, `IDENT`).

---

## 1. Translation unit (top level)

A BBK file is a sequence of top-level declarations, optionally preceded by a module directive.

```
<translation-unit>
    : { <directive> }*
      <ctl-opt-statement>?
      { <directive> | <file-declaration> }*
      { <directive> | <module-data-declaration> }*
      { <directive> | <prototype-declaration> }*
      { <directive> | <procedure-declaration> }*

<module-data-declaration>
    : <constant-declaration>
    | <variable-declaration>
    | <data-structure-declaration>
```

**Notes:**
- **Forced order** of top-level declarations (closed decision): first `CTL-OPT`, then `DCL-F`, then `DCL-C`/`DCL-S`/`DCL-DS` (free among themselves), then `DCL-PR`, then `DCL-PROC`. Equivalent to the RPG convention (H → F → D → P).
- Directives (`PRE-IF`, `PRE-INCLUDE`, etc.) can appear at any point without breaking the order.

---

## 2. Module directive: CTL-OPT

```
<ctl-opt-statement>
    : CTL-OPT { <ctl-opt-keyword> }* ;

<ctl-opt-keyword>
    : IDENT                                     // e.g. NOMAIN, DEBUG
    | IDENT ( <ctl-opt-arg-list> )              // e.g. DFTACTGRP(*NO)

<ctl-opt-arg-list>
    : <ctl-opt-arg>
    | <ctl-opt-arg-list> : <ctl-opt-arg>

<ctl-opt-arg>
    : IDENT
    | <literal>
    | * IDENT                                   // figurative argument, e.g. *NO, *NEW
```

Example:

```bbk
CTL-OPT NOMAIN DFTACTGRP(*NO) ACTGRP("BBKTEST") DEBUG;
```

---

## 3. Variable, constant and type declarations

### 3.1 Standalone variable (DCL-S)

```
<variable-declaration>
    : DCL-S IDENT <type-specification> { <var-modifier> }* ;

<type-specification>
    : <primitive-type>
    | <like-reference>

<primitive-type>
    : <type-keyword>
    | <type-keyword> ( <type-arg-list> )

<type-keyword> one of:
    CHAR  VARCHAR  PACKED  ZONED  BINDEC
    INT   UNS      FLOAT
    DATE  TIME     TIMESTAMP
    BOOL  POINTER  VOID

<type-arg-list>
    : <integer-literal>
    | <integer-literal> : <integer-literal>

<like-reference>
    : LIKE ( IDENT )
    | LIKEDS ( IDENT )
    | LIKEREC ( IDENT ( : <part>)? )

<part> one of:
    *ALL  *INPUT  *OUTPUT  *KEY

<var-modifier>
    : INZ ( <expression> )
    | INZ ( *LIKEDS )
    | BASED ( IDENT )
    | DIM ( <integer-literal> )
    | OVERLAY ( IDENT ( : <integer-literal> )? )
    | POS ( <integer-literal> )
    | STATIC
    | EXPORT
    | IMPORT
    | TEMPLATE
    | ALIGN
    | OPTIONS ( <options-list> )
    | CCSID ( <expression> )

<options-list>
    : <option>
    | <options-list> : <option>

<option> one of:
    *NOPASS  *OMIT  *VARSIZE  *STRING  *NULLIND
```

Examples:

```bbk
DCL-S counter INT(10);
DCL-S name CHAR(50) INZ("");
DCL-S price PACKED(9:2) INZ(0d);
DCL-S birthDate DATE;
DCL-S active BOOL INZ(false);
DCL-S nums INT(10) DIM(100);
DCL-S overlayField CHAR(20) OVERLAY(parentDS:1);
DCL-S sameAsPrice LIKE(price);
```

### 3.2 Constants (DCL-C)

```
<constant-declaration>
    : DCL-C IDENT <constant-value> ;
    | DCL-C IDENT CONST ( <constant-value> ) ;

<constant-value>
    : <literal>
    | <figurative-constant>
    | ( <constant-expression> )

<figurative-constant> one of:
    true  false  null
```

(In BBK we drop RPG's legacy figurative constants; `true`/`false`/`null` are the only ones.)

Examples:

```bbk
DCL-C MAX_RETRIES 5;
DCL-C PI 3.14159;
DCL-C GREETING "Hello";
DCL-C IS_ENABLED CONST(true);
```

### 3.3 Data structures (DCL-DS)

```
<data-structure-declaration>
    : DCL-DS IDENT { <ds-modifier> }* { <ds-subfield> { <ds-subfield> }* }
    | DCL-DS IDENT { <ds-modifier> }* ;                  // forward / template

<ds-modifier>
    : QUALIFIED
    | TEMPLATE
    | EXTNAME ( <string-literal> )
    | LIKEDS ( IDENT )
    | LIKEREC ( IDENT )
    | INZ
    | BASED ( IDENT )
    | DIM ( <integer-literal> )
    | ALIGN
    | PSDS
    | INFDS ( IDENT )

<ds-subfield>
    : DCL-SUBF? IDENT <type-specification> { <var-modifier> }* ;
```

(`DCL-SUBF` is optional; subfields can be declared simply as `<name> <type>;`.)

Examples:

```bbk
DCL-DS employee QUALIFIED {
  id        INT(10);
  name      CHAR(50);
  hireDate  DATE;
  salary    PACKED(9:2);
}

// Template (definition with no storage; usable with LIKEDS):
DCL-DS addressTemplate TEMPLATE {
  street CHAR(100);
  city   CHAR(50);
  zip    CHAR(10);
}

DCL-DS shippingAddr LIKEDS(addressTemplate);
DCL-DS billingAddr  LIKEDS(addressTemplate);
```

### 3.4 Prototypes (DCL-PR)

Forward declaration of a procedure.

```
<prototype-declaration>
    : DCL-PR IDENT <inline-param-list>? <return-type>? { <pr-modifier> }* ;

<return-type>
    : -> <type-specification>

<inline-param-list>
    : ( )
    | ( <inline-param> { , <inline-param> }* )

<inline-param>
    : IDENT <type-specification> { <param-modifier> }*

<pr-modifier>
    : EXTPGM ( <string-literal> )
    | EXTPROC ( <string-literal> )
    | OPDESC
    | RTNPARM

<param-modifier>
    : VALUE
    | CONST
    | OPDESC
    | OPTIONS ( <options-list> )
```

**Notes:**
- **Inline form only** (closed decision). The form with an explicit internal `DCL-PI` is no longer supported.
- If there is a return value: `-> <type>` after the parameters.
- No return value: omit `-> <type>`.

Examples:

```bbk
// Inline:
DCL-PR sum(a INT(10), b INT(10)) -> INT(10);

// No return:
DCL-PR greet(name CHAR(50));

// Call to an external program:
DCL-PR CUSTPROG EXTPGM("CUSTPROG") {
  custId INT(10) VALUE;
  status INT(10);
}
```

### 3.5 Procedures (DCL-PROC)

**Inline form only** (closed decision). `DCL-PI` as a separate declaration was removed from the language; parameters go directly in the `DCL-PROC` signature.

```
<procedure-declaration>
    : DCL-PROC IDENT <inline-param-list>? <return-type>? { <proc-modifier> }* { <procedure-body> }

<procedure-body>
    : { <statement-or-declaration> }*

<statement-or-declaration>
    : <statement>
    | <variable-declaration>
    | <constant-declaration>
    | <data-structure-declaration>

<proc-modifier>
    : EXPORT
    | EXTPROC ( <string-literal> )
```

Examples:

```bbk
DCL-PROC sum(a INT(10), b INT(10)) -> INT(10) EXPORT {
  return a + b;
}

// No return, no parameters:
DCL-PROC greet {
  // body
}

// With local variables:
DCL-PROC processOrder(orderId INT(10)) -> BOOL {
  DCL-S total PACKED(11:2);
  DCL-S valid BOOL;
  
  // ...
  return valid;
}
```

### 3.6 File declarations (DCL-F)

```
<file-declaration>
    : DCL-F IDENT { <f-keyword> }* ;

<f-keyword>
    : USAGE ( <usage-list> )
    | KEYED
    | RECNO ( IDENT )
    | PREFIX ( IDENT ( : <integer-literal> )? )
    | RENAME ( IDENT : IDENT )
    | EXTNAME ( <string-literal> )
    | EXTFILE ( <string-literal> )
    | EXTMBR ( <string-literal> )
    | EXTDESC ( <string-literal> )
    | USROPN
    | DISK | PRINTER | WORKSTN | SEQ
    | BLOCK ( * IDENT )
    | INDDS ( IDENT )
    | INFDS ( IDENT )

<usage-list>
    : <usage-arg>
    | <usage-list> : <usage-arg>

<usage-arg> one of:
    *INPUT  *OUTPUT  *UPDATE  *DELETE
```

Examples:

```bbk
DCL-F customers DISK USAGE(*INPUT) KEYED EXTNAME("CUSTOMER");
DCL-F orders   DISK USAGE(*INPUT:*OUTPUT:*UPDATE) KEYED EXTNAME("ORDER");
DCL-F report   PRINTER USAGE(*OUTPUT) USROPN;
```

---

## 4. Statements

```
<statement>
    : <expression-statement>
    | <assignment-statement>
    | <if-statement>
    | <select-statement>
    | <while-statement>
    | <do-while-statement>
    | <for-statement>
    | <break-statement>
    | <continue-statement>
    | <return-statement>
    | <block-statement>
    | <call-statement>
    | <file-operation-statement>
    | <monitor-statement>
```

### 4.1 Expression statement

```
<expression-statement>
    : <expression> ;
    | ;                                          // null statement
```

Examples:

```bbk
process(orderId);              // call as statement
increment();
;                              // empty statement
```

### 4.2 Assignment statement

```
<assignment-statement>
    : <lvalue> <assignment-operator> <expression> <attribute-modifier>? ;

<lvalue>
    : IDENT
    | <lvalue> . IDENT                          // member access on qualified DS
    | <lvalue> [ <expression> ]                 // subscript
    | <lvalue> -> IDENT                         // pointer member access

<assignment-operator> one of:
    =  +=  -=  *=  /=  %=  &=  |=  ^=  <<=  >>=

<attribute-modifier>
    : @ IDENT                                   // e.g. @halfup, @halfdown, @trunc
```

Examples:

```bbk
counter = 0;
counter += 1;
total = price * vat @halfup;
employee.id = 100;
nums[0] = 42;
employees[i].salary = baseSalary;
```

### 4.3 If statement

```
<if-statement>
    : if ( <expression> ) <block-or-statement>
        { else if ( <expression> ) <block-or-statement> }*
        ( else <block-or-statement> )?

<block-or-statement>
    : <block-statement>
    | <statement>
```

Examples:

```bbk
if (cond) {
  ...
}

if (cond) {
  ...
} else if (otherCond) {
  ...
} else {
  ...
}

if (cond) doSomething();          // braceless, single statement
```

(Decision to confirm: do we allow braceless statements as in C, or force braces? Forcing braces eliminates the classic braceless-`if` bug. I recommend allowing both for ergonomics.)

### 4.4 Select / when / other statement

```
<select-statement>
    : select { <when-clause> { <when-clause> }* <other-clause>? }

<when-clause>
    : when ( <expression> ) <block-or-statement>

<other-clause>
    : other <block-or-statement>
```

Examples:

```bbk
select {
  when (type == "A") {
    processA();
  }
  when (type == "B" || type == "C") {
    processBC();
  }
  other {
    processDefault();
  }
}
```

(`select` with no discriminating condition — each `when` does the check. Unlike C's `switch`, `when` expressions don't need to be constants.)

### 4.5 While statement

```
<while-statement>
    : while ( <expression> ) <block-or-statement>
```

Example:

```bbk
while (i < 10) {
  process(i);
  i += 1;
}
```

### 4.6 Do-while statement

```
<do-while-statement>
    : do <block-or-statement> while ( <expression> ) ;
```

Example:

```bbk
do {
  result = compute();
} while (result == 0);
```

### 4.7 For statement

```
<for-statement>
    : for ( <for-init>? ; <expression>? ; <for-update>? ) <block-or-statement>

<for-init>
    : <expression>
    | <variable-declaration-inline>

<variable-declaration-inline>
    : DCL-S IDENT <type-specification> = <expression>

<for-update>
    : <expression>
```

(C99 allows declaration in the `for` init. BBK accepts the equivalent syntax with an inline `DCL-S`.)

Examples:

```bbk
for (i = 0; i < 10; i += 1) {
  process(i);
}

for (DCL-S j INT(10) = 0; j < 100; j += 1) {
  // j is local to the for
}
```

### 4.8 Break / continue / return

```
<break-statement>     : break ;
<continue-statement>  : continue ;
<return-statement>    : return <expression>? ;
```

### 4.9 Block statement

```
<block-statement>
    : { <statement-or-declaration>* }
```

Allows free mixing of declarations and statements (C99 style).

### 4.10 Call statement

Procedure call as a statement (without assigning the result).

```
<call-statement>
    : <call-expression> ;

<call-expression>
    : <primary> ( <argument-list>? )

<argument-list>
    : <expression>
    | <argument-list> , <expression>
```

Examples:

```bbk
greet();
greet("world");
processOrder(orderId, status);
```

### 4.11 File operation statement

File operations. Each opcode is an optional keyword in front; the general syntax is function-like.

```
<file-operation-statement>
    : <file-op-keyword> <file-op-args> ;
    | <file-op-function-call> ;                    // alternative function-like syntax

<file-op-keyword> one of:
    read  reade  readp  readpe  chain
    write  update  delete  unlock
    open  close  setll  setgt  exfmt

<file-op-args>
    : IDENT                                        // file
    | <expression> IDENT                           // key + file
    | IDENT IDENT                                  // file + result DS
    | <expression> IDENT IDENT                     // key + file + result DS
```

Examples:

```bbk
read customers;
read customers customerDS;
chain custId customers customerDS;
write orders orderDS;
update orders orderDS;
setll *START customers;
```

(Decision to confirm: `read customers` with bare identifiers and no parentheses, or `read(customers)` function-like? The no-parens form is more readable for simple statements; the function-like form is more uniform with the rest of the language. I'd recommend function-like for consistency.)

### 4.12 Monitor statement (error handling)

```
<monitor-statement>
    : monitor { { <statement> }* } { <on-error-clause> }*  <on-exit-clause>?

<on-error-clause>
    : on-error <status-list>? <block-statement>

<status-list>
    : ( <expression> { , <expression> }* )

<on-exit-clause>
    : on-exit <block-statement>
```

Examples:

```bbk
monitor {
  open customers;
  read customers customerDS;
} on-error (00404, 00405) {
  log("file not found");
} on-error {
  log("unknown error");
} on-exit {
  close customers;
}
```

---

## 5. Expressions

Precedence from lowest to highest (top-down):

```
<expression>
    : <ternary-expression>

<ternary-expression>
    : <logical-or-expression>
    | <logical-or-expression> ? <expression> : <ternary-expression>

<logical-or-expression>
    : <logical-and-expression>
    | <logical-or-expression> || <logical-and-expression>

<logical-and-expression>
    : <bitwise-or-expression>
    | <logical-and-expression> && <bitwise-or-expression>

<bitwise-or-expression>
    : <bitwise-xor-expression>
    | <bitwise-or-expression> | <bitwise-xor-expression>

<bitwise-xor-expression>
    : <bitwise-and-expression>
    | <bitwise-xor-expression> ^ <bitwise-and-expression>

<bitwise-and-expression>
    : <equality-expression>
    | <bitwise-and-expression> & <equality-expression>

<equality-expression>
    : <relational-expression>
    | <equality-expression> == <relational-expression>
    | <equality-expression> != <relational-expression>

<relational-expression>
    : <shift-expression>
    | <relational-expression> < <shift-expression>
    | <relational-expression> > <shift-expression>
    | <relational-expression> <= <shift-expression>
    | <relational-expression> >= <shift-expression>

<shift-expression>
    : <additive-expression>
    | <shift-expression> << <additive-expression>
    | <shift-expression> >> <additive-expression>

<additive-expression>
    : <multiplicative-expression>
    | <additive-expression> + <multiplicative-expression>
    | <additive-expression> - <multiplicative-expression>

<multiplicative-expression>
    : <power-expression>
    | <multiplicative-expression> * <power-expression>
    | <multiplicative-expression> / <power-expression>
    | <multiplicative-expression> % <power-expression>

<power-expression>
    : <unary-expression>
    | <unary-expression> ** <power-expression>        // right-associative

<unary-expression>
    : <postfix-expression>
    | + <unary-expression>
    | - <unary-expression>
    | ! <unary-expression>
    | ~ <unary-expression>

<postfix-expression>
    : <primary>
    | <postfix-expression> ( <argument-list>? )      // function call
    | <postfix-expression> [ <expression> ]          // subscript
    | <postfix-expression> . IDENT                   // member access
    | <postfix-expression> -> IDENT                  // pointer member access

<primary>
    : IDENT
    | <literal>
    | true | false | null
    | ( <expression> )
```

### 5.1 Precedence table (high → low)

| Level | Operators | Associativity |
|---|---|---|
| 1 | `(...)` `[...]` `.` `->` (postfix) | left |
| 2 | unary `+` `-` `!` `~` | right |
| 3 | `**` | right |
| 4 | `*` `/` `%` | left |
| 5 | `+` `-` | left |
| 6 | `<<` `>>` | left |
| 7 | `<` `>` `<=` `>=` | left |
| 8 | `==` `!=` | left |
| 9 | `&` | left |
| 10 | `^` | left |
| 11 | `\|` | left |
| 12 | `&&` | left |
| 13 | `\|\|` | left |
| 14 | `?:` ternary | right |

### 5.2 Literals

```
<literal>
    : INT_LIT
    | INT_LIT_HEX
    | INT_LIT_OCT
    | FLOAT_LIT
    | DEC_LIT
    | STR_LIT
```

(Detailed lexical definition in [`tokens.md`](tokens.md) §3.)

### 5.3 Date/time/timestamp constructors

These are built as function calls — they are not their own literals:

```bbk
DCL-S d DATE;
DCL-S t TIME;
DCL-S ts TIMESTAMP;

d = date("2026-05-22");
t = time("14:30:00");
ts = timestamp("2026-05-22T14:30:00.000000");
```

---

## 6. Directives

Processed by a preprocessing phase before main parsing. Syntax similar to the C preprocessor, but using the RPG `/KEYWORD` style.

```
<directive>
    : <conditional-directive>
    | <define-directive>
    | <include-directive>
    | <eof-directive>
```

### 6.1 Conditional compilation

```
<conditional-directive>
    : <if-directive> { <elseif-directive> }* <else-directive>? <endif-directive>

<if-directive>
    : PRE-IF <condition-expression>

<elseif-directive>
    : PRE-ELSEIF <condition-expression>

<else-directive>
    : PRE-ELSE

<endif-directive>
    : PRE-ENDIF

<condition-expression>
    : DEFINED ( IDENT )
    | NOT DEFINED ( IDENT )
    | <constant-expression>
```

Example:

```bbk
PRE-IF DEFINED(DEBUG)
  log("debug mode active");
PRE-ELSEIF DEFINED(PRODUCTION)
  log("production mode");
PRE-ELSE
  log("default mode");
PRE-ENDIF
```

### 6.2 Define / undefine

```
<define-directive>
    : PRE-DEFINE IDENT
    | PRE-DEFINE IDENT <replacement-text>

<undefine-directive>
    : PRE-UNDEFINE IDENT
```

BBK accepts both modes: simple flag (`PRE-DEFINE DEBUG`) or with replacement text (`PRE-DEFINE MAX_RETRIES 5`). The processor replaces occurrences of the symbol with the text throughout the rest of the file.

### 6.3 Include

```
<include-directive>
    : PRE-INCLUDE <string-literal>
    | PRE-INCLUDE IDENT
```

Unified — there's no distinction between `/COPY` and `/INCLUDE`. One directive with the semantics "include a source file at this point".

Example:

```bbk
PRE-INCLUDE "common-types.bbki"
PRE-INCLUDE "db-prototypes.bbki"
```

### 6.4 EOF

```
<eof-directive>
    : PRE-EOF
```

Marks a premature end of source; what follows in the file is ignored.

---

## 7. Attribute modifiers

Tokens introduced with `@`. Closed list in V1; may be extended in future versions.

```
<attribute-modifier>
    : @ <attribute-name>

<attribute-name> one of:
    halfup    halfdown    trunc
```

**Contexts where it applies:**

- Assignment statement: modifies the rounding of the assigned value.
- Possible extensions: procedure body (`@inline`?), variable type (`@volatile`?). For now, assignment only.

Example:

```bbk
DCL-S total    PACKED(11:2);
DCL-S price    PACKED(9:2);
DCL-S quantity INT(10);

total = price / quantity @halfup;     // half-up rounding
total = price / quantity @trunc;      // truncate (default)
```

---

## 8. Semantic rules — synthesis

(As with [`../c99-grammar.md`](../c99-grammar.md) §4, semantic rules cannot be put in BNF. They will be documented in `semantics.md` when written.)

For now, the critical points still pending formal definition:

- **Type system.** Promotion rules between numeric types (INT ↔ PACKED ↔ FLOAT). Assignment compatibility CHAR ↔ VARCHAR. Implicit vs explicit conversions.
- **Scope rules.** Module-level vs procedure-local variables. Visibility with EXPORT/IMPORT.
- **Storage durations.** STATIC, automatic, based (BASED).
- **Initialization defaults.** Does BBK auto-initialize, or does it require explicit `INZ`? (Decision already made in [`../mapping/translatable.md`](../../mapping/translatable.md): always explicit.)
- **Sequence points.** When side effects are guaranteed.
- **Decimal arithmetic.** Precision and rounding rules in operations between PACKED/ZONED of different scales.
- **Pointers and aliasing.** What's allowed with BASED, OVERLAY, casts.
- **Error handling.** monitor/on-error/on-exit semantics. Equivalent to try/catch/finally.
- **File ops.** Cursor state after read/chain/setll. write/update behavior after error.

---

## 9. Open decisions (grammar specifics)

Items where the proposed grammar is a choice but alternatives exist:

| # | Decision | Resolution |
|---|---|---|
| 1 | Top-level declaration ordering | **Forced order** CTL-OPT → DCL-F → (DCL-C/S/DS) → DCL-PR → DCL-PROC. Directives may go anywhere. |
| 2 | Procedure inline vs explicit | **Inline only.** Separate `DCL-PI` is removed from the language. |
| 3 | DS subfield with DCL-SUBF | **Optional.** Both forms (with and without `DCL-SUBF`) are valid. |
| 4 | Mandatory braces in blocks | **Yes, braces required** in `if`, `else`, `while`, `do/while`, `for`, `select`/`when`/`other`, `monitor`/`on-error`/`on-exit`. No braceless single-statements. |
| 5 | File operations syntax | **Keyword style** (`read customers customerDS;`), not function-like. |
| 6 | `PRE-DEFINE` with replacement text | **C style** — accepts `PRE-DEFINE NAME` (flag) and `PRE-DEFINE NAME value` (with replacement text). |
| 7 | `/INCLUDE` and `/COPY` | **Unified** in `PRE-INCLUDE`. |
| 8 | Multi-dim arrays | **`arr[i, j]`** (comma-separated indices in a single `[]`), not chained `[i][j]`. |
| 9 | Lvalue can contain call result | **Yes.** `f().field = x` and similar are syntactically valid. The semantics (function must return a reference/lvalue) is validated in the type checker. |
| 10 | `LIKE(expression)` | **IDENT only** — `LIKE(otherVar)` valid; `LIKE(f())` or `LIKE(arr[i])` not. |
| 11 | Inline declaration in `for` | **Supported** — `for (DCL-S i INT(10) = 0; i < 10; i += 1) { ... }`. |
| 12 | Attribute modifier syntax | **`@`** — `@halfup`, `@halfdown`, `@trunc`. Closed list. |

**The 12 grammar decisions are closed.** Still to do:
- Reflect in the BNF productions the changes from #4 (force braces), #8 (subscript with comma), #9 (lvalue with calls) — these are pending edits in this file.
- Define formal semantic rules in `semantics.md` (type system, conversions, sequence points, etc.).

---

## 10. Related documents

- [`tokens.md`](tokens.md) — lexicon (individual tokens)
- [`../c99-grammar.md`](../c99-grammar.md) — C99 grammar (lowering target)
- [`../rpgle-grammar.md`](../rpgle-grammar.md) — RPG grammar (frontend source)
- [`../../mapping/similarities.md`](../../mapping/similarities.md) — what maps directly RPG ↔ C
- [`../../mapping/translatable.md`](../../mapping/translatable.md) — what needs translation with runtime
- [`../../mapping/runtime-required.md`](../../mapping/runtime-required.md) — what can't be solved by translation alone
