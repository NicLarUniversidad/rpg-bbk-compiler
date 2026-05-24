# Stubs + Index — Classes

**Status:** design / not implemented
**Scope:** every class to create and every file to modify for Block C (stubs + project-wide index)
**Companion:** [`theory.md`](theory.md)
**Prerequisites:** Block A and Block B landed and verified

---

## 1. New classes

All under `plugin-bbk/src/main/java/com/larena/boxbreaker/plugin/bbk/`.

### 1.1 Stubs (compact serialised PSI)

Each stub interface extends `StubElement<PsiClass>`, declares the fields stored on disk, and is consumed by the matching `IStubElementType` (defined in §1.2).

| Class | Responsibility |
|---|---|
| `stub/BbkFileStub.java` | Root stub for the whole BBK file. Extends `PsiFileStubImpl<BbkFile>`. Top of the stub tree; child stubs are the file's top-level declarations. |
| `stub/BbkVariableDeclarationStub.java` | Stub for `DCL-S`. Fields: `name`, `typeText` (string form of the type spec for quick display in goto-symbol). |
| `stub/BbkConstantDeclarationStub.java` | Stub for `DCL-C`. Fields: `name`, `valueText`. |
| `stub/BbkDataStructureDeclarationStub.java` | Stub for `DCL-DS`. Fields: `name`, `isQualified`, optionally `extName`. Subfield stubs live as children. |
| `stub/BbkDsSubfieldStub.java` | Stub for a DS subfield. Fields: `name`, `typeText`. Child of `BbkDataStructureDeclarationStub`. (Needed so member access can be resolved cross-file.) |
| `stub/BbkPrototypeDeclarationStub.java` | Stub for `DCL-PR`. Fields: `name`, `paramsText`, `returnTypeText`, `isExtPgm`. |
| `stub/BbkProcedureDeclarationStub.java` | Stub for `DCL-PROC`. Fields: `name`, `paramsText`, `returnTypeText`, `isExported`. |
| `stub/BbkFileDeclarationStub.java` | Stub for `DCL-F`. Fields: `name`, `usage`, `isKeyed`. |

### 1.2 Stub element types (factory + serializer)

Each `IStubElementType` knows how to read/write its stub from/to disk, build a PSI element from the stub, and create the stub from an AST node. They are the bridge between the AST world and the persisted stub world.

| Class | Responsibility |
|---|---|
| `stub/BbkStubElementTypes.java` | Central registry: one `public static final` field per stub element type. Loaded by `plugin.xml`'s `stubElementTypeHolder` extension so IntelliJ can discover all stub types at startup. |
| `stub/BbkStubElementTypeFactory.java` | Single static `factory(String name)` method referenced from `BBK.bnf`'s `elementTypeFactory=...` attribute. Maps a rule name to its stub element type. Grammar-Kit calls it during code generation. |
| `stub/BbkFileStubElementType.java` | Extends `IStubFileElementType<BbkFileStub>`. Holds the **stub version number** (critical: bump on every schema change). Builds `BbkFile` PSI from `BbkFileStub`. |
| `stub/BbkVariableDeclarationStubElementType.java` | Extends `IStubElementType<BbkVariableDeclarationStub, BbkVariableDeclaration>`. Implements `createStub`, `createPsi`, `serialize`, `deserialize`, `indexStub` (registers the stub with `BbkVariableIndex`). |
| `stub/BbkConstantDeclarationStubElementType.java` | Same shape as above, for DCL-C. |
| `stub/BbkDataStructureDeclarationStubElementType.java` | Same shape, for DCL-DS. |
| `stub/BbkDsSubfieldStubElementType.java` | Same shape, for DS subfields. |
| `stub/BbkPrototypeDeclarationStubElementType.java` | Same shape, for DCL-PR. |
| `stub/BbkProcedureDeclarationStubElementType.java` | Same shape, for DCL-PROC. |
| `stub/BbkFileDeclarationStubElementType.java` | Same shape, for DCL-F. |

### 1.3 Indexes (name → stubs)

Each `StringStubIndexExtension` exposes a queryable index keyed by case-insensitive name.

| Class | Responsibility |
|---|---|
| `index/BbkIndexKeys.java` | Central catalog of `StubIndexKey` instances. One key per declaration kind. Used by both the indexes (to register themselves) and the references (to query). |
| `index/BbkVariableIndex.java` | Extends `StringStubIndexExtension<BbkVariableDeclaration>`. Returns the `StubIndexKey` from `BbkIndexKeys`. Used by `BbkIdentReference` and `BbkScopeCompletionProvider` when looking for cross-file variables. |
| `index/BbkConstantIndex.java` | Same shape, for constants. |
| `index/BbkDataStructureIndex.java` | Same shape, for data structures. |
| `index/BbkPrototypeIndex.java` | Same shape, for prototypes. |
| `index/BbkProcedureIndex.java` | Same shape, for procedures. The most-used index in practice (procedure calls). |
| `index/BbkFileDeclarationIndex.java` | Same shape, for files. |

### 1.4 Cross-file reference helpers

