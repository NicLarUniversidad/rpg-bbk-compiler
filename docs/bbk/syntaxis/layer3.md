# BBK Grammar — Layer 3

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Prerequisites:** [`layer1.md`](layer1.md), [`layer2.md`](layer2.md)
**Test files:** [`05-procedures.bbk`](../../../tests/boxbreaker/examples/05-procedures.bbk) (valid), [`bad-12-bad-procedures.bbk`](../../../tests/boxbreaker/examples/bad/bad-12-bad-procedures.bbk) (intentional errors)

---

## Scope

Layer 3 brings **procedures** to the language:

| Construct | Form | Example |
|---|---|---|
| `DCL-PR` | Prototype (forward declaration) | `DCL-PR sum(a INT(10) VALUE, b INT(10) VALUE) -> INT(10);` |
| `DCL-PROC` | Definition with body | `DCL-PROC sum(a INT(10), b INT(10)) -> INT(10) EXPORT { return a + b; }` |

**Scope decision to keep L3 bounded:** the procedure **body** supports nested declarations (`DCL-S`/`DCL-C`/`DCL-DS`) and anything else falls through to the `unknown_proc_item` fallback. That means real statements and expressions **are not parsed yet** — they come in Layer 4. But the procedure's outer structure (signature, params, return, body) is parsed and validated.

**Still not covered:**
- Statements (`if`, `while`, `for`, `select`, `return`, etc.) — Layer 4
- Expressions (`a + b`, `f(x)`, etc.) — Layer 5
- File ops (`read`, `chain`, `write`, etc.) — Layer 6
- `monitor`/`on-error`/`on-exit` — Layer 6
- `PRE-*` directives — Layer 6

---

## Updated top-level

```bnf
top_level_item ::= variable_declaration       // L1
                 | constant_declaration        // L1
                 | ctl_opt_statement           // L2
                 | data_structure_declaration  // L2
                 | file_declaration            // L2
                 | prototype_declaration       // L3
                 | procedure_declaration       // L3
                 | unknown_item
```

---

## BNF productions

### Prototype declaration

```bnf
prototype_declaration ::= KW_DCL_PR IDENT inline_param_list? return_type? pr_modifier* SEMI {pin=1}
```

**Form:** `DCL-PR name (params)? (-> RetType)? modifiers* ;`

Examples:
```bbk
DCL-PR sum(a INT(10) VALUE, b INT(10) VALUE) -> INT(10);
DCL-PR greet(name VARCHAR(50) CONST);                   // no return
DCL-PR factorial(n INT(10) VALUE) -> INT(10);
DCL-PR CUSTPROG(custId INT(10) VALUE, status INT(10)) EXTPGM("CUSTPROG");
```

### Procedure definition

```bnf
procedure_declaration ::= KW_DCL_PROC IDENT inline_param_list? return_type? proc_modifier* procedure_body {pin=1}

procedure_body ::= LBRACE proc_body_item* RBRACE {pin=1}

proc_body_item ::= variable_declaration
                 | constant_declaration
                 | data_structure_declaration
                 | unknown_proc_item

// Body fallback: consume any token but stop at the closing RBRACE.
private unknown_proc_item ::= !RBRACE !<<eof>> any_token
```

**Form:** `DCL-PROC name (params)? (-> RetType)? modifiers* { body }`

Examples:
```bbk
DCL-PROC main {
  print("hello");
}

DCL-PROC sum(a INT(10) VALUE, b INT(10) VALUE) -> INT(10) EXPORT {
  return a + b;
}

DCL-PROC processOrder(rec LIKEDS(orderRec) CONST) {
  print("processing");
}
```

The body supports local `DCL-S`/`DCL-C`/`DCL-DS` with full analysis, and the rest (statements, expressions, function calls) goes through `unknown_proc_item`. The key for the fallback is **`!RBRACE`**: it consumes any token except `}`, so the final `RBRACE` correctly closes the body.

### Inline parameter list

```bnf
inline_param_list ::= LPAREN inline_params? RPAREN {pin=1}

private inline_params ::= inline_param (COMMA inline_param)*

inline_param ::= IDENT type_specification param_modifier* {pin=2}
```

