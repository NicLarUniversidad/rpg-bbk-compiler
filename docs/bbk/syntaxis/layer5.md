# BBK Grammar — Layer 5

**Status:** implemented and verified
**Parser source:** [`plugin-bbk/src/main/grammar/BBK.bnf`](../../../plugin-bbk/src/main/grammar/BBK.bnf)
**Lexer source:** [`plugin-bbk/src/main/grammar/BBK.flex`](../../../plugin-bbk/src/main/grammar/BBK.flex) (19 new tokens added in this layer)
**Prerequisites:** [`layer1.md`](layer1.md), [`layer2.md`](layer2.md), [`layer3.md`](layer3.md), [`layer4.md`](layer4.md)
**Test files:** [`10-files.bbk`](../../../tests/boxbreaker/examples/10-files.bbk) (valid — uses file ops and monitor), [`bad-14-bad-file-ops-and-subroutines.bbk`](../../../tests/boxbreaker/examples/bad/bad-14-bad-file-ops-and-subroutines.bbk) (errors)

---

## Scope reframing

In `layer4.md` L5 had been left vague ("refinements") because nearly everything had been pushed into L4. **Layer 5 was redirected** to complete what still fell through inside procedure bodies: file ops, subroutines and CALLP. Top-level directives (`PRE-*`) move to Layer 6.

## Scope

| Construct | Form | Example |
|---|---|---|
| File ops (access) | `read file [ds];` `chain key file [ds];` `setll key file;` `reade key file [ds];` etc. | `chain custId customers customerRec;` |
| File ops (write) | `write target [ds];` `update target [ds];` `delete [key] file;` | `write orders orderRec;` |
| File ops (lifecycle) | `open file;` `close file;` `unlock file;` `exfmt format [ds];` | `open report;` |
| Subroutine definition | `BEGSR name; ... ENDSR [name];` | See below |
| Subroutine call | `EXSR name;` | `EXSR validate;` |
| Exit subroutine | `LEAVESR;` | `LEAVESR;` |
| Explicit CALLP | `CALLP procName(args);` | `CALLP CUSTPROG(custId, status);` |

**Still not covered:**
- `PRE-*` directives — **Layer 6**
- `%X(...)` BIFs with their special syntax — Layer 6 or a future lexer improvement

---

## New tokens (19)

Layer 5 requires adding new JFlex lexer tokens that did not exist before:

```
// File operations (14)
KW_READ      "read"
KW_READE     "reade"
KW_READP     "readp"
KW_READPE    "readpe"
KW_CHAIN     "chain"
KW_SETLL     "setll"
KW_SETGT     "setgt"
KW_WRITE     "write"
KW_UPDATE    "update"
KW_DELETE    "delete"
KW_UNLOCK    "unlock"
KW_OPEN      "open"
KW_CLOSE     "close"
KW_EXFMT     "exfmt"

// Subroutines (4)
KW_BEGSR     "BEGSR"
KW_ENDSR     "ENDSR"
KW_EXSR      "EXSR"
KW_LEAVESR   "LEAVESR"

// Call as statement (1)
KW_CALLP     "CALLP"
```

**Trade-off:** these names are now reserved keywords. If a user had a variable named `read`, `chain`, `write`, etc., they can't anymore. Acceptable because they are semantically loaded names in RPG.

Convention: file ops in lowercase (C style: `read`, `write`), subroutines in uppercase (RPG style: `BEGSR`, `EXSR`). Remember that **BBK is case-insensitive** (`READ` = `read` = `Read`), but the visual convention aids reading.

---

## BNF productions

### File operation statements

```bnf
file_op_statement ::= read_op | reade_op | readp_op | readpe_op
                    | chain_op | setll_op | setgt_op
                    | write_op | update_op | delete_op | unlock_op
                    | open_op | close_op | exfmt_op

// Sequential / random access reads
read_op    ::= KW_READ    IDENT IDENT? SEMI {pin=1}                // read file [ds]
readp_op   ::= KW_READP   IDENT IDENT? SEMI {pin=1}                // read prior
reade_op   ::= KW_READE   expression IDENT IDENT? SEMI {pin=1}     // reade key file [ds]
readpe_op  ::= KW_READPE  expression IDENT IDENT? SEMI {pin=1}     // read equal prior

// Random access by key
chain_op   ::= KW_CHAIN   expression IDENT IDENT? SEMI {pin=1}     // chain key file [ds]
setll_op   ::= KW_SETLL   expression IDENT SEMI {pin=1}            // setll key file
setgt_op   ::= KW_SETGT   expression IDENT SEMI {pin=1}            // setgt key file

// Updates / inserts
write_op   ::= KW_WRITE   IDENT IDENT? SEMI {pin=1}                // write format_or_file [ds]
update_op  ::= KW_UPDATE  IDENT IDENT? SEMI {pin=1}                // update format_or_file [ds]
delete_op  ::= KW_DELETE  expression? IDENT SEMI {pin=1}           // delete [key] file

// File lifecycle
unlock_op  ::= KW_UNLOCK  IDENT SEMI {pin=1}
open_op    ::= KW_OPEN    IDENT SEMI {pin=1}
close_op   ::= KW_CLOSE   IDENT SEMI {pin=1}
exfmt_op   ::= KW_EXFMT   IDENT IDENT? SEMI {pin=1}                // exfmt format [ds]
```

