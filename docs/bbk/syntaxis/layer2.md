# BBK Grammar — Layer 2

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Prerequisite:** [`layer1.md`](layer1.md)
**Test files:** [`tests/boxbreaker/examples/`](../../../tests/boxbreaker/examples/) (valid) and [`bad-09`](../../../tests/boxbreaker/examples/bad/bad-09-bad-ctl-opt.bbk), [`bad-10`](../../../tests/boxbreaker/examples/bad/bad-10-bad-file-decl.bbk), [`bad-11`](../../../tests/boxbreaker/examples/bad/bad-11-bad-data-structures.bbk) (intentional errors)

---

## Scope

Layer 2 completes the set of **module-level declarations** without yet entering procedures, statements or expressions. It adds three constructs:

| Construct | Form | Example |
|---|---|---|
| `CTL-OPT` | Module directive | `CTL-OPT MAIN(helloMain) NOMAIN DEBUG;` |
| `DCL-F` | File declaration | `DCL-F customers DISK USAGE(*INPUT) KEYED EXTNAME("CUSTOMER");` |
| `DCL-DS` | Data structure | `DCL-DS person QUALIFIED { id INT(10); name CHAR(50); }` |

**Key novelty:** introduces the **`{ }` block syntax** through `DCL-DS { subfield; subfield; }`. This pattern is reused in later layers (`DCL-PROC { body }`, `if (cond) { ... }`, etc.).

**Still not covered:**
- `DCL-PR`, `DCL-PROC` — Layer 3
- Statements, expressions, file ops, `PRE-*` directives — Layers 4-6

---

## Updated top-level

```bnf
top_level_item ::= variable_declaration       // L1
                 | constant_declaration        // L1
                 | ctl_opt_statement           // L2
                 | data_structure_declaration  // L2
                 | file_declaration            // L2
                 | unknown_item
```

---

## BNF productions

### CTL-OPT statement

```bnf
ctl_opt_statement ::= KW_CTL_OPT ctl_opt_keyword* SEMI {pin=1}

ctl_opt_keyword ::= IDENT ctl_opt_args?

ctl_opt_args ::= LPAREN ctl_opt_arg (COLON ctl_opt_arg)* RPAREN {pin=1}

private ctl_opt_arg ::= literal | KW_TRUE | KW_FALSE | KW_NULL | STAR_IDENT | IDENT
```

**Form:** `CTL-OPT keyword keyword(arg) keyword(a:b:c) ... ;`

Keywords (`MAIN`, `NOMAIN`, `DFTACTGRP`, `DEBUG`, etc.) are **not dedicated tokens** — they are parsed as `IDENT` and the semantics are interpreted at a later stage. This prevents the token list from exploding with every IBM option.

**Accepted arguments:**
- Literals (`INT_LIT`, `STR_LIT`, etc.)
- `true` / `false` / `null`
- `STAR_IDENT` (e.g. `*NO`, `*NEW`, `*CALLER`)
- `IDENT` (e.g. procedure names, library names)

Multi-args are separated by `:` (not `,`): `OPTION(*SRCSTMT:*NODEBUGIO)`.

### File declaration

```bnf
file_declaration ::= KW_DCL_F IDENT f_keyword+ SEMI {pin=1}

f_keyword ::= simple_f_keyword
            | usage_f_keyword
            | extname_f_keyword
            | extfile_f_keyword
            | prefix_f_keyword
            | rename_f_keyword
            | indds_f_keyword
            | infds_f_keyword

simple_f_keyword  ::= KW_KEYED | KW_USROPN | KW_DISK | KW_PRINTER | KW_WORKSTN | KW_SEQ
usage_f_keyword   ::= KW_USAGE   LPAREN STAR_IDENT (COLON STAR_IDENT)* RPAREN {pin=1}
extname_f_keyword ::= KW_EXTNAME LPAREN STR_LIT RPAREN {pin=1}
extfile_f_keyword ::= KW_EXTFILE LPAREN STR_LIT RPAREN {pin=1}
prefix_f_keyword  ::= KW_PREFIX  LPAREN IDENT (COLON INT_LIT)? RPAREN {pin=1}
rename_f_keyword  ::= KW_RENAME  LPAREN IDENT COLON IDENT RPAREN {pin=1}
indds_f_keyword   ::= KW_INDDS   LPAREN IDENT RPAREN {pin=1}
infds_f_keyword   ::= KW_INFDS   LPAREN IDENT RPAREN {pin=1}
```

