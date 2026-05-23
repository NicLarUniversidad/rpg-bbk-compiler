# BBK Grammar — Layer 6

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Prerequisites:** [`layer1.md`](layer1.md) → [`layer5.md`](layer5.md)
**Test files:** [`11-directives.bbk`](../../../tests/boxbreaker/examples/11-directives.bbk) (valid), [`bad-15-bad-directives.bbk`](../../../tests/boxbreaker/examples/bad/bad-15-bad-directives.bbk) (intentional errors)

---

## Scope

Layer 6 closes out the main syntactic surface with **preprocessor directives** (`PRE-*`). Ideally these are all handled in a pre-parser phase, but for simplicity we integrate them as statements in the main grammar.

| Directive | Form | Example |
|---|---|---|
| `PRE-IF` / `PRE-ELSEIF` / `PRE-ELSE` / `PRE-ENDIF` | Conditional compilation | `PRE-IF DEFINED(DEBUG) ... PRE-ENDIF` |
| `PRE-DEFINE` | Define a macro (with or without value) | `PRE-DEFINE VERSION "1.0.0"` or `PRE-DEFINE DEBUG` |
| `PRE-UNDEFINE` | Remove a macro | `PRE-UNDEFINE DEBUG` |
| `PRE-INCLUDE` | Include another file | `PRE-INCLUDE "common-types.bbki"` |
| `PRE-EOF` | Premature end of source | `PRE-EOF` |

**After L6 — the main syntax is complete.** What follows are semantic refinements and IDE improvements, not new grammar.

---

## Strategy: directives as flat statements

Unlike many preprocessors (C/C++) where IF/ENDIF form a nested syntactic block, in BBK **each directive is a flat, independent statement**. The `PRE-IF`↔`PRE-ENDIF` matching is validated later (semantically).

**Trade-off:** the parser does not automatically report "orphan PRE-ELSE" or "missing PRE-ENDIF". A future semantic analyzer will handle that.

**Benefit:** the grammar is simpler, with no recursive nesting of directives, and directives can appear anywhere (top-level or inside blocks) without parallel grammars.

---

## BNF productions

### General category

```bnf
directive ::= pre_if_directive
            | pre_elseif_directive
            | pre_else_directive
            | pre_endif_directive
            | pre_define_directive
            | pre_undefine_directive
            | pre_include_directive
            | pre_eof_directive
```

### Conditional compilation

```bnf
pre_if_directive     ::= KW_PRE_IF expression {pin=1}
pre_elseif_directive ::= KW_PRE_ELSEIF expression {pin=1}
pre_else_directive   ::= KW_PRE_ELSE
pre_endif_directive  ::= KW_PRE_ENDIF
```

The condition is a full `expression` (from L4). That means `PRE-IF` accepts:
- Bare IDENT: `PRE-IF DEBUG` (means "if DEBUG is defined and truthy")
- Function calls: `PRE-IF DEFINED(DEBUG)` — `DEFINED` parses as an IDENT and `(DEBUG)` as a call
- Negation: `PRE-IF !DEFINED(DEBUG)`
- Booleans: `PRE-IF DEFINED(DEBUG) && !DEFINED(PRODUCTION)`
- Constants: `PRE-IF VERSION_MAJOR > 2`
- Literals: `PRE-IF true` (always includes)

**Note on `NOT`:** BBK uses `!` for negation. The RPG-style `NOT DEFINED(X)` is not valid here; use `!DEFINED(X)`.

### Macro definitions

```bnf
pre_define_directive   ::= KW_PRE_DEFINE IDENT pre_define_value? {pin=1}
pre_undefine_directive ::= KW_PRE_UNDEFINE IDENT {pin=1}

private pre_define_value ::= literal | STAR_IDENT
```

Two forms of `PRE-DEFINE`:
- **Flag (no value):** `PRE-DEFINE DEBUG` — the macro is defined, with no replacement value
- **With value:** `PRE-DEFINE VERSION "1.0.0"` — the macro is replaced by the literal

