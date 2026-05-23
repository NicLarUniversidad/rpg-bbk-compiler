# BBK Grammar — Layer 1

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Test files:** [`tests/boxbreaker/examples/`](../../../tests/boxbreaker/examples/) (valid) and [`tests/boxbreaker/examples/bad/`](../../../tests/boxbreaker/examples/bad/) (with intentional errors)

---

## Scope

Layer 1 is the first functional layer of the BBK parser. It recognizes the two simplest module-level declarations:

| Construct | Form | Example |
|---|---|---|
| `DCL-S` | Standalone variable | `DCL-S counter INT(10) INZ(0);` |
| `DCL-C` | Named constant | `DCL-C MAX_RETRIES 5;` |

**What Layer 1 does NOT cover** (falls through to the `unknown_item` fallback without reporting errors):
- `CTL-OPT`, `DCL-F`, `DCL-DS` — covered in Layer 2
- `DCL-PR`, `DCL-PROC` — Layer 3
- Statements, expressions, file ops, `PRE-*` directives — Layers 4-6

---

## BNF productions

### Top-level

```bnf
translation_unit ::= top_level_item*

top_level_item ::= variable_declaration
                 | constant_declaration
                 | unknown_item
                 // (Layer 2+ adds more alternatives)
```

### Variable declaration

```bnf
variable_declaration ::= KW_DCL_S IDENT type_specification var_modifier* SEMI {pin=1}
```

`{pin=1}` means that once `DCL-S` is consumed, the parser commits to the rule. Any subsequent failure is reported as an error rather than triggering a backtrack.

### Constant declaration

```bnf
constant_declaration ::= KW_DCL_C IDENT constant_value SEMI {pin=1}

constant_value ::= const_wrapper | literal | KW_TRUE | KW_FALSE | KW_NULL

const_wrapper ::= KW_CONST LPAREN (literal | IDENT) RPAREN {pin=1}
```

`DCL-C` accepts:
- Direct literals: `DCL-C PI 3.14159d;`
- Figurative constants: `DCL-C IS_DEBUG true;`
- Expression wrapped in `CONST(...)`: `DCL-C DOUBLE CONST(MAX * 2);` (Layer 1 only allows literal or IDENT inside; real expressions come with Layer 5)

### Type specification

```bnf
type_specification ::= like_reference | primitive_type_spec

primitive_type_spec ::= primitive_type type_args?

primitive_type ::= KW_CHAR | KW_VARCHAR
                 | KW_PACKED | KW_ZONED | KW_BINDEC
                 | KW_INT | KW_UNS | KW_FLOAT
                 | KW_DATE | KW_TIME | KW_TIMESTAMP
                 | KW_BOOL | KW_POINTER | KW_VOID

type_args ::= LPAREN INT_LIT type_args_tail? RPAREN {pin=1}
private type_args_tail ::= COLON INT_LIT {pin=1}

like_reference ::= (KW_LIKE | KW_LIKEDS | KW_LIKEREC) LPAREN IDENT RPAREN {pin=1}
```

**Notes:**
- `type_args` has `{pin=1}` so that cases like `CHAR()` or `INT(;` report an error instead of silently backtracking.
- `like_reference` reuses the `KW_LIKE`/`KW_LIKEDS`/`KW_LIKEREC` tokens (the same three used as DCL-DS modifiers, but here they act as a type-spec replacement).

### Modifiers

```bnf
var_modifier ::= inz_modifier
               | static_modifier
               | export_modifier
               | dim_modifier
               | based_modifier
               | qualified_modifier
               | overlay_modifier
               | pos_modifier

inz_modifier        ::= KW_INZ LPAREN modifier_value RPAREN {pin=1}
static_modifier     ::= KW_STATIC
export_modifier     ::= KW_EXPORT
dim_modifier        ::= KW_DIM LPAREN INT_LIT (COLON INT_LIT)? RPAREN {pin=1}
based_modifier      ::= KW_BASED LPAREN IDENT RPAREN {pin=1}
qualified_modifier  ::= KW_QUALIFIED
overlay_modifier    ::= KW_OVERLAY LPAREN IDENT (COLON INT_LIT)? RPAREN {pin=1}
pos_modifier        ::= KW_POS LPAREN INT_LIT RPAREN {pin=1}
```

