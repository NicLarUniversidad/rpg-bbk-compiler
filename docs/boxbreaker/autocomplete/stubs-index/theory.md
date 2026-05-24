# Stubs + Index — Theory

**Status:** design / not implemented
**Scope:** the IntelliJ Platform concepts that underpin Block C (cross-file resolution + project-wide indexing) of [`../functionalities.md`](../functionalities.md)
**Plugin module:** `plugin-bbk/`
**Prerequisites:** Block A and Block B — see [`../basic-autocomplete/theory.md`](../basic-autocomplete/theory.md) and [`../reference-scope/theory.md`](../reference-scope/theory.md)

---

## 1. The central question

Block B resolves identifiers **inside a single file**. Block C answers the next question:

> How do we find the declaration of `processOrder` when it lives in `procedures.bbk` and is used from `main.bbk`, **without re-parsing `procedures.bbk` every time the IDE asks**?

The naive answer — parse every file in the project on every `resolve()` — does not scale. A 100-file project with thousands of references would collapse the IDE.

IntelliJ's solution: **PSI stubs + StubIndex**.

---

## 2. PSI Stubs — the "compact" version of the PSI

A **stub** is a serialisable, minimal representation of the structurally important parts of the PSI. For BBK, the stub-backed items are:

- `DCL-S name` → stub with `{name: "currentCustomer", type: "LIKEDS(customer)"}`
- `DCL-C name` → stub with `{name: "MAX_RETRIES", value: "5"}`
- `DCL-DS name` → stub with `{name: "customer", isQualified: true, subfields: [...]}`
- `DCL-PR name` → stub with `{name: "processOrder", paramSig: "(...)", returnType: "..."}`
- `DCL-PROC name` → stub with `{name: "processOrder", paramSig: "(...)", isExported: true}`
- `DCL-F name` → stub with `{name: "customers", usage: "*INPUT"}`

**What does NOT go in stubs:** procedure bodies, expressions, statements, comments. Those only matter when the file is being edited.

Stubs are stored in IntelliJ's persistent database (`%user%/.local/share/.../caches`). When you open a file:

- If the file's content has not changed since the last indexing → IntelliJ loads the deserialised stub, no parse
- If it has changed → re-parse and update the stub

This is **orders of magnitude faster** than full parsing.

---

## 3. StubIndex — name-based lookup

On top of stubs, IntelliJ provides `StubIndex`: given a **key** (typically the name), return every stub in the project that has it.

For BBK we define one index per declaration kind:

```
BbkProcedureIndex          name → List<BbkProcedureDeclaration>
BbkPrototypeIndex          name → List<BbkPrototypeDeclaration>
BbkVariableIndex           name → List<BbkVariableDeclaration>
BbkConstantIndex           name → List<BbkConstantDeclaration>
BbkDataStructureIndex      name → List<BbkDataStructureDeclaration>
BbkFileDeclarationIndex    name → List<BbkFileDeclaration>
```

Case-insensitive, because BBK is.

The index does **not** store PSI — it stores keys plus offsets into the stub storage. When you query it, IntelliJ deserialises the matching stubs on demand.

---

## 4. The new resolution chain

`BbkIdentReference.resolve()` now does two things:

```
1. Walk the local scope (Block B)  → is there a match in this file?
2. If not                          → query StubIndex with the name
                                     → enumerate cross-file stubs
                                     → return the first matching one
```

`getVariants()` (used by autocomplete) follows the same pattern:

```
1. Visible declarations from the local scope
2. + all names in BbkProcedureIndex / BbkPrototypeIndex  (callable procedures)
3. + all names in BbkConstantIndex                       (global constants)
4. + selectively other indexes
```

---

## 5. What changes in the BNF

To make a PSI element stub-backed, it has to be annotated in `BBK.bnf`:

```bnf
procedure_declaration ::= KW_DCL_PROC IDENT inline_param_list? return_type? proc_modifier* block_statement {
  pin=1
  mixin="...BbkNamedElementMixin"
  implements="com.intellij.psi.PsiNamedElement"
  // ← NEW:
  stubClass="...stub.BbkProcedureDeclarationStub"
  elementTypeFactory="...stub.BbkStubElementTypeFactory.factory"
}
```

Grammar-Kit regenerates the PSI impl class so it extends `StubBasedPsiElementBase<BbkProcedureDeclarationStub>` instead of `ASTWrapperPsiElement`. That gives it two constructors — one from an AST node (when full PSI is available) and one from a stub (when only the compact version is loaded).

Also: `BbkParserDefinition` switches from a plain `IFileElementType` to an `IStubFileElementType` with explicit versioning. Bumping the version invalidates the persistent stub storage when the schema changes.

---

## 6. What comes for free with Block C