**Form:** `DCL-F filename keyword keyword(args) ... ;`

Unlike `CTL-OPT`, the DCL-F keywords **are dedicated tokens** (`KW_USAGE`, `KW_EXTNAME`, etc.) because each has its own grammar for its arguments. This yields more precise error reporting.

**Argument type restrictions:**
- `USAGE(*X:*Y:...)` — only `STAR_IDENT`, not `IDENT`
- `EXTNAME("file")` / `EXTFILE("file")` — only `STR_LIT`
- `PREFIX(prefix:n)` — `IDENT` optionally followed by `:INT_LIT`
- `RENAME(old:new)` — two `IDENT`s separated by `:`
- `INDDS(name)` / `INFDS(name)` — a single `IDENT`

`f_keyword+` (one or more) — a `DCL-F` always needs at least one keyword. `DCL-F customers;` raises an error.

### Data structure declaration

```bnf
data_structure_declaration ::= KW_DCL_DS IDENT ds_modifier* ds_tail {pin=1}

private ds_tail ::= ds_body | SEMI

ds_body ::= LBRACE ds_subfield* RBRACE {pin=1}

ds_subfield ::= IDENT type_specification var_modifier* SEMI {pin=1}
```

**Two forms:**

1. **Inline with body** — declares the structure with its subfields:
   ```bbk
   DCL-DS person QUALIFIED {
     id     INT(10);
     name   CHAR(50);
     active BOOL;
   }
   ```

2. **No body, just `;`** — used with `LIKEDS` or `TEMPLATE` to reuse structures:
   ```bbk
   DCL-DS homeAddress LIKEDS(addressTemplate);
   DCL-DS customerRec EXTNAME("CUSTOMER") QUALIFIED;
   ```

### DS modifiers

```bnf
ds_modifier ::= qualified_modifier      // reused from var_modifier
              | template_modifier        // L2
              | align_modifier           // L2
              | dim_modifier             // reused
              | based_modifier           // reused
              | inz_modifier             // reused
              | extname_ds_modifier      // L2
              | likeds_ds_modifier       // L2
              | likerec_ds_modifier      // L2
              | infds_ds_modifier        // L2

template_modifier     ::= KW_TEMPLATE
align_modifier        ::= KW_ALIGN
extname_ds_modifier   ::= KW_EXTNAME LPAREN STR_LIT RPAREN {pin=1}
likeds_ds_modifier    ::= KW_LIKEDS  LPAREN IDENT RPAREN {pin=1}
likerec_ds_modifier   ::= KW_LIKEREC LPAREN IDENT (COLON IDENT)? RPAREN {pin=1}
infds_ds_modifier     ::= KW_INFDS   LPAREN IDENT RPAREN {pin=1}
```

**DS-specific modifiers:**
- `TEMPLATE` — the DS allocates no storage, it only defines the layout for reuse with `LIKEDS`
- `ALIGN` — aligns subfields on natural boundaries
- `EXTNAME("FILE")` — takes subfields from the external schema of the file
- `LIKEDS(other)` — copies the layout of another DS
- `LIKEREC(rec)` — copies the layout of a file's record format
- `INFDS(name)` — declares this DS to be the file info DS of file `name`

**Modifiers reused from `var_modifier`** (Layer 1):
- `QUALIFIED` — forces access via `ds.subfield` instead of direct variables
- `DIM(n)` — array of DSs
- `BASED(ptr)` — DS based on a pointer
- `INZ` — initializes subfields to default

### Subfields

```bnf
ds_subfield ::= IDENT type_specification var_modifier* SEMI {pin=1}
```

Identical to `variable_declaration` but **without** the `DCL-S` prefix. Reuse all Layer 1 modifiers (`INZ`, `OVERLAY`, `POS`, etc.).

Example with `OVERLAY` (memory union):
```bbk
DCL-DS dateRecord QUALIFIED {
  fullDate CHAR(8);
  year     CHAR(4) OVERLAY(fullDate:1);
  month    CHAR(2) OVERLAY(fullDate:5);
  day      CHAR(2) OVERLAY(fullDate:7);
}
```

