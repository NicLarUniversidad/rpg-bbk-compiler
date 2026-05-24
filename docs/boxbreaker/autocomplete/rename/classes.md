# Rename Refactor — Classes

**Status:** partial today (`Shift+F6` opens, rewrite is no-op) — design for the full implementation
**Scope:** every class to create and every method to modify so Rename actually rewrites the file
**Companion:** [`theory.md`](theory.md)
**Prerequisites:** Reference + Scope and Stubs + Index landed and verified

---

## 1. New classes

All under `plugin-bbk/src/main/java/com/larena/boxbreaker/plugin/bbk/`.

### 1.1 PSI factory — the core piece

| Class | Responsibility |
|---|---|
| `psi/factory/BbkElementFactory.java` | The "PSI factory". Takes text, returns a PSI node ready to insert into the tree. Exposes `createIdentifier(Project, String name) → PsiElement` — creates an IDENT leaf with the given text by parsing a mini-snippet (`DCL-S <name> INT(10);`) and pulling out the first IDENT. This is the only correct way to synthesize PSI nodes in IntelliJ; without it, Rename cannot rewrite, nor can future quick-fixes or intentions. |

### 1.2 (Optional) Validation

| Class | Responsibility |
|---|---|
| `refactoring/BbkRenameInputValidator.java` | Implements `RenameInputValidator`. IntelliJ invokes it after the user types the new name, **before** applying. Maintains a blacklist of BBK keywords (`value`, `const`, `if`, `else`, every `DCL-*`, every primitive type, every file-spec modifier), validates the new name against the BBK IDENT regex (`[a-zA-Z_][a-zA-Z0-9_]*`), and may reject names containing `-` (collides with hyphenated keywords like `DCL-S`). Rejection shows inline in the Rename dialog and blocks OK. |
| `refactoring/BbkReservedWords.java` | Immutable case-insensitive set of every BBK keyword. Consumed by the validator. Worth keeping standalone so eventual inspections ("identifier shadows a keyword") can share it. |

### 1.3 (Optional) Marker for future refactorings

| Class | Responsibility |
|---|---|
| `refactoring/BbkRefactoringSupportProvider.java` | Implements `RefactoringSupportProvider`. Today not strictly required for Rename (the dialog opens already because declarations are `PsiNamedElement`), but enables other refactors (Extract, Inline, Safe Delete) if added later. Around 10 lines; defer until those refactors are needed. |

---

## 2. Modified classes

### 2.1 `BbkNamedElementMixin.setName(String)` — non-stub-backed named elements

For inline parameters and subroutines. Today returns `this` (no-op). Becomes:

```java
public PsiElement setName(String name) {
    PsiElement newId = BbkElementFactory.createIdentifier(getProject(), name);
    PsiElement oldId = getNameIdentifier();
    if (oldId != null) oldId.replace(newId);
    return this;
}
```

Updates the IDENT of the **declaration itself**.

### 2.2 `BbkStubBasedNamedElementMixin.setName(String)` — the 7 stub-backed declarations

Same change as above, for DCL-S, DCL-C, DCL-DS, DS subfield, DCL-PR, DCL-PROC, DCL-F.

### 2.3 `reference/BbkIdentReference.handleElementRename(String)`

Today inherits the default from `PsiReferenceBase`, which fails silently because there is no factory in place. Becomes:

```java
public PsiElement handleElementRename(String newName) {
    PsiElement newId = BbkElementFactory.createIdentifier(getElement().getProject(), newName);
    // Element is the composite (e.g., BbkPrimary). Replace the IDENT child sitting
    // within getRangeInElement().
    PsiElement oldId = findIdentInRange(getElement(), getRangeInElement());
    if (oldId != null) oldId.replace(newId);
    return getElement();
}
```

Updates the IDENT of **each use-site**.

### 2.4 Same override on the other three references

- `reference/BbkMemberReference.handleElementRename(...)` — IDENT after the `.`
- `reference/BbkSubroutineReference.handleElementRename(...)` — IDENT after `exsr`
- `reference/BbkTypeReference.handleElementRename(...)` — IDENT inside `LIKEDS(...)` / `LIKE(...)` / `LIKEREC(...)`

