# Cross-file examples — Block C

These four `.bbk` files exercise the cross-file resolution that Block C unlocks
(stub index + project-wide lookup).

## Files

| File | Declares |
|---|---|
| `common-constants.bbk` | `DCL-C MAX_RETRIES`, `DCL-C COMPANY_NAME`, `DCL-C PI`, `DCL-C IS_DEBUG`, `DCL-C DEFAULT_PORT` |
| `common-types.bbk`     | `DCL-DS customer`, `DCL-DS order`, `DCL-DS addressTemplate` (TEMPLATE) |
| `common-procs.bbk`     | `DCL-PR sendNotification` / `computeTax` / `formatCurrency`; `DCL-PROC validateCustomer` / `processOrder` / `greetCustomer` |
| `main.bbk`             | `CTL-OPT MAIN(runDemo)`; consumes everything from the other three |

## Things to verify in the sandbox IDE

Open `main.bbk` after the first index build finishes ("Indexing..." in the bottom-right).

| Action | Expected |
|---|---|
| `Ctrl+B` on `customer` (inside `LIKEDS(customer)`) | jumps to `common-types.bbk` line of `DCL-DS customer` |
| `Ctrl+B` on `processOrder` (call site) | jumps to `common-procs.bbk` line of `DCL-PROC processOrder` |
| `Ctrl+B` on `validateCustomer` | jumps to `common-procs.bbk` |
| `Ctrl+B` on `MAX_RETRIES`, `COMPANY_NAME`, `DEFAULT_PORT` | jumps to `common-constants.bbk` |
| `Ctrl+B` on `addressTemplate` | jumps to `common-types.bbk` |
| `Ctrl+B` on `localHelper` | jumps to local `DCL-PROC localHelper` in the same file |
| `currentCustomer.` (with the dot) → completion | lists `id`, `firstName`, `lastName`, `email`, `birthDate`, `isActive` |
| `Alt+F7` on `customer` in `common-types.bbk` | lists every usage in `main.bbk` AND in `common-procs.bbk` |
| `Ctrl+Alt+Shift+N` and type `process` | shows `processOrder` from `common-procs.bbk` |
| `Ctrl+Alt+Shift+N` and type `validate` | shows `validateCustomer` |
| Autocomplete inside `runDemo` after typing `comp` | suggests `computeTax`, `COMPANY_NAME` (cross-file) |

## Known V1 caveats (documented, not bugs)

- **Rename (`Shift+F6`)** opens the dialog but the text-rewrite is a no-op
  (Block C unlocks the action, but the actual rewriter is deferred).
- **Goto symbol** may take a few seconds the very first time the project is
  opened, while IntelliJ builds the stub index. Subsequent opens are instant.