`overlay_modifier` and `pos_modifier` were added with `DCL-DS` subfields in mind (Layer 2), but they also apply to `DCL-S BASED(ptr)` for overlays over based memory.

### Literal

```bnf
literal ::= INT_LIT | INT_LIT_HEX | INT_LIT_OCT | FLOAT_LIT | DEC_LIT | STR_LIT
```

### Fallback

```bnf
private unknown_item ::= !<<eof>> any_token
external any_token ::= consumeAnyToken
```

`any_token` is implemented in Java (`BbkParserUtil.consumeAnyToken`) and advances the lexer one token at a time.

---

## Examples

### Valid code (should not flag any errors)

```bbk
DCL-S counter   INT(10) INZ(0);
DCL-S name      VARCHAR(100) INZ("");
DCL-S price     PACKED(9:2) INZ(19.95d);
DCL-S birthDate DATE;
DCL-S isActive  BOOL INZ(true);
DCL-S nums      INT(10) DIM(100);
DCL-S matrix    INT(10) DIM(10:10);
DCL-S basedInt  INT(10) BASED(ptr);
DCL-S priceCopy LIKE(price);
DCL-S sharedCnt INT(10) STATIC;

DCL-C MAX_RETRIES 5;
DCL-C PI          3.14159d;
DCL-C COMPANY     "Acme Corporation";
DCL-C IS_DEBUG    true;
DCL-C DOUBLED     CONST(MAX_RETRIES * 2);
```

See [`02-variables.bbk`](../../../tests/boxbreaker/examples/02-variables.bbk) and [`03-constants.bbk`](../../../tests/boxbreaker/examples/03-constants.bbk).

### Errors detected

The files in [`bad/`](../../../tests/boxbreaker/examples/bad/) cover the following cases:

| File | Error type |
|---|---|
| `bad-01-missing-type.bbk` | DCL-S without type (common: putting a modifier directly as the type) |
| `bad-02-missing-semicolon.bbk` | Missing `;` at the end |
| `bad-03-missing-name.bbk` | Missing IDENT for the name |
| `bad-04-bad-type-args.bbk` | Malformed type args: `CHAR()`, `INT(;`, `PACKED(9:)`, etc. |
| `bad-05-bad-modifiers.bbk` | INZ/DIM/BASED with invalid args |
| `bad-06-missing-constant-value.bbk` | DCL-C without value or with bad CONST |
| `bad-07-bad-like-reference.bbk` | LIKE/LIKEDS/LIKEREC with invalid args |
| `bad-08-mixed-errors.bbk` | Multiple errors mixed with valid code (tests recovery) |

---

## Implementation notes

### Pins

Each main rule uses `{pin=1}` to commit after consuming the first token. This is what allows reporting specific errors instead of silently failing with a backtrack.

Example: `variable_declaration ::= KW_DCL_S IDENT type_specification var_modifier* SEMI {pin=1}`:
- If we see `DCL-S`, we're already parsing a variable declaration.
- Missing `IDENT` after that → "IDENT expected, got ...".
- Missing `type_specification` → "type specification expected, got ...".
- Missing `;` at the end → "; expected, got ...".

### The `type_args` pin trick

Without a pin on `type_args`, cases like `CHAR()` went unnoticed: the parser would try `type_args`, fail upon seeing `)` where it expected `INT_LIT`, backtrack, and leave `()` dangling as `unknown_item`. The result was a `DCL-S` that finished without an error but also without consuming the parentheses.

With `{pin=1}` on `type_args`, once `(` is seen the parser commits. If what follows is not `INT_LIT`, ERROR.

### Language IDs

- `BbkLanguage.INSTANCE.getID() = "BBK"`
- Element types are generated with `new BbkElementType("...")` bound to `BbkLanguage`
- Token types are generated with `new BbkTokenType("...")` also bound

If element types were generated with `null` as the language (as happened in an early version of the scaffold), IntelliJ would NOT report `PsiErrorElement`s as red squigglies even though the PSI tree contained them.

---

## Next layer

[`layer2.md`](layer2.md) — adds `CTL-OPT`, `DCL-F`, `DCL-DS` (with `{ }` blocks for subfields).
