# BBK Grammar — Layer 4

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Prerequisites:** [`layer1.md`](layer1.md), [`layer2.md`](layer2.md), [`layer3.md`](layer3.md)
**Test files:** [`05-procedures.bbk`](../../../tests/boxbreaker/examples/05-procedures.bbk), [`04-control-flow.bbk`](../../../tests/boxbreaker/examples/04-control-flow.bbk) (valid), [`bad-13-bad-statements.bbk`](../../../tests/boxbreaker/examples/bad/bad-13-bad-statements.bbk) (errors)

---

## Scope

Layer 4 is the **largest layer** so far. It delivers the two components that make the language "executable":

### Statements
| Construct | Form |
|---|---|
| `if/else if/else` | `if (cond) { ... } else if (cond) { ... } else { ... }` |
| `select/when/other` | `select { when (cond) { ... } when (cond) { ... } other { ... } }` |
| `while` | `while (cond) { ... }` |
| `do/while` | `do { ... } while (cond);` |
| `for` (with optional inline DCL-S) | `for (DCL-S i INT(10) = 0; i < 10; i += 1) { ... }` |
| `break`, `continue` | `break;` / `continue;` |
| `return` | `return;` or `return expr;` |
| `monitor`/`on-error`/`on-exit` | `monitor { ... } on-error (404, 405) { ... } on-exit { ... }` |
| Assignment | `lvalue = expr;` (also `+=`, `-=`, etc.) with optional `@halfup` |
| Expression statement | `f(args);` or any expression followed by `;` |

### Expressions
Full precedence hierarchy (14 levels, lowest to highest):

```
ternary           ?: (right-assoc)
logical_or        ||
logical_and       &&
bitwise_or        |
bitwise_xor       ^
bitwise_and       &
equality          ==  !=
relational        <  >  <=  >=
shift             <<  >>
additive          +  -
multiplicative    *  /  %
power             **  (right-assoc)
unary             +  -  !  ~  (prefix)
postfix           ()  []  .  ->
primary           literal | IDENT | (expr) | true | false | null | *IDENT
```

**Important pattern change:** Layer 4 unifies the `{ ... }` block. The `procedure_body` rule from Layer 3 disappears; `procedure_declaration` now uses `block_statement` directly (the same rule used by `if`, `while`, `for`, etc.). Every block allows mixing local declarations (`DCL-S`/`DCL-C`/`DCL-DS`) and statements.

**Still not covered:**
- File ops (`read`, `chain`, `write`, `setll`, `exfmt`, etc.) — Layer 6
- Subroutines (`BEGSR`/`ENDSR`/`EXSR`) — Layer 6
- `PRE-*` directives — Layer 6

---

## Key BNF productions

### Unified block

```bnf
block_statement ::= LBRACE block_item* RBRACE {pin=1}

block_item ::= variable_declaration
             | constant_declaration
             | data_structure_declaration
             | statement
             | unknown_block_item

private unknown_block_item ::= !RBRACE !<<eof>> any_token
```

The `unknown_block_item` with `!RBRACE !<<eof>>` is the trick that allows **constructs not yet supported** (file ops, EXSR, etc.) to **not produce errors** inside blocks. The parser consumes them token by token without reporting errors until Layer 6 parses them properly.

### Main statement rule

```bnf
statement ::= if_statement
            | select_statement
            | while_statement
            | do_while_statement
            | for_statement
            | break_statement
            | continue_statement
            | return_statement
            | monitor_statement
            | expression_statement
```

Each statement has `{pin=1}` after its distinguishing keyword (`KW_IF`, `KW_WHILE`, etc.). That is what allows specific errors to be reported:
- `if x > 0 { }` → "LPAREN expected, got IDENT"
- `while () { }` → "expression expected, got RPAREN"
- `for ; ; ;` → "LPAREN expected, got SEMI"

### Assignment + expression statement (special case)

```bnf
expression_statement ::= expression assignment_tail? SEMI

private assignment_tail ::= assignment_op expression attribute_modifier? {pin=1}
```

This unifies two things that other languages keep separate:
- `f(args);` → expression followed by `;`
- `total = a + b @halfup;` → expression (lvalue) + `=` + expression + modifier + `;`

**Subtle but important:** `expression_statement` **has no pin**. The `assignment_tail` **does have an internal pin**.

