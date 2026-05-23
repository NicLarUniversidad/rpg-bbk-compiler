# BoxBreaker (BBK) Tokens — tentative proposal

**Status:** draft. The decisions here are a starting point; several are explicitly left open at the end of the document.

**Design principle behind the choices:** semantic vocabulary from RPG (when it's richer than C), pragmatic syntax from C (when it's cleaner), none of the historical baggage from either. See [`../../mapping/similarities.md`](../../mapping/similarities.md) and [`../../mapping/translatable.md`](../../mapping/translatable.md) for item-by-item reasoning.

**Global rules that affect lexing:**

- BBK is **always free-form**. No column-based mode.
- Statements end in `;`.
- Blocks are delimited with `{` and `}` (C style). This eliminates the `END-IF`, `ENDIF`, `ENDFOR`, `ENDDO`, `ENDSL`, `END-DS`, `END-PR`, `END-PI`, `END-PROC` keywords from the lexicon — `}` replaces them.
- **Case sensitivity:** **case-insensitive** everywhere (keywords and identifiers), same as RPG. `dcl-s`, `DCL-S`, `Dcl-S` are the same token; `myVar`, `MYVAR`, `MyVar` reference the same variable. The frontend normalizes to a canonical form (TBD: lowercase) before emitting C (which is case-sensitive).
- No `%` prefix for BIFs: in BBK they are regular functions (`trim`, `substr`, etc.).
- No RPG figurative constants (`*ON`, `*OFF`, `*NULL`, `*ZERO`, `*BLANK`): replaced by C literals (`true`, `false`, `null`, `0`, `" "`). The frontend translates.
- No global indicators (`*IN01`-`*IN99`): the frontend translates them into named booleans.

---

## 1. Whitespace and comments

Discarded by the lexer.

| Token | Description |
|---|---|
| `WHITESPACE` | spaces, tabs, newlines — separates tokens |
| `LINE_COMMENT` | `//` to end of line |
| `BLOCK_COMMENT` | `/* ... */` multiline, not nested |

---

## 2. Identifiers

```
<identifier>  ::=  <letter> { <letter> | <digit> | _ }*
<letter>      ::=  a..z | A..Z
<digit>       ::=  0..9
```

No special characters like `#`, `$`, `@` (those are RPG EBCDIC compatibility, not relevant for BBK).

**Token:** `IDENT`

---

## 3. Literals

### 3.1 Numeric

| Token | Form | Examples |
|---|---|---|
| `INT_LIT` | decimal: `[0-9]+` | `0`, `42`, `100` |
| `INT_LIT_HEX` | `0x[0-9A-Fa-f]+` | `0xFF`, `0x1A2B` |
| `INT_LIT_OCT` | `0o[0-7]+` | `0o755` |
| `FLOAT_LIT` | `[0-9]+ . [0-9]+ ( e [+-]? [0-9]+ )?` | `3.14`, `1.5e10`, `2.0e-3` |
| `DEC_LIT` | `[0-9]+ . [0-9]+ d?` (optional `d` suffix) | `19.95d`, `0.01`, `19.95` |