**Form:** `(name TYPE modifier*, name TYPE modifier*, ...)`

Details:
- **Comma-separated** (not colon, unlike CTL-OPT/USAGE).
- Parentheses may be empty: `()` for procedures without parameters.
- `inline_param {pin=2}` commits after seeing `IDENT type_specification`. If the type fails, it backtracks (useful to tolerate stray IDENTs in other contexts without generating spurious errors).

### Return type

```bnf
return_type ::= ARROW type_specification {pin=1}
```

**Form:** `-> TYPE`

Without a return type, the procedure is `void` (returns nothing). With a return type, the body must use `return expression;`.

### Procedure-level modifiers

```bnf
proc_modifier ::= KW_EXPORT | extproc_modifier

extproc_modifier ::= KW_EXTPROC LPAREN STR_LIT RPAREN {pin=1}
```

- `EXPORT` — the procedure is visible outside the module
- `EXTPROC("name")` — use this external name instead of the BBK name (for custom naming or calls from procs in other languages)

### Prototype-level modifiers

```bnf
pr_modifier ::= extpgm_modifier
              | extproc_modifier
              | KW_OPDESC
              | KW_RTNPARM

extpgm_modifier ::= KW_EXTPGM LPAREN STR_LIT RPAREN {pin=1}
```

- `EXTPGM("PGMNAME")` — the prototype is for an external IBM i `*PGM` program (not a sub-procedure). Invoked with `CALL` in legacy RPG; in BBK it's transparent.
- `EXTPROC("name")` — same as in proc_modifier
- `OPDESC` — passes operational descriptors (type metadata) alongside args
- `RTNPARM` — special convention where the "return" is delivered through an implicit parameter

### Parameter modifiers

```bnf
param_modifier ::= KW_VALUE
                 | KW_CONST
                 | KW_OPDESC
                 | options_modifier

options_modifier ::= KW_OPTIONS LPAREN STAR_IDENT (COLON STAR_IDENT)* RPAREN {pin=1}
```

- `VALUE` — pass by value (BBK default is by-reference)
- `CONST` — by-reference immutable: allows passing literals/expressions that the callee can't modify
- `OPDESC` — passes the operational descriptor for this parameter
- `OPTIONS(*NOPASS:*OMIT:*VARSIZE:*STRING:*NULLIND)` — additional flags (optional parameter, omittable, variable size, string conversion, null support)

---

## Full examples

### Valid — `05-procedures.bbk`

```bbk
CTL-OPT MAIN(proceduresDemo);

// Forward prototypes
DCL-PR sum(a INT(10) VALUE, b INT(10) VALUE) -> INT(10);
DCL-PR greet(name VARCHAR(50) CONST);
DCL-PR factorial(n INT(10) VALUE) -> INT(10);
DCL-PR CUSTPROG(customerId INT(10) VALUE, status INT(10)) EXTPGM("CUSTPROG");

DCL-PROC proceduresDemo {
  DCL-S total      INT(10);
  DCL-S factResult INT(10);
  DCL-S custStatus INT(10);

  total = sum(5, 7);                       // statement: unknown_proc_item
  print("5 + 7 = " + char(total));         // statement: unknown_proc_item
  greet("World");                          // statement: unknown_proc_item
  factResult = factorial(5);               // statement: unknown_proc_item
  CUSTPROG(12345, custStatus);             // statement: unknown_proc_item
}

DCL-PROC sum(a INT(10) VALUE, b INT(10) VALUE) -> INT(10) EXPORT {
  return a + b;                            // statement: unknown_proc_item
}

DCL-PROC factorial(n INT(10) VALUE) -> INT(10) {
  if (n <= 1) {                            // statement: unknown_proc_item
    return 1;
  }
  return n * factorial(n - 1);
}
```

Layer 3 recognizes the entire outer structure (CTL-OPT, prototypes, definitions, params, return types, modifiers, body delimiters, internal declarations). The lines marked `// statement` are consumed as individual tokens via `unknown_proc_item` without generating errors or structured PSI — that's Layer 4's responsibility.

### Errors detected — `bad-12-bad-procedures.bbk`