Why? Trade-off between two goals:
- **Detect `total = ;`** (assignment without value): when `=` is consumed and the right expression fails, the `assignment_tail` pin engages and the error is reported. ✓
- **Don't flag false errors on file ops like `read customers;`**: the parser tries expression_statement, matches `read` as an expression, doesn't find `=` (the optional doesn't fire), expects `;`, gets IDENT, fails with no pin, backtracks cleanly, and the `unknown_block_item` fallback silently consumes everything. ✓

When Layer 6 adds file ops as their own statements, they'll match before expression_statement and this trade-off becomes unnecessary.

### Expression hierarchy

```bnf
expression ::= ternary_expression

ternary_expression ::= logical_or_expression (QUESTION expression COLON ternary_expression)?

logical_or_expression  ::= logical_and_expression (PIPE_PIPE logical_and_expression)*
logical_and_expression ::= bitwise_or_expression (AMP_AMP bitwise_or_expression)*
// ... (8 more in the middle)
power_expression       ::= unary_expression (STAR_STAR power_expression)?  // right-assoc
unary_expression       ::= (PLUS | MINUS | BANG | TILDE) unary_expression
                         | postfix_expression
postfix_expression     ::= primary postfix_suffix*

primary ::= literal | KW_TRUE | KW_FALSE | KW_NULL | STAR_IDENT | IDENT | LPAREN expression RPAREN

postfix_suffix ::= LPAREN argument_list? RPAREN
                 | LBRACKET subscript_list RBRACKET
                 | DOT IDENT
                 | ARROW IDENT
```

**Notes:**
- **Right-associative**: ternary (`a ? b : c ? d : e` = `a ? b : (c ? d : e)`) and power (`2 ** 3 ** 4` = `2 ** (3 ** 4)`)
- **Left-associative**: everything else (follows the standard `lhs (OP rhs)*` pattern)
- **Subscript with commas**: `arr[i, j]` for multi-dim (not `arr[i][j]`), following decision #8 in `grammar.md`
- **Calls as lvalue**: syntactically `f().field = x` is valid. The semantic type checker must verify that `f()` returns a reference (future)

### `for` with inline declaration

```bnf
for_statement ::= KW_FOR LPAREN for_init? SEMI expression? SEMI for_update? RPAREN block_statement {pin=1}

for_init ::= for_inline_decl | for_assignment | expression
for_update ::= for_assignment | expression
for_inline_decl ::= KW_DCL_S IDENT type_specification EQ expression {pin=1}
for_assignment ::= lvalue assignment_op expression {pin=2}
```

Valid examples:
```bbk
for (i = 0; i < 10; i += 1) { ... }                        // assignment-based
for (DCL-S j INT(10) = 0; j < 100; j += 1) { ... }         // inline declaration
```

### `select/when/other`

```bnf
select_statement ::= KW_SELECT LBRACE when_clause+ other_clause? RBRACE {pin=1}
when_clause      ::= KW_WHEN LPAREN expression RPAREN block_statement {pin=1}
other_clause     ::= KW_OTHER block_statement {pin=1}
```

`when_clause+` (one or more) — an empty `select { }` fails. This differs from C `switch`, which does accept an empty switch.

### `monitor/on-error/on-exit`

```bnf
monitor_statement ::= KW_MONITOR block_statement on_error_clause* on_exit_clause? {pin=1}
on_error_clause   ::= KW_ON_ERROR status_list? block_statement {pin=1}
on_exit_clause    ::= KW_ON_EXIT block_statement {pin=1}
status_list       ::= LPAREN expression (COMMA expression)* RPAREN {pin=1}
```

Zero or more `on-error` clauses (each with an optional list of status codes), and zero or one final `on-exit`.

---

## Full examples

### Valid — `04-control-flow.bbk`

```bbk
DCL-PROC controlFlowDemo {
  DCL-S counter INT(10) INZ(0);
  DCL-S status  CHAR(1);
  DCL-S sum     INT(10) INZ(0);

  if (counter == 0) {
    print("Zero");
  } else if (counter < 10) {
    print("Small");
  } else {
    print("Large");
  }

  status = "A";
  select {
    when (status == "A") {
      print("Active");
    }
    when (status == "I" || status == "P") {
      print("Inactive or Pending");
    }
    other {
      print("Unknown status");
    }
  }

  while (counter < 5) {
    counter += 1;
  }

  do {
    counter -= 1;
  } while (counter > 0);

  for (DCL-S i INT(10) = 0; i < 10; i += 1) {
    sum += i;
  }

  for (DCL-S j INT(10) = 0; j < 100; j += 1) {
    if (j == 50) {
      break;
    }
    if (j % 2 == 0) {
      continue;
    }
    sum += j;
  }
}
```

Layer 4 recognizes and structures all of this: every block, every condition, every `print()` call, every assignment with compound operators.

### Errors detected — `bad-13-bad-statements.bbk`

| Category | Cases |
|---|---|
| **if/else** | `if x > 0 { }` (missing parens), `if (x > 0) doSomething();` (missing braces), `if () { }` (empty condition), `else` without body |
| **while** | Missing parens, empty condition, no body |
| **do/while** | Missing parens on while, missing braces, missing final `;` |
| **for** | No parens, missing `;` separators, inline decl without initial value |
| **select** | `select { }` (requires `when+`), `when` outside `select` |
| **return/break/continue** | Missing final `;` |
| **assignments** | `x = ;` (no rhs), `= 5;` (no lvalue), `x +=;` (compound op with no rhs) |
| **expressions** | `5 +;` (incomplete additive), `(5 + 7;` (unclosed parens), `foo(;` (call args), `arr[;` (subscript), `a ? b;` (ternary missing `:` and else) |
| **monitor** | No body, on-error without body, trailing comma in status list |

---

## Implementation notes

### The unified block trick

Before L4, `procedure_body`, `ds_body`, and any other `{ ... }` were defined as separate rules. Layer 4 introduces `block_statement`, which they all share:

- `procedure_declaration ::= ... block_statement {pin=1}`
- `if_statement ::= KW_IF LPAREN expression RPAREN block_statement ...`
- `while_statement ::= KW_WHILE LPAREN expression RPAREN block_statement {pin=1}`
- `monitor_statement ::= KW_MONITOR block_statement ...`
- etc.

Benefit: any improvement to `block_statement` (e.g. allowing new types of declaration inside) propagates automatically to every context. `ds_body` stays separate because its items are subfields, not statements.

### Why `expression_statement` has no pin but `assignment_tail` does

Discussed above in the productions section. Summary:
- Pin on `expression_statement` → file ops like `read customers;` produce false errors ("SEMI expected, got IDENT")
- No pin → malformed assignments like `total = ;` are not detected
- Solution: pin only on `assignment_tail` (the `= expression` part)

When Layer 6 adds file ops as their own rules, we could add the pin to `expression_statement` without issue.

### Braces mandatory

Closed decision (from `grammar.md` #4): **every block requires `{ }`**, even for a single statement. `if (cond) doStuff();` is not accepted — it must be `if (cond) { doStuff(); }`.

This avoids the classic C bug:
```c
if (cond)
    statement1();
    statement2();  // always executes, NOT part of the if
```

### Permissive `lvalue`

```bnf
lvalue ::= postfix_expression
```

Syntactically, any postfix_expression can appear on the left of `=`. That includes:
- `x = ...` (simple IDENT)
- `ds.field = ...` (member access)
- `arr[i] = ...` (subscript)
- `arr[i, j] = ...` (multi-dim)
- `ptr->field = ...` (pointer deref)
- `f(x).field = ...` (call result, decision #9)

Validation of "is this actually assignable?" would be done by the type checker in a future semantic phase.

### Potential conflicts with file ops

With Layer 4 in place but **without Layer 6 yet**, expressions like:

```bbk
read customers customerRec;
chain key file rec;
exfmt screenFormat;
```

fall into `unknown_block_item` token by token. They produce no errors and no structure-view items. Layer 6 will replace them with `file_op_statement` with full error reporting.

In the meantime, files like `10-files.bbk` that use file ops inside monitor blocks **parse without errors** (anything that is not a recognized declaration or statement falls into the silent fallback). That is desirable to keep the experience clean across layers.

---

## Next layer

`layer5.md` — refinements to expressions if any, and possibly extended attribute modifiers.

`layer6.md` — file operations (`read`, `chain`, `write`, etc.), subroutines (`BEGSR`/`EXSR`), directives (`PRE-*`).