(`DEC_LIT` distinguishes exact decimal literals from binary floats. **Without the suffix**, the type is inferred from context: if the target is `PACKED`/`ZONED`/`BINDEC` it's treated as an exact decimal; if the target is `FLOAT` or the context is ambiguous, it's treated as a binary float. **With the `d` suffix**, it's always treated as an exact decimal. The suffix follows the Java/C# decimal literal style to disambiguate when needed.)

### 3.2 Strings

| Token | Form | Examples |
|---|---|---|
| `STR_LIT_DOUBLE` | `"..."` with escapes `\n`, `\t`, `\\`, `\"`, `\xNN`, `\uNNNN` | `"hello"`, `"line\n"` |
| `STR_LIT_SINGLE` | `'...'` with escapes `\n`, `\t`, `\\`, `\'`, `\xNN`, `\uNNNN` | `'hello'`, `'it\'s'` |

Both forms are semantically equivalent — they are the same string type. They coexist so that code ported from RPG (which uses `'...'`) doesn't need to rewrite quotes, and so that new code or code ported from C (which typically uses `"..."`) doesn't either. The lexer emits both as `STR_LIT` for the parser; the double/single distinction does not reach the AST.

### 3.3 Booleans and null

| Token | Form |
|---|---|
| `TRUE` | `true` |
| `FALSE` | `false` |
| `NULL` | `null` |

### 3.4 Dates/times/timestamps

**No dedicated literal.** Built with functions:

```
date("2026-05-22")
time("14:30:00")
timestamp("2026-05-22T14:30:00.000000")
```

(Decision to be confirmed: the alternative would be SQL-style prefixed literals `DATE '2026-05-22'`. The functional form is more uniform.)

---

## 4. Punctuators

| Token | Symbol | Use |
|---|---|---|
| `SEMI` | `;` | end of statement |
| `COMMA` | `,` | separator for arguments, fields, etc. |
| `DOT` | `.` | access to a qualified DS member |
| `ARROW` | `->` | member access via pointer, **and** procedure return type |
| `LPAREN` | `(` | open grouping / parameter list |
| `RPAREN` | `)` | close grouping |
| `LBRACE` | `{` | open block |
| `RBRACE` | `}` | close block |
| `LBRACKET` | `[` | open array subscript |
| `RBRACKET` | `]` | close array subscript |
| `COLON` | `:` | separator in `OVERLAY(parent:pos)`, `PACKED(n:d)`, `@halfup` attributes |
| `AT` | `@` | introduces an attribute modifier (e.g. `@halfup`) |

---

## 5. Operators

### 5.1 Arithmetic

| Token | Symbol |
|---|---|
| `PLUS` | `+` |
| `MINUS` | `-` |
| `STAR` | `*` |
| `SLASH` | `/` |
| `PERCENT` | `%` (modulo) |
| `STAR_STAR` | `**` (exponentiation) |

### 5.2 Comparison

| Token | Symbol |
|---|---|
| `EQ_EQ` | `==` |
| `BANG_EQ` | `!=` |
| `LT` | `<` |
| `GT` | `>` |
| `LT_EQ` | `<=` |
| `GT_EQ` | `>=` |

### 5.3 Logical

| Token | Symbol |
|---|---|
| `AMP_AMP` | `&&` |
| `PIPE_PIPE` | `\|\|` |
| `BANG` | `!` |

### 5.4 Bitwise

| Token | Symbol |
|---|---|
| `AMP` | `&` |
| `PIPE` | `\|` |
| `CARET` | `^` |
| `TILDE` | `~` |
| `LT_LT` | `<<` |
| `GT_GT` | `>>` |

### 5.5 Assignment

| Token | Symbol |
|---|---|
| `EQ` | `=` |
| `PLUS_EQ` | `+=` |
| `MINUS_EQ` | `-=` |
| `STAR_EQ` | `*=` |
| `SLASH_EQ` | `/=` |
| `PERCENT_EQ` | `%=` |
| `AMP_EQ` | `&=` |
| `PIPE_EQ` | `\|=` |
| `CARET_EQ` | `^=` |
| `LT_LT_EQ` | `<<=` |
| `GT_GT_EQ` | `>>=` |

### 5.6 Ternary

| Token | Symbol |
|---|---|
| `QUESTION` | `?` |
| `COLON` | `:` (reuses the punctuator token) |

**Not included** (deliberate decision):

- `++` / `--` — risk of sequence point issues, no real win over `+= 1`.
- `,` as an expression operator (C's comma). Confusing, rarely useful.

---

## 6. Keywords — control flow (C style)

| Token | Keyword |
|---|---|
| `KW_IF` | `if` |
| `KW_ELSE` | `else` |
| `KW_WHILE` | `while` |
| `KW_DO` | `do` |
| `KW_FOR` | `for` |
| `KW_BREAK` | `break` |
| `KW_CONTINUE` | `continue` |
| `KW_RETURN` | `return` |

Typical form:

```bbk
if (cond) {
  ...
} else if (cond) {
  ...
} else {
  ...
}

while (cond) {
  ...
}

do {
  ...
} while (cond);

for (i = 0; i < 10; i += 1) {
  ...
}
```

RPG's `DOW` and `DOU` map to `while` and `do...while` respectively.

---

## 7. Keywords — control flow (RPG style retained)

```bbk
select {
  when (type == "A") { ... }
  when (type == "B" || type == "C") { ... }
  other { ... }
}
```

| Token | Keyword |
|---|---|
| `KW_SELECT` | `select` |
| `KW_WHEN` | `when` |
| `KW_OTHER` | `other` |

(We don't use C's `switch`/`case` because `case` requires constants in C; `when` takes an arbitrary boolean expression. The structure is RPG's, the delimiters are C's.)

---

## 8. Keywords — declaration (RPG style, prior decision)

### 8.1 Declaration tokens (one each)

| Token | Keyword | Use |
|---|---|---|
| `KW_DCL_S` | `DCL-S` | standalone variable |
| `KW_DCL_C` | `DCL-C` | named constant |
| `KW_DCL_DS` | `DCL-DS` | data structure |
| `KW_DCL_PR` | `DCL-PR` | prototype (forward declaration) |
| `KW_DCL_PROC` | `DCL-PROC` | procedure |
| `KW_DCL_F` | `DCL-F` | file declaration |
| `KW_DCL_PARM` | `DCL-PARM` | parameter (when explicit) |
| `KW_DCL_SUBF` | `DCL-SUBF` | DS subfield (when explicit) |

**Decision:** the `END-*` (END-DS, END-PR, END-PI, END-PROC) **are not tokens**. `}` replaces them.

### 8.2 Primitive types

| Token | Keyword |
|---|---|
| `KW_CHAR` | `CHAR` |
| `KW_VARCHAR` | `VARCHAR` |
| `KW_PACKED` | `PACKED` |
| `KW_ZONED` | `ZONED` |
| `KW_BINDEC` | `BINDEC` |
| `KW_INT` | `INT` |
| `KW_UNS` | `UNS` |
| `KW_FLOAT` | `FLOAT` |
| `KW_DATE` | `DATE` |
| `KW_TIME` | `TIME` |
| `KW_TIMESTAMP` | `TIMESTAMP` |
| `KW_BOOL` | `BOOL` |
| `KW_POINTER` | `POINTER` |
| `KW_VOID` | `VOID` |

**Notes:**
- RPG's `IND` (indicator = char `'1'`/`'0'`) is replaced by `BOOL` (a real boolean).
- `OBJECT` (RPG's Java object) does not enter; BBK does not target the JVM.
- Length syntax: `CHAR(50)`, `VARCHAR(100)`, `PACKED(9:2)`, `INT(10)`, etc.

### 8.3 Declaration modifiers

| Token | Keyword |
|---|---|
| `KW_INZ` | `INZ` (initializer) |
| `KW_BASED` | `BASED` |
| `KW_DIM` | `DIM` |
| `KW_OVERLAY` | `OVERLAY` |
| `KW_POS` | `POS` |
| `KW_LIKE` | `LIKE` |
| `KW_LIKEDS` | `LIKEDS` |
| `KW_LIKEREC` | `LIKEREC` |
| `KW_TEMPLATE` | `TEMPLATE` |
| `KW_QUALIFIED` | `QUALIFIED` |
| `KW_ALIGN` | `ALIGN` |
| `KW_VALUE` | `VALUE` |
| `KW_CONST` | `CONST` |
| `KW_OPTIONS` | `OPTIONS` |
| `KW_RTNPARM` | `RTNPARM` |
| `KW_OPDESC` | `OPDESC` |
| `KW_STATIC` | `STATIC` |
| `KW_EXPORT` | `EXPORT` |
| `KW_IMPORT` | `IMPORT` |
| `KW_EXTPGM` | `EXTPGM` |
| `KW_EXTPROC` | `EXTPROC` |

### 8.4 `DCL-F`-specific keywords

| Token | Keyword |
|---|---|
| `KW_USAGE` | `USAGE` |
| `KW_KEYED` | `KEYED` |
| `KW_EXTNAME` | `EXTNAME` |
| `KW_EXTFILE` | `EXTFILE` |
| `KW_PREFIX` | `PREFIX` |
| `KW_RENAME` | `RENAME` |
| `KW_DISK` | `DISK` |
| `KW_PRINTER` | `PRINTER` |
| `KW_WORKSTN` | `WORKSTN` |
| `KW_SEQ` | `SEQ` |
| `KW_USROPN` | `USROPN` |
| `KW_INFDS` | `INFDS` |
| `KW_INDDS` | `INDDS` |

### 8.5 USAGE arguments

`USAGE(...)` arguments are not dedicated keywords but special constants:

| Form | Meaning |
|---|---|
| `*INPUT` | input file |
| `*OUTPUT` | output |
| `*UPDATE` | update |
| `*DELETE` | delete |

They are recognized as **identifiers with a `*` prefix** via the generic `STAR_IDENT` token. This avoids having to list a dedicated token for every figurative constant, and leaves the door open to add new ones (`*NEW`, `*CALLER`, `*ALL`, etc.) without touching the lexer.

---

## 9. Directives — one token each

### 9.1 Module directive

| Token | Symbol |
|---|---|
| `KW_CTL_OPT` | `CTL-OPT` |

**On the directive prefix:** RPG uses `/IF`, `/INCLUDE`, etc. with `/` at the front. In BBK we replace `/` with the compound prefix `PRE-` (consistent with `DCL-S`, `DCL-DS`, etc.). It serves to distinguish directives from control-flow keywords (`if`, `else`) without needing an "annoying" symbol up front.

### 9.2 Conditional compilation directives

| Token | Keyword |
|---|---|
| `KW_PRE_IF` | `PRE-IF` |
| `KW_PRE_ELSEIF` | `PRE-ELSEIF` |
| `KW_PRE_ELSE` | `PRE-ELSE` |
| `KW_PRE_ENDIF` | `PRE-ENDIF` |
| `KW_PRE_DEFINE` | `PRE-DEFINE` |
| `KW_PRE_UNDEFINE` | `PRE-UNDEFINE` |

(`PRE-ENDIF` is kept because directives don't use `{ }` blocks — they are line-oriented as in RPG/C.)

### 9.3 Inclusion directive

| Token | Keyword |
|---|---|
| `KW_PRE_INCLUDE` | `PRE-INCLUDE` |

Unified — RPG's `/COPY` and `/INCLUDE` collapse into a single token. The semantics of "include a source file at this point" is the same.

### 9.4 End-of-source directive

| Token | Keyword |
|---|---|
| `KW_PRE_EOF` | `PRE-EOF` |

**Discarded** (no value in a modern compiler):
- `/EJECT`, `/TITLE`, `/SPACE` (legacy from the IBM compiler listing)

---

## 10. Attribute modifiers (novel intermediate syntax)

Tokens introduced with `@` to modify a statement's behavior, without needing wrapping keywords like `EVAL(H)`.

| Token | Symbol | Meaning |
|---|---|---|
| `ATTR_HALFUP` | `@halfup` | half-up rounding (equivalent to `EVAL(H)`) |
| `ATTR_HALFDOWN` | `@halfdown` | half-down rounding |
| `ATTR_TRUNC` | `@trunc` | truncate (default; mostly to be explicit) |

Use:

```bbk
total = price / quantity @halfup;
```

(Decision: hardcoded closed list, or free `@<ident>` with parser validation. I recommend a closed list for initial simplicity.)

---

## 11. Summary — token categories

```
Whitespace / Comments         (3)    — WHITESPACE, LINE_COMMENT, BLOCK_COMMENT (discarded by the lexer)
Identifiers                   (2)    — IDENT, STAR_IDENT
Literals                      (~7)   — INT_LIT, INT_LIT_HEX, INT_LIT_OCT, FLOAT_LIT, DEC_LIT, STR_LIT, TRUE, FALSE, NULL
Punctuators                   (12)   — ; , . -> ( ) { } [ ] : @
Operators                     (~30)  — arithmetic, comparison, logical, bitwise, assignment, ternary
Control flow C-style          (8)    — if, else, while, do, for, break, continue, return
Control flow RPG-style        (3)    — select, when, other
Declaration keywords          (9)    — DCL-S, DCL-C, DCL-DS, DCL-PR, DCL-PI, DCL-PROC, DCL-F, DCL-PARM, DCL-SUBF
Primitive types               (~14)  — CHAR, VARCHAR, PACKED, ZONED, BINDEC, INT, UNS, FLOAT, DATE, TIME, TIMESTAMP, BOOL, POINTER, VOID
Declaration modifiers         (~20)  — INZ, BASED, DIM, OVERLAY, POS, LIKE, LIKEDS, LIKEREC, TEMPLATE, QUALIFIED, ALIGN, VALUE, CONST, OPTIONS, RTNPARM, OPDESC, STATIC, EXPORT, IMPORT, EXTPGM, EXTPROC
File-spec keywords            (~13)  — USAGE, KEYED, EXTNAME, EXTFILE, PREFIX, RENAME, DISK, PRINTER, WORKSTN, SEQ, USROPN, INFDS, INDDS
Module directive              (1)    — CTL-OPT
Compilation directives        (8)    — PRE-IF, PRE-ELSEIF, PRE-ELSE, PRE-ENDIF, PRE-DEFINE, PRE-UNDEFINE, PRE-INCLUDE, PRE-EOF
Attribute modifiers           (3)    — @halfup, @halfdown, @trunc

APPROX TOTAL                  ~130 tokens
```

(Note: the string literals line is made up of `STR_LIT_DOUBLE` and `STR_LIT_SINGLE`, both collapsing to `STR_LIT` in the AST.)

Comparison: C99 has ~50 tokens, RPG IV has ~200+ (counting opcodes and BIFs). BBK sits in the middle, which is consistent with its role as an intermediate IR.

---

## 12. Closed decisions (resolved)

Items that were open in the first version of this document and have now been resolved:

| # | Decision | Resolution |
|---|---|---|
| 1 | Case sensitivity | **Case-insensitive everywhere.** Keywords and identifiers. The frontend normalizes before emitting C. |
| 2 | Multiline comments | **Included.** `/* ... */`, not nested. |
| 3 | `d` suffix for decimal literals | **Optional.** Without the suffix, the type is inferred from the target's context; with the `d` suffix it's always exact decimal. |
| 4 | Quotes for strings | **Both.** `"..."` and `'...'` equivalent. |
| 5 | Date/time as literals or functions | **Functions.** `date("2026-05-22")`, not `D"..."` prefixes. |
| 6 | `/COPY` vs `/INCLUDE` | **Unified** in `PRE-INCLUDE`. |
| 7 | `*INPUT`/`*OUTPUT` as tokens or identifiers with `*` prefix | **Identifiers with `*` prefix** (token `STAR_IDENT`). |
| 8 | Attribute modifiers — closed or open list | **Closed.** Only `@halfup`, `@halfdown`, `@trunc`. |
| 9 | `LIKEREC` in V1 | **Yes, included.** |
| 10 | Keyword case | **N/A** — everything is case-insensitive (see #1). |

**Additional change from the previous version:** directive prefix `/X` → `PRE-X` (see §9.1).

---

## 13. Suggested next documents in `boxbreaker/`

Once the open decisions are closed:

- `lexical.md` — formal lexical grammar (regex/EBNF for each category)
- `grammar.md` — syntactic grammar (productions)
- `type-system.md` — type system, conversions, promotions
- `semantics.md` — execution semantics
- `examples.md` — example BBK programs, with their RPG origin and C output