| Class | Responsibility |
|---|---|
| `reference/BbkProjectScopeLookup.java` | Static utility: `findInProject(project, name, indexKey)` returns all declarations across the project matching a name. Wraps `StubIndex.getElements(...)`. Used by every reference's `resolveUncached()` after the local scope misses, and by `BbkScopeCompletionProvider` to enumerate cross-file variants. |

---

## 2. New resources

| File | Responsibility |
|---|---|
| (none) | Block C adds no new XML, properties, or icons. All new strings (e.g. "cross-file usage" labels) are added as keys to the existing `messages/BbkBundle.properties` if needed. |

---

## 3. Modified files

### 3.1 `src/main/grammar/BBK.bnf` (required)

Add two attributes to each declaration rule that should be stub-backed (DCL-S, DCL-C, DCL-DS, DS subfield, DCL-PR, DCL-PROC, DCL-F):

```bnf
procedure_declaration ::= KW_DCL_PROC IDENT inline_param_list? return_type? proc_modifier* block_statement {
  pin=1
  mixin="com.larena.boxbreaker.plugin.bbk.psi.BbkNamedElementMixin"
  implements="com.intellij.psi.PsiNamedElement"
  // ↓ NEW for Block C
  stubClass="com.larena.boxbreaker.plugin.bbk.stub.BbkProcedureDeclarationStub"
  elementTypeFactory="com.larena.boxbreaker.plugin.bbk.stub.BbkStubElementTypeFactory.factory"
}
```

Plus the same change on all other stub-backed rules. Requires a regen via `:plugin-bbk:generateBbkParser`.

### 3.2 `psi/BbkNamedElementMixin.java` (required)

Change the base class so stub-backed elements work. Currently:

```java
public abstract class BbkNamedElementMixin extends ASTWrapperPsiElement
    implements PsiNameIdentifierOwner { ... }
```

Becomes:

```java
public abstract class BbkNamedElementMixin<S extends StubElement<?>>
    extends StubBasedPsiElementBase<S> implements PsiNameIdentifierOwner {

    protected BbkNamedElementMixin(@NotNull ASTNode node) { super(node); }
    protected BbkNamedElementMixin(@NotNull S stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }
    // getName / getNameIdentifier / setName unchanged
}
```

This gives the regenerated PSI impls the two-constructor pattern (AST or stub).

### 3.3 `BbkParserDefinition.java` (required)

Replace the plain `IFileElementType FILE` with an `IStubFileElementType`:

```java
public static final IFileElementType FILE = new BbkFileStubElementType();
```

And implement `getStubVersion()` in `BbkFileStubElementType` returning a hard-coded integer that gets bumped whenever the stub schema changes.

### 3.4 `psi/BbkFile.java` (required)

Change so the file PSI is stub-aware:

```java
public class BbkFile extends PsiFileBase implements PsiFileWithStubSupport {
    // overrides getStub(), getStubTree(), etc. as required
}
```

### 3.5 `src/main/resources/META-INF/plugin.xml` (required)

Two new extension points:

```xml
<!-- Register all stub element types so IntelliJ can persist/load them. -->
<stubElementTypeHolder
    class="com.larena.boxbreaker.plugin.bbk.stub.BbkStubElementTypes"/>

<!-- Register every StubIndex extension. -->
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkVariableIndex"/>
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkConstantIndex"/>
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkDataStructureIndex"/>
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkPrototypeIndex"/>
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkProcedureIndex"/>
<stubIndex implementation="com.larena.boxbreaker.plugin.bbk.index.BbkFileDeclarationIndex"/>
```

### 3.6 References extended for cross-file (required)

| File | Change |
|---|---|
| `reference/BbkIdentReference.java` | In `resolveUncached`, after the local-scope lookup misses, fall back to `BbkProjectScopeLookup.findInProject(...)` for each candidate index (procedures, prototypes, constants, variables, DSs, files). |
| `reference/BbkTypeReference.java` | Same fall-back, but only into `BbkDataStructureIndex` and `BbkVariableIndex`. |
| (subroutine reference stays unchanged — `exsr` is procedure-scoped, no cross-file) | — |

### 3.7 Scope completion extended for cross-file (required)

`completion/providers/BbkScopeCompletionProvider.java`: after iterating `BbkScopeWalker.allVisible(position)`, also iterate the project-wide indexes (filtering out duplicates by name to keep the popup clean).

### 3.8 (Optional) `messages/BbkBundle.properties`

Add labels distinguishing "this file" vs "another file" for completion entries, if we choose to surface that information visually.

---

## 4. Files NOT touched

| File | Why |
|---|---|
| `BBK.flex` | Lexer unchanged |
| `BbkLanguage.java`, `BbkFileType.java`, `BbkLexerAdapter.java` | Stable |
| `BbkElementType.java`, `BbkTokenType.java` | Token/element infra |
| `BbkSyntaxHighlighter.java` | No new colours |
| `BbkWordsScanner.java` | Already in place |
| `scope/*` | Block B's scope chain remains the source of truth for local visibility |
| `types/BbkTypeResolver.java` | Shallow resolver unchanged; cross-file DS resolution piggybacks on `BbkDataStructureIndex` |
| Existing 14 keyword completion providers from Block A | Untouched |
| `editor/BbkSmartTypingHandler.java`, `editor/BbkBraceMatcher.java` | Editor polish unrelated to indexing |

