# Rename Refactor — Theory

**Status:** partial (action wired, rewrite is no-op in V1) — full implementation pending
**Scope:** the IntelliJ Platform concepts behind feature #7 (Rename) of [`../functionalities.md`](../functionalities.md)
**Plugin module:** `plugin-bbk/`
**Prerequisites:** Block A, Block B, Block C — see the corresponding `theory.md` files

---

## 1. What it is

`Shift+F6` on any identifier → IntelliJ opens a dialog (or inline rename) that:

- Shows the current name and a field for the new one
- Renames **every occurrence** of the symbol (declaration + every use) across ALL files of the project, **atomically** (a single `Ctrl+Z` undoes it)

UX variants:

- **Dialog** (classic, with settings or conflicts)
- **Inline rename** (caret on the declaration → type directly on the red highlight)

---

## 2. How IntelliJ implements it

The rename engine is **derived** from the infrastructure already in place:

```
1. Caret over `customer`
2. IntelliJ resolves the target declaration (via PsiReference.resolve)
3. Prompts for the new name
4. Internally executes:
     a. ReferencesSearch.search(declaration)
        → finds EVERY intra + cross-file reference (same engine as Find Usages)
     b. For each PsiReference found:
        reference.handleElementRename(newName)
        → each reference rewrites ITS own source text
     c. For the declaration itself:
        declaration.setName(newName)
        → the declaration rewrites ITS OWN IDENT
     d. All inside a single Command (atomic, undoable)
```

Critically, IntelliJ does **not** write text to disk directly — it modifies the PSI tree, and the PSI changes are reflected back into the `Document`, which IntelliJ persists like any other edit.

---

## 3. The two APIs that must be implemented

| API | Caller | Purpose |
|---|---|---|
| `PsiNamedElement.setName(String)` | rename engine, on the declaration | rewrite the declaration's IDENT |
| `PsiReference.handleElementRename(String)` | rename engine, on each use-site | rewrite the IDENT of each use |

If both work, rename "just works" cross-file.

---

## 4. What we have today

| Component | Status |
|---|---|
| Declarations implement `PsiNameIdentifierOwner` via mixins | ✅ |
| `Shift+F6` dialog opens | ✅ (IntelliJ derives it from `PsiNamedElement`) |
| Cross-file reference enumeration | ✅ (StubIndex from the cross-file resolution work) |
| `setName(...)` on the mixins | ⚠️ **no-op** — just returns `this` |
| `handleElementRename(...)` on references | ⚠️ default inherited from `PsiReferenceBase` calls `getElement().replace(...)` — but the new element is never built because we have no factory |

**Current behaviour:** the dialog opens, you type the new name, hit OK → nothing visibly happens. IntelliJ does not corrupt anything, it just does not write.

---

## 5. What is missing — the `PsiElementFactory`

For `setName` and `handleElementRename` to rewrite, they need to **create a new PSI element** with the new name. The idiomatic IntelliJ way:

```
1. Create a synthetic BbkFile with the text "DCL-S newName INT(10);"
   via PsiFileFactory.createFileFromText()
2. Navigate to the first IDENT of the synthetic file — that is the new IDENT
3. element.getNode().replaceChild(oldIDENT.getNode(), newIDENT.getNode())
```

That is what is typically called a **`BbkElementFactory`** (or `BbkPsiFactory`). A single class with methods like:

- `createIdentifier(project, name)` → returns a PsiElement of type IDENT with the given text

Once you have this:

- `setName` on the mixins uses it to replace the IDENT child
- `handleElementRename` on references uses it to replace the IDENT at the use-site

---

## 6. Cross-file: already wired

Since `ReferencesSearch` already enumerates cross-file references (via StubIndex), once `setName` / `handleElementRename` rewrite locally, IntelliJ walks the rest of the project and applies them. **No additional code is needed for cross-file.**

---

## 7. Validation and conflicts

IntelliJ provides for free:

- **New-name validation** — checks it is a valid IDENT (no spaces, no keywords)
- **Conflict detection** — if the new name already exists in the same scope, it shows "Variable 'x' is already defined in scope" before applying
- **Preview** — shows a tree of "what will change" before confirming

For finer "valid name" validation we can optionally implement `RenameInputValidator`:

- Reject keywords (`if`, `else`, `DCL-S`, `value`, etc.)
- Reject names with invalid characters
- Allow/reject hyphenated names depending on context

---

## 8. `RenameProcessor` and advanced cases

For more sophisticated refactorings (renaming references in strings, comments, non-PSI locations), IntelliJ uses `RenamePsiElementProcessor`. **We do not need it** for BBK V1 — reference-based standard rename is enough.

---

## 9. Class map

| Class | Responsibility | Size |
|---|---|---|
| `psi/factory/BbkElementFactory.java` | `createIdentifier(project, String)`. Also useful beyond rename (quick fixes, intentions later). | ~30 lines |
| `BbkNamedElementMixin.setName()` change | Uses the factory; replaces the current element's IDENT child. | ~15 lines |
| `BbkStubBasedNamedElementMixin.setName()` change | Same. | ~15 lines |
| `reference/BbkIdentReference.handleElementRename()` | Override to use the factory and replace the IDENT at the use-site. | ~10 lines |
| Same override on `BbkMemberReference`, `BbkSubroutineReference`, `BbkTypeReference` | Same pattern. | ~10 lines each |
| (optional) `refactoring/BbkRenameInputValidator.java` | Rejects keywords and invalid names as the new name. | ~40 lines |
| (optional) `refactoring/BbkRefactoringSupportProvider.java` | Marker extension that enables extracts, etc. — not required for Rename. | ~10 lines |

Plus a `<lang.refactoringSupport>` and/or `<renameInputValidator>` registration in `plugin.xml`.

---

## 10. Open decisions

| # | Topic | Question |
|---|---|---|
| 1 | Inline rename vs dialog | IntelliJ's default picks per context. Force one? Recommendation: keep the default. |
| 2 | Strict keyword validation | Reject rename to `value`, `if`, etc.? Recommendation: yes, via `RenameInputValidator`. |
| 3 | Case sensitivity in conflicts | BBK is case-insensitive. Are `customer` and `CUSTOMER` the same symbol? Recommendation: yes (consistent with the rest of the scope). |
| 4 | Rename of `LIKEDS(customer)` when renaming the DS `customer` | Should update the IDENT inside LIKEDS too. This is automatic because `BbkTypeReference` points to the same declaration. ✅ |
| 5 | Rename of DCL-F affects `read fileName`, etc. | Same — `BbkIdentReference` resolves to the file declaration; rename updates it. ✅ |
| 6 | Conflict with cross-file procedures of the same name | If you rename `processOrder` to something that already exists in another file, IntelliJ should warn. Comes for free if scope checks are correct. |

---

## 11. Estimated effort

- `BbkElementFactory` + the two `setName` updates: **1 day**
- `handleElementRename` on the 4 references: **0.5 day**
- `RenameInputValidator` with keyword set: **0.5 day**
- Integration tests (intra + cross-file rename): **0.5 day**

**Total: ~2-3 days** for a production-quality Rename.

---

## 12. What is NOT covered

- Rename of `.bbk` files (IntelliJ already does it at the filesystem level, no plugin code needed)
- Rename of "symbols in strings" (the kind of refactor in string literals seen in some Spring/Angular plugins)
- Smart naming suggestions during rename (style `isFoo → wasFoo` automatic)

---

## 13. Related documents

- [`../functionalities.md`](../functionalities.md) — full feature map, all blocks
- [`../basic-autocomplete/theory.md`](../basic-autocomplete/theory.md) — completion infrastructure
- [`../reference-scope/theory.md`](../reference-scope/theory.md) — references and scope (foundation for rename)
- [`../stubs-index/theory.md`](../stubs-index/theory.md) — cross-file enumeration (foundation for cross-file rename)
- (future) `./classes.md` — concrete class set, BNF/plugin.xml changes, order of implementation