The value can be any `literal` (string, number, hex, etc.) or a `STAR_IDENT` (`*NO`, `*YES`). IDENT is **not** allowed as a value, to avoid ambiguity with the following directive.

### Inclusion

```bnf
pre_include_directive ::= KW_PRE_INCLUDE (STR_LIT | IDENT) {pin=1}
```

Accepts two forms:
- `PRE-INCLUDE "common-types.bbki"` — string literal (most common; supports paths with special characters)
- `PRE-INCLUDE common_types` — identifier (RPG `/COPY libname` style)

Decision settled in `tokens.md`: `/COPY` and `/INCLUDE` were unified into `PRE-INCLUDE`.

### EOF

```bnf
pre_eof_directive ::= KW_PRE_EOF
```

Marks premature end of source. Everything after `PRE-EOF` is ignored (the preprocessor does not emit it to the main parser).

### Integration with top-level and blocks

```bnf
top_level_item ::= variable_declaration
                 | constant_declaration
                 | ctl_opt_statement
                 | data_structure_declaration
                 | file_declaration
                 | prototype_declaration
                 | procedure_declaration
                 | directive             // L6
                 | unknown_item

block_item ::= variable_declaration
             | constant_declaration
             | data_structure_declaration
             | subroutine_definition
             | directive                 // L6
             | statement
             | unknown_block_item
```

Directives are valid **both at module level and inside procedure bodies** (and by extension, inside any block). This enables:

```bbk
PRE-DEFINE DEBUG

DCL-PROC main {
  DCL-S x INT(10);

  PRE-IF DEFINED(DEBUG)
    print("debug build");
  PRE-ELSE
    print("production build");
  PRE-ENDIF

  return;
}
```

---

## Examples

### Valid — `11-directives.bbk`

```bbk
PRE-DEFINE VERSION "1.0.0"
PRE-DEFINE DEBUG

PRE-INCLUDE "common-types.bbki"

CTL-OPT MAIN(directivesDemo);

DCL-C MAX_BUFFER 1024;

DCL-PROC directivesDemo {
  DCL-S buffer CHAR(MAX_BUFFER);

  PRE-IF DEFINED(DEBUG)
    print("Debug build, version " + VERSION);
  PRE-ELSE
    print("Production build");
  PRE-ENDIF

  PRE-IF DEFINED(FEATURE_LOGGING)
    log("Demo started");
  PRE-ENDIF
}

// Conditional inclusion of additional source files
PRE-IF DEFINED(EXTENDED_FEATURES)
  PRE-INCLUDE "extended-procs.bbki"
PRE-ELSEIF DEFINED(LITE_MODE)
  PRE-INCLUDE "lite-procs.bbki"
PRE-ENDIF
```