| Feature | How it's derived |
|---|---|
| **Cross-file Go to declaration** | `Ctrl+B` on `processOrder` → query `BbkProcedureIndex` → open the file where it lives |
| **Cross-file Find usages** | IntelliJ walks references project-wide using indexes, no re-parse |
| **Cross-file Rename** (full #7) | combines cross-file find-usages with Block B's intra-file rename |
| **Goto symbol** (`Ctrl+Alt+Shift+N`) | IntelliJ's standard "find symbol" UI enumerates every registered index → typing `proce` lists every procedure in the project |
| **Cross-file autocomplete** | references' `getVariants()` includes cross-file stubs |
| **Fast project open** | no full re-parse — the IDE reads cached stubs |

Five user-visible features plus a performance jump, all from one block.

---

## 7. Cache and invalidation

- IntelliJ versions stubs via `IStubFileElementType.getStubVersion()`.
- Every time the stub schema changes (a new field, a renamed property), **bump the version number**.
- If the version changes → IntelliJ invalidates the entire persistent stub store and re-indexes.
- If a file's content changes → IntelliJ regenerates only that file's stub.

The single real trap with stubs: if you forget to bump the version when the schema changes, IntelliJ will load deserialised stubs with the old reader → silent corruption.

---

## 8. Interaction with Block B

`BbkScopeWalker` and the reference classes **are not rewritten**. They are extended:

- `BbkModuleScope.getDeclarations()` can optionally fold in stub-index results when asked for cross-file visibility.
- `BbkIdentReference.resolveUncached()` adds a fallback to the index when the local scope does not resolve.
- `BbkScopeCompletionProvider.addCompletions()` adds index-sourced suggestions.

Block B remains the base. Block C **adds a cross-file layer on top**.

---

## 9. What Block C does NOT cover

| Feature pending | Block |
|---|---|
| Smart completion type-aware (#9) | D |
| Type-aware inspections (#10) | D |
| Parameter info hints (#11) | D |
| Quick documentation with BIF docs (#12) | E |
| Real (non-no-op) rename rewrite | a rename-specifics refactor combined with the index |

---

## 10. Pitfalls to watch for

1. **Stubs must be immutable.** Once created, they are not modified. Re-parsing generates a new stub; it does not edit the existing one.
2. **Case-insensitive names.** When indexing by name, normalise to lowercase (BBK is case-insensitive).
3. **Mandatory versioning.** Every stub schema change requires a version bump.
4. **Index build performance.** The index builds in the background (it can take minutes the first time on a large project). Keys must be designed to be efficiently queryable — only the name, not full signatures.
5. **PSI / stub coexistence.** When IntelliJ instantiates a PSI from a stub, certain methods (those that need the AST) lazy-load the full PSI. Call `getStub()` when available to avoid forcing that load needlessly.

---

## 11. Minimal class set for Block C

Putting the theory into a concrete file map:

```
plugin-bbk/src/main/java/com/larena/boxbreaker/plugin/bbk/
├── stub/
│   ├── BbkStubElementTypes.java                    — central registry of every stub element type
│   ├── BbkStubElementTypeFactory.java              — factory referenced from BBK.bnf
│   ├── BbkFileStub.java                            — root stub for the whole file
│   ├── BbkVariableDeclarationStub.java
│   ├── BbkConstantDeclarationStub.java
│   ├── BbkDataStructureDeclarationStub.java
│   ├── BbkPrototypeDeclarationStub.java
│   ├── BbkProcedureDeclarationStub.java
│   └── BbkFileDeclarationStub.java
└── index/
    ├── BbkIndexKeys.java                           — central catalog of StubIndexKey instances
    ├── BbkProcedureIndex.java
    ├── BbkPrototypeIndex.java
    ├── BbkVariableIndex.java
    ├── BbkConstantIndex.java
    ├── BbkDataStructureIndex.java
    └── BbkFileDeclarationIndex.java
```

Plus changes to `BBK.bnf` (`stubClass` and `elementTypeFactory` on the six declaration rules), an updated `BbkParserDefinition` (now backed by `IStubFileElementType`), and extensions in `plugin.xml` for each `<stubIndex>`.

---

## 12. What Block C does NOT cover (deliberately)

- **Rename rewrite for real.** Block B's `setName` is currently a no-op. Block C unlocks cross-file rename mechanically, but the actual text-rewriting needs `PsiElementFactory`-style helpers. Could be added here or in a small dedicated post-block.
- **Type checking.** Block D.
- **BIF documentation.** Block E.
- **Block A polish.** Any remaining provider-context papercuts surfaced in sandbox testing.

---

## 13. Related documents

- [`../functionalities.md`](../functionalities.md) — full feature map, all blocks
- [`../basic-autocomplete/theory.md`](../basic-autocomplete/theory.md) — Block A
- [`../reference-scope/theory.md`](../reference-scope/theory.md) — Block B
- [`./classes.md`](classes.md) — new and modified classes for Block C
- (future) `../type-system/theory.md` — Block D
- (future) `../builtins/theory.md` — Block E