| Case | Approximate line |
|---|---|
| `DCL-PR;` — missing name | Prototype without IDENT |
| `DCL-PR sum(;` — unclosed params | Unterminated args |
| `DCL-PR sum(a);` — param without type | IDENT alone, no type_specification |
| `DCL-PR sum() ->;` — empty return type | ARROW without TYPE |
| `DCL-PR foo EXTPGM(MyProgram);` — wrong arg type | EXTPGM expects STR_LIT, got IDENT |
| `DCL-PROC main;` (without body) | Missing `{ ... }` |
| `DCL-PROC main(a INT(10),) {` | Trailing comma in params |
| `DCL-PROC body1 { ... ` without `}` | Unclosed body |
| `DCL-S x;` inside body | L1 error (missing type) correctly reported inside the body |

**Cases that do NOT flag errors** (because they fall through to the body fallback):
- `total = 5 + 7;` — statement not implemented
- `print("anything");` — call not implemented
- `if (x > 0) { doSomething(); }` — control flow not implemented

That is correct for L3 — Layer 4 will flag them.

---

## Implementation notes

### The `unknown_proc_item` fallback

```bnf
private unknown_proc_item ::= !RBRACE !<<eof>> any_token
```

The double negation `!RBRACE !<<eof>>` is the key design choice:
- `!RBRACE` — negative lookahead: only proceed if the next token is NOT `}`
- `!<<eof>>` — negative lookahead for EOF

Without this, the fallback would consume the final `}` and the `RBRACE` required by `procedure_body` would never be found, yielding a confusing error at end-of-file.

### Pin position in `inline_param`

```bnf
inline_param ::= IDENT type_specification param_modifier* {pin=2}
```

`pin=2` means "pin after the first two elements" (IDENT + type_specification). This matters because:

- **Seeing only an IDENT** (without type): backtrack, no error. This allows a stray IDENT in some unusual context (unlikely, but defensive) to not force errors.
- **Once the `IDENT TYPE` combination is seen**: commit. If `)` is missing or a strange token appears later, an error is reported.

### `LIKEDS` in parameters (uniform with DCL-S)

Parameters use the same `type_specification` as `DCL-S`, so `LIKEDS(...)`, `LIKEREC(...)`, `LIKE(...)` work identically:

```bbk
DCL-PR processOrder(rec LIKEDS(orderRec) CONST);
DCL-PR processArray(arr LIKE(myArray) VALUE);
```

### EXTPGM vs EXTPROC difference

Both take `(STR_LIT)`. They are only allowed in different contexts:

| Modifier | Where allowed | Meaning |
|---|---|---|
| `EXTPGM` | Only in `DCL-PR` (not in DCL-PROC) | The prototype is for an external `*PGM` program |
| `EXTPROC` | Both in `DCL-PR` and `DCL-PROC` | External name of the procedure (different from the BBK name) |

This is reflected in the BNF: `pr_modifier` includes both, `proc_modifier` includes only `extproc_modifier`.

### Procedure body does NOT permit certain top-level items

Inside a `procedure_body`, **the following are not permitted**:
- `DCL-F` (files are module-global)
- `DCL-PR` (prototypes belong at module level)
- `DCL-PROC` (RPG does not support nested procedures)
- `CTL-OPT` (it's a module directive)

This is reflected in `proc_body_item`, which only includes `variable_declaration | constant_declaration | data_structure_declaration | unknown_proc_item`. If someone tries `DCL-PROC nested { DCL-PROC inner { ... } }`, the inner `DCL-PROC` falls through to `unknown_proc_item` and may eventually confuse the closing brace (the inner `{` opens a block that swallows the outer `}`). Layer 4 could improve this by adding an explicit error.

---

## Next layer

`layer4.md` (pending) — will add **statements**: `if`/`else`, `while`, `do/while`, `for`, `select`/`when`/`other`, `return`, `break`, `continue`, assignments (`x = expr;`), and call-as-statement (`f(args);`).

Layer 5 will then add **expressions** with the full precedence hierarchy (arithmetic, comparison, logical, bitwise, ternary, member access, subscript, function calls as expressions).