Layer 6 recognizes every directive plus the conditionally compiled content between them (which is parsed by the rest of the grammar as if the directives weren't there).

### Errors detected — `bad-15-bad-directives.bbk`

| Category | Cases |
|---|---|
| PRE-DEFINE without a name | `PRE-DEFINE` |
| PRE-DEFINE with a non-IDENT name | `PRE-DEFINE "STRING_NAME"`, `PRE-DEFINE 123` |
| PRE-UNDEFINE missing name or with wrong type | `PRE-UNDEFINE`, `PRE-UNDEFINE "S"` |
| PRE-INCLUDE without a file | `PRE-INCLUDE`, `PRE-INCLUDE 42` |
| PRE-IF / PRE-ELSEIF without a condition | `PRE-IF`, `PRE-ELSEIF` |
| Incomplete condition | `PRE-IF DEFINED(` |

**Cases that do NOT flag errors** (intentionally):
- Orphan `PRE-ELSE` (no preceding PRE-IF) — the parser does not validate pairing
- Orphan `PRE-ENDIF` — ditto
- `PRE-EOF` anywhere — ditto

Those cases will be reported by a future semantic analyzer.

---

## Implementation notes

### No SEMI on directives

Directives do not end with `;`. This differs from the rest of BBK (where every statement ends in `;`). It's a deliberate decision to align with the RPG convention of line-oriented directives.

**How the parser knows where a directive ends:** by greedy matching of its fixed content. For example, `PRE-DEFINE VERSION "1.0.0"` consumes `KW_PRE_DEFINE`, `IDENT (VERSION)`, and a `pre_define_value` (the string literal). After that, the next token starts a new `top_level_item` (or `block_item`).

If after `PRE-DEFINE VERSION` there's another IDENT instead of a literal:
```
PRE-DEFINE VERSION
otherIdent
```

`pre_define_value` doesn't match IDENT by design (only `literal | STAR_IDENT`). So `otherIdent` is left as the next item to parse. This avoids the ambiguity of "is this IDENT the value of the previous define or a new statement?".

### Why `expression` for the PRE-IF condition

```bnf
pre_if_directive ::= KW_PRE_IF expression {pin=1}
```

Instead of inventing an ad-hoc `pre_condition`, we reuse `expression` (from L4). That gives `PRE-IF` the full power of expressions:

```bbk
PRE-IF DEFINED(DEBUG)
PRE-IF !DEFINED(PRODUCTION)
PRE-IF VERSION_MAJOR > 2
PRE-IF DEFINED(DEBUG) && !DEFINED(LITE_MODE)
PRE-IF (FEATURES & 0x1) != 0
```

All of that parses with the same rule. The semantics (what "DEFINED" means, how it's evaluated at preprocessing time) is the preprocessing phase's responsibility, not the parser's.

### `DEFINED(X)` parses as a function call

`DEFINED` is not a keyword in BBK. It parses as an IDENT. `DEFINED(X)` is therefore a `postfix_expression`:
- `DEFINED` (primary, IDENT)
- `(X)` (postfix_suffix, function call)

The semantic preprocessor must recognize "this call in PRE-IF context means: is X defined?".

Same pattern as with BIFs like `trim(x)`, `len(s)`, etc. — the parser doesn't know anything special; it recognizes them as generic calls.

### IF/ENDIF matching is semantic

The parser does **not** validate that a `PRE-IF` has a matching `PRE-ENDIF`. Each directive is a flat item. The (future) semantic validator will walk the PSI tree checking:
- Every `PRE-IF` has a subsequent `PRE-ENDIF` before EOF
- `PRE-ELSE` and `PRE-ELSEIF` are inside an open PRE-IF
- `PRE-ELSE` appears at most once per block
- Etc.

If the complexity of keeping this separate becomes annoying, we can refactor into a `pre_if_block` that wraps the items with real nesting. For now, simpler is better.

---

## Project status after Layer 6

**The BBK syntax is complete.** The 6 layers cover the language's entire surface:

| Layer | Focus | Status |
|---|---|---|
| L1 | Variables, constants | ✅ |
| L2 | Module (CTL-OPT, files, data structures) | ✅ |
| L3 | Procedures | ✅ |
| L4 | Statements + expressions | ✅ |
| L5 | File ops, subroutines, CALLP | ✅ |
| L6 | Directives | ✅ |

**What's next (outside the "syntax" scope):**

1. **IDE plugin improvements:**
   - Update `BbkSyntaxHighlighter` with the L5 and L6 keywords (file ops, subroutines, directives) — without this they appear without special color
   - PSI structure view
   - Code folding per block and per subroutine
   - Brace matching
   - Comment toggle (`//` and `/* */`)
   - Keyword table for basic autocomplete

2. **Semantic analysis:**
   - Type checking (`DCL-S x INT(10); x = "string"` should fail)
   - Scope resolution (references to undeclared variables)
   - IF/ENDIF, BEGSR/ENDSR matching
   - Resolution of prototypes vs definitions
   - Validation of call arguments (count and type)

3. **Code generation** (lowering BBK → C):
   - The backend in `bbk-compiler`
   - The runtime in `bbk-runtime` (custom decimal BCD, IBM i emulation)

4. **RPG → BBK frontend** (`rpg-frontend`):
   - RPG parser (fixed + free form)
   - RPG → AST → BBK translation