All follow the same pattern.

### 2.5 `META-INF/plugin.xml` — register the validator (if implemented)

```xml
<renameInputValidator
    implementation="com.larena.boxbreaker.plugin.bbk.refactoring.BbkRenameInputValidator"/>
```

And `<lang.refactoringSupport>` if `BbkRefactoringSupportProvider` is added.

---

## 3. Files NOT touched

| File | Why |
|---|---|
| `BBK.bnf`, `BBK.flex` | Parser / lexer unchanged |
| `scope/*` | Scope already enumerates everything Rename needs |
| `stub/*`, `index/*` | Cross-file enumeration is already in place |
| `BbkReferenceContributor` | Reference registration unchanged |
| `BbkParserDefinition`, `BbkLanguage`, `BbkFileType` | Stable |
| Live templates, brace matcher, smart typing, completion providers | Unrelated |

---

## 4. Headcount summary

| Category | Count |
|---|---|
| New mandatory classes | **1** (`BbkElementFactory`) |
| New optional classes | **3** (`BbkRenameInputValidator`, `BbkRefactoringSupportProvider`, `BbkReservedWords`) |
| Modified methods | **6** (2 × `setName` + 4 × `handleElementRename`) |
| XML changes | **1** (`plugin.xml` — only if the validator is added) |

**Minimum:** 1 new class + 6 modified methods → Rename works.
**With validation:** +3 classes → Rename works and rejects bad names early.

**Estimated effort:** 2–3 days total (see [`theory.md`](theory.md) §11).

---

## 5. Order of implementation

1. **`BbkElementFactory`** — plus a test that `createIdentifier(project, "foo")` returns an IDENT leaf with text "foo".
2. **Update both `setName` methods** — plus a test that `decl.setName("nuevo")` changes the IDENT in the file.
3. **Update `BbkIdentReference.handleElementRename`** — plus an integration test for intra-file rename.
4. **Update the other three references** — plus tests for member access, `exsr`, and `LIKEDS`.
5. **Cross-file test** — rename a procedure in `common-procs.bbk` and verify `main.bbk` updates automatically.
6. **`BbkRenameInputValidator`** + `BbkReservedWords`** — plus a test that rejects renaming to `value`, `if`, etc.

Each step is an independent verifiable commit. If something breaks, you know exactly where.

---

## 6. Open decisions

| # | Topic | Question |
|---|---|---|
| 1 | Inline rename vs dialog | Keep IntelliJ's default. No override needed. |
| 2 | Strict keyword validation | Yes — implement `RenameInputValidator`. |
| 3 | Case sensitivity in conflicts | BBK is case-insensitive: `customer` and `CUSTOMER` are the same symbol. Reuse `equalsIgnoreCase` everywhere. |
| 4 | LIKEDS / LIKE / LIKEREC are followed automatically | Yes — `BbkTypeReference` already points to the same declaration. ✅ |
| 5 | File ops (`read fileName`, etc.) follow DCL-F rename | Yes — `BbkIdentReference` resolves to the file declaration. ✅ |
| 6 | Cross-file procedure name collision | IntelliJ warns automatically when the new name already exists in scope. No extra code. |
| 7 | Renaming the same name in different files (where they are unrelated) | We rely on `ReferencesSearch.search(declaration)`: it only returns references that resolve to the chosen declaration, so unrelated same-named symbols in other files are not touched. ✅ |

---

## 7. What is NOT covered

- Rename of `.bbk` files (IntelliJ handles it at the filesystem level)
- Rename of "symbols in strings" (refactor-in-string-literals seen in some Spring/Angular plugins)
- Smart naming suggestions during rename (`isFoo → wasFoo` style auto-rename)
- Inline / Extract / Safe Delete refactorings (require `RefactoringSupportProvider` plus dedicated processors)

---

## 8. Related documents

- [`theory.md`](theory.md) — theory and motivation
- [`../functionalities.md`](../functionalities.md) — full feature map
- [`../reference-scope/theory.md`](../reference-scope/theory.md) — references and scope (foundation)
- [`../stubs-index/theory.md`](../stubs-index/theory.md) — cross-file enumeration (foundation)