---

## 5. Headcount summary

| Category | Block B end-state | Added in C | After C |
|---|---|---|---|
| New Java classes | 36 | **24** (8 stubs + 8 stub element types + 6 indexes + index keys + project lookup) | 60 |
| New XML resources | 1 | 0 | 1 |
| Properties files | 1 | 0 (extended) | 1 |
| Required modified files | 1 (plugin.xml) | 5 (plugin.xml, BBK.bnf, BbkParserDefinition, BbkFile, BbkNamedElementMixin) | — |
| References modified for cross-file | 0 | 2 (BbkIdentReference, BbkTypeReference) | — |
| Providers modified for cross-file | 0 | 1 (BbkScopeCompletionProvider) | — |

**Estimated effort:** 4–6 days. Boilerplate-heavy but mechanical: one stub class + one stub element type per declaration kind, all following the same template.

---

## 6. Order of implementation

Suggested sequence to minimise half-broken states:

1. **Mixin generification.** Change `BbkNamedElementMixin` to be generic over a `StubElement` subtype and provide both constructors. No-op until stubs exist; verify the project still compiles.
2. **File stub root.** Create `BbkFileStub`, `BbkFileStubElementType`, switch `BbkParserDefinition.FILE` to the new file element type. Bump version to `1`. Verify the IDE still opens BBK files.
3. **One declaration stub end-to-end** (DCL-PROC, the most useful). Create `BbkProcedureDeclarationStub` + `BbkProcedureDeclarationStubElementType`. Add `stubClass`/`elementTypeFactory` to the BNF for that rule. Regenerate. Verify the PSI compiles and the IDE still opens files.
4. **`BbkStubElementTypes` and the factory.** Wire the holder so IntelliJ knows about the new types. Register in `plugin.xml` via `stubElementTypeHolder`.
5. **`BbkProcedureIndex`.** Implement `StringStubIndexExtension`, register a `StubIndexKey` in `BbkIndexKeys`, and have the stub element type's `indexStub` push the procedure name into it. Verify in sandbox that `Ctrl+Alt+Shift+N` finds procedures.
6. **Wire `BbkProjectScopeLookup` + extend `BbkIdentReference`.** Add the cross-file fallback. Verify `Ctrl+B` on a procedure call jumps to its definition in another file.
7. **Repeat for the other declaration kinds** (DCL-S, DCL-C, DCL-DS, DS subfields, DCL-PR, DCL-F). Each one adds a stub, a stub element type, an index, and a small extension in `BbkProjectScopeLookup`/references.
8. **Extend `BbkScopeCompletionProvider`** to also enumerate the indexes for cross-file autocomplete.
9. **Bundle keys (optional)** for "this file" vs "another file" labelling.

After step 6 the plugin's killer feature lights up — that's the first natural demo checkpoint.

---

## 7. Open decisions (to resolve before / during implementation)

| # | Topic | Question |
|---|---|---|
| 1 | What goes in each stub? | Just `name`, or `name + signature/type text`? Storing signatures lets goto-symbol show "processOrder(a, b) -> INT" in the popup but doubles storage. Recommend: name + a short summary string, not full PSI. |
| 2 | Subfield indexing | Should DS subfields have their own index, or only be discoverable via parent DS? Recommend: yes, separate index — enables go-to-declaration on `currentCustomer.firstName` cross-file. |
| 3 | Cross-file procedure overload | What if two `.bbk` files declare a procedure with the same name? BBK doesn't allow overloading, so this is an error. Recommend: index returns both, references return a poly-variant, and an inspection later flags the duplicate. |
| 4 | Initial stub version number | Start at `1`. Document that every schema change requires a bump. |
| 5 | Stub vs PSI for completion entries | When a completion entry comes from another file, should we instantiate the full PSI to format the lookup, or work off the stub? Stub is faster but limits what we can show. Recommend: stub only (we already have the schematic summary). |
| 6 | Include `EXTPGM` / `EXTPROC` in stubs | These attributes affect how a procedure is called externally. Recommend: yes, store the external name when present so cross-file resolution can match by both internal and external names. |
| 7 | Index keys for files (DCL-F) | DCL-F refers to a physical file by name. Do we index by the BBK identifier (the F-name in the program) or by the external name (`EXTFILE("CUSTOMERS")`)? Recommend: both, but the internal name is the primary key. |
| 8 | Caching strategy for cross-file lookup | The index handles it. Don't add a second-level cache; that's a maintenance trap. |
| 9 | Performance on first index build | Acceptable to make the user wait the first time? IntelliJ's default UI ("Indexing...") handles this gracefully. Recommend: no special handling. |
| 10 | Stub serialisation format | Use IntelliJ's built-in `StubOutputStream`/`StubInputStream` (preferred). Don't roll a custom binary format. |

---

## 8. Closed decisions

(Empty for now. Populate after a design review, the same way Block A's §7 and Block B's §7 were populated.)