---

## Examples

### Valid code

```bbk
CTL-OPT MAIN(helloMain) NOMAIN DFTACTGRP(*NO) DEBUG;

DCL-F customers DISK USAGE(*INPUT) KEYED EXTNAME("CUSTOMER");
DCL-F orders    DISK USAGE(*INPUT:*OUTPUT:*UPDATE) KEYED EXTNAME("ORDER");
DCL-F report    PRINTER USAGE(*OUTPUT) USROPN;

DCL-DS person QUALIFIED {
  firstName VARCHAR(50);
  lastName  VARCHAR(50);
  age       INT(10);
  salary    PACKED(9:2);
}

DCL-DS addressTemplate TEMPLATE {
  street VARCHAR(100);
  city   VARCHAR(50);
  zip    CHAR(10);
}

DCL-DS homeAddress LIKEDS(addressTemplate);
DCL-DS workAddress LIKEDS(addressTemplate);

DCL-DS employees QUALIFIED DIM(1000) {
  id     INT(10);
  name   VARCHAR(50);
  active BOOL;
}

DCL-DS customerRec EXTNAME("CUSTOMER") QUALIFIED;
```

See [`01-hello.bbk`](../../../tests/boxbreaker/examples/01-hello.bbk), [`06-data-structures.bbk`](../../../tests/boxbreaker/examples/06-data-structures.bbk), [`10-files.bbk`](../../../tests/boxbreaker/examples/10-files.bbk).

### Errors detected

| File | Error type |
|---|---|
| `bad-09-bad-ctl-opt.bbk` | CTL-OPT with unterminated args, empty args, trailing colons, missing `;` |
| `bad-10-bad-file-decl.bbk` | DCL-F without name, no keywords, USAGE/EXTNAME with wrong arg type |
| `bad-11-bad-data-structures.bbk` | DCL-DS without name/body, bad LIKEDS, subfields without type/`;`, unclosed body |

---

## Implementation notes

### The block pattern (`LBRACE ... RBRACE`)

Layer 2 introduces the first language block in `ds_body`:

```bnf
ds_body ::= LBRACE ds_subfield* RBRACE {pin=1}
```

The `{pin=1}` after `LBRACE` is key: once `{` appears, the parser commits to closing it with `}`. If `}` is missing (unterminated body), it reports the error clearly.

This same pattern is reused for:
- `procedure_body` in Layer 3
- `if`/`else`/`while` blocks in Layer 4
- `select { when { ... } }` in Layer 4

### Reusing modifiers between `DCL-S` and `DCL-DS`

`var_modifier` (defined in Layer 1) is reused for `DCL-DS` subfields. That means a subfield can carry `INZ`, `OVERLAY`, `DIM`, etc. — the same options as a standalone variable.

DS-exclusive modifiers (`TEMPLATE`, `EXTNAME`, `LIKEDS`, `LIKEREC`, `INFDS`) go in `ds_modifier`, separately.

There is intentional duplication in some cases:
- `KW_EXTNAME(...)` appears as `extname_f_keyword` in DCL-F and as `extname_ds_modifier` in DCL-DS — same syntax but different PSI elements.
- `KW_INFDS(...)` likewise as `infds_f_keyword` and `infds_ds_modifier`.

This separation later eases semantic analysis (knowing whether an EXTNAME is in F or DS context without ambiguity).

### CTL-OPT keywords are IDENT, not tokens

Unlike DCL-F where each keyword (`USAGE`, `EXTNAME`, etc.) is a dedicated token, the CTL-OPT keywords (`MAIN`, `NOMAIN`, `DFTACTGRP`, `THREAD`, etc.) are parsed as generic `IDENT`s.

**Reason:** IBM adds new CTL-OPT keywords in every release. Having them as dedicated tokens would force lexer/parser updates with each change. Better to parse them as identifiers and validate in a later semantic phase against an updatable table.

**Trade-off:** typos in keywords (e.g. `DFTACTGROP` instead of `DFTACTGRP`) are not caught at the parser level — they are accepted as valid IDENT. They will be detected in semantic validation.

---

## Next layer

[`layer3.md`](layer3.md) — adds `DCL-PR` and `DCL-PROC` with parameters and bodies. It's the largest because it brings **statements and expressions** inside procedure bodies.