**Design notes:**
- **`expression` for keys** instead of `IDENT`: allows literals, future BIFs (`%KDS(...)`), arithmetic expressions, etc. as keys. `IDENT IDENT?` for file / result DS names.
- **`IDENT?` for optional result DS**: for `chain key file;` (reads into the program's global variables) or `chain key file ds;` (reads into the given DS).
- **`delete expression? IDENT`**: the key is optional because `DELETE` can also operate on the current record (no key) or with an explicit key.
- **Pin after the keyword** in every op: once `read`/`chain`/etc. is seen, the parser commits and reports specific errors.

### Subroutines

```bnf
subroutine_definition ::= KW_BEGSR IDENT SEMI sr_item* KW_ENDSR IDENT? SEMI {pin=1}

sr_item ::= variable_declaration
          | constant_declaration
          | data_structure_declaration
          | statement
          | unknown_sr_item

// SR body fallback: stop at ENDSR (closes this SR), RBRACE (parent block), or EOF.
private unknown_sr_item ::= !KW_ENDSR !RBRACE !<<eof>> any_token

exsr_statement    ::= KW_EXSR IDENT SEMI {pin=1}
leavesr_statement ::= KW_LEAVESR SEMI {pin=1}
```

**Differences with DCL-PROC:**
- **No braces** `{ }`. Delimited by `BEGSR ... ENDSR`. This preserves the historical RPG syntax.
- The name repeated after ENDSR is optional: `BEGSR mySR; ... ENDSR mySR;` or just `ENDSR;`. Improves clarity but is not mandatory.
- A subroutine is a `block_item` (lives inside a procedure body), not a `top_level_item`.

**The `unknown_sr_item` trick:**
- `!KW_ENDSR` — to stop before ENDSR closes the SR
- `!RBRACE` — so that a missing ENDSR doesn't consume the parent procedure's `}`
- `!<<eof>>` — to stop at EOF

This triple negation guarantees that a poorly closed BEGSR yields a clear error ("ENDSR expected") and does not swallow the rest of the file.

### CALLP (explicit call-as-statement)

```bnf
callp_statement ::= KW_CALLP postfix_expression SEMI {pin=1}
```

**Form:** `CALLP procName(args);`

CALLP is optional: a call such as `myProc(a, b);` also works via `expression_statement` (the expression is the call, terminated by `;`). CALLP exists for cases where:
- You want to be explicit that it's a call (readability)
- There is syntactic ambiguity (rare in BBK because calls have `()`)
- Compatibility with RPG where CALLP was required

### Integration with statement and block_item

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
            | file_op_statement       // L5
            | exsr_statement          // L5
            | leavesr_statement       // L5
            | callp_statement         // L5
            | expression_statement

block_item ::= variable_declaration
             | constant_declaration
             | data_structure_declaration
             | subroutine_definition  // L5
             | statement
             | unknown_block_item
```

**Alternative order in `statement`:** file ops and EXSR/LEAVESR/CALLP go **before** expression_statement. This is important because `read customers;` before L5 (when KW_READ did not exist) fell through as an expression_statement with the false error "SEMI expected". Now it matches as `read_op` first. ✓

---

## Examples

### Valid — `10-files.bbk`

```bbk
DCL-F customers DISK USAGE(*INPUT) KEYED EXTNAME("CUSTOMER");
DCL-F orders    DISK USAGE(*INPUT:*OUTPUT:*UPDATE) KEYED EXTNAME("ORDER");
DCL-F report    PRINTER USAGE(*OUTPUT) USROPN;

DCL-DS customerRec EXTNAME("CUSTOMER") QUALIFIED;

DCL-DS orderRec QUALIFIED {
  orderId    INT(10);
  customerId INT(10);
  amount     PACKED(11:2);
  orderDate  DATE;
}

DCL-PR processOrder(rec LIKEDS(orderRec) CONST);

DCL-PROC filesDemo {
  DCL-S targetCustomerId INT(10) INZ(12345);
  DCL-S customerFound    BOOL;

  monitor {
    open report;

    chain targetCustomerId customers customerRec;
    customerFound = (status() == 0);

    if (customerFound) {
      print("Customer: " + trim(customerRec.name));

      setll targetCustomerId orders;
      reade targetCustomerId orders orderRec;
      while (status() == 0) {
        processOrder(orderRec);
        reade targetCustomerId orders orderRec;
      }
    } else {
      print("Customer not found");
    }

    orderRec.orderId    = 99999;
    orderRec.customerId = targetCustomerId;
    orderRec.amount     = 150.00d;
    orderRec.orderDate  = currentDate();
    write orders orderRec;

  } on-error (404, 405) {
    print("File access error: code " + char(status()));
  } on-exit {
    close report;
  }
}
```

Layer 5 fully recognizes and structures: file ops (`open`, `chain`, `setll`, `reade`, `write`, `close`), control flow (`if`, `while`), expressions (`status() == 0`), assignment with member access (`orderRec.orderId = 99999`), call-as-statement (`print(...)`, `processOrder(orderRec)`).

### Example with subroutines

```bbk
DCL-PROC oldSchoolStyle {
  DCL-S counter INT(10) INZ(0);

  // Subroutine call
  EXSR validate;
  EXSR process;

  return;

  // Subroutine definitions
  BEGSR validate;
    if (counter < 0) {
      LEAVESR;   // exit the SR early
    }
    counter += 1;
  ENDSR validate;

  BEGSR process;
    DCL-S temp INT(10);  // local to the SR (actually proc-scoped, but the idea is here)
    temp = counter * 2;
    print(char(temp));
  ENDSR;   // ENDSR without repeated name is also valid
}
```

### Errors detected — `bad-14-bad-file-ops-and-subroutines.bbk`

| Category | Cases |
|---|---|
| File ops without minimum args | `read;`, `chain;`, `setll;`, `write;`, `open;` |
| File ops without `;` | `read customers customerRec`, `chain key`, `delete` |
| Partial file ops | `setll *START;` (no file), `reade ;` (no key), `chain ( orders;` (parens) |
| Subroutine def without name | `BEGSR;` |
| Subroutine without `;` after the name | `BEGSR mySR` |
| Unclosed subroutine | `BEGSR mySR; ... ` (no ENDSR) |
| ENDSR without `;` | `ENDSR otherSR` (missing `;`) |
| EXSR without name / without `;` | `EXSR;`, `EXSR mySR` |
| LEAVESR without `;` | `LEAVESR` |
| CALLP without call | `CALLP;` |
| CALLP without parens or without `;` | `CALLP myProc`, `CALLP myProc(a, b)` |

---

## Implementation notes

### Keyword case convention (reminder)

BBK is case-insensitive at the lexer level (`%ignorecase` in BBK.flex). So `READ` = `read` = `Read` = `rEaD` from the parser's point of view. Suggested visual convention:

- **File ops in lowercase** (`read`, `chain`, `write`) — closer to C, flows better when mixing with expressions.
- **Declarations in uppercase with hyphens** (`DCL-S`, `BEGSR`, `ENDSR`) — closer to RPG, more recognizable as "program structure".

But technically any capitalization is valid.

### Why `expression` (not `IDENT`) for keys

```bnf
chain_op ::= KW_CHAIN expression IDENT IDENT? SEMI {pin=1}
```

The first arg (the key) is `expression`, not `IDENT`. It allows:
- `chain custId customers ds;` (key = simple IDENT)
- `chain *START orders;` (key = figurative STAR_IDENT)
- `chain 12345 customers;` (key = literal)
- Future: `chain %KDS(myKeysDS) customers;` when BIFs are added

But `expression` IS greedy. For `chain x customers;`, the parser:
- expression: `x` (primary IDENT). Postfix? No LPAREN/etc. follows.
- Walks up additive, etc. Nothing matches. Expression ends with just `x`.
- IDENT `customers` ✓.
- SEMI ✓.

OK, it works. For `chain x + y customers;` (computed key):
- expression: `x` then `+` then `y` (additive expression) = `x + y`.
- IDENT `customers` ✓.

Also works.

### Subroutines as a sub-block inside procedures

Subroutines are legacy RPG. BBK supports them but the modern convention is to use sub-procedures (DCL-PROC) instead. Key difference:

| Aspect | Subroutine (BEGSR/ENDSR) | Sub-procedure (DCL-PROC) |
|---|---|---|
| Scope | Shares variables with the parent proc | Has its own scope |
| Parameters | None | Yes, typed |
| Return value | None | Yes, optional |
| Call | `EXSR name;` | `name(args);` |
| Modern recommendation | Avoid for new code | Prefer |

BBK supports subroutines for compatibility with legacy RPG. When the frontend translates them, they could be converted into auto-generated procedures with explicit scope.

### Edge case: BEGSR before the statement that calls it

In RPG (and BBK by extension), order is free:
```bbk
DCL-PROC main {
  EXSR helper;       // call before the definition
  return;

  BEGSR helper;
    print("hello");
  ENDSR;
}
```

The L5 parser accepts this without issue — the SR can be defined anywhere in the body. Validation of "EXSR refers to an existing SR" is semantic, not syntactic.

---

## Next layer

`layer6.md` — **preprocessor directives** (`PRE-IF`, `PRE-ELSEIF`, `PRE-ELSE`, `PRE-ENDIF`, `PRE-DEFINE`, `PRE-UNDEFINE`, `PRE-INCLUDE`, `PRE-EOF`). They have their own sub-grammar and are ideally processed before the main parser, but for simplicity we can integrate them as top-level statements with special syntax.

Eventually also: BIFs with `%X(args)` syntax, which require a new token for `%IDENT`.
