# BoxBreaker Plugin — Autocomplete and Related IDE Functionalities

**Status:** design / not implemented
**Scope:** IntelliJ Platform features unlocked by the autocomplete infrastructure
**Plugin module:** `plugin-bbk/`

---

## Overview

Implementing autocomplete in IntelliJ unavoidably means building a **language analysis infrastructure** that is consumed by autocomplete plus several other IDE features. This document maps each feature to the infrastructure pieces that enable it, so investment can be planned around "blocks that unlock multiple features" rather than treating autocomplete in isolation.

---

## Functionalities by user-facing feature

| # | Feature | What the user sees | Enabling components |
|---|---|---|---|
| 1 | **Basic autocomplete** | `Ctrl+Space` suggests context-aware options (keywords, modifiers, types) | `completion/` |
| 2 | **Identifier autocomplete** | Suggests user-declared variables, procedures, constants in scope | `completion/` + `reference/` + `scope/` |
| 3 | **Member autocomplete** | After `employee.` suggests `firstName`, `lastName`, etc. | `completion/` + `reference/` + scope of DS |
| 4 | **Live templates** | `dcls` + Tab expands to `DCL-S name TYPE;` | `templates/` + XML resource |
| 5 | **Go to declaration** | `Ctrl+B` on an identifier jumps to its declaration | `reference/` |
| 6 | **Find usages** | `Alt+F7` lists every use of a symbol | `reference/` (+ `index/` for cross-file) |
| 7 | **Rename refactor** | `Shift+F6` renames a symbol across all uses | `reference/` + `scope/` |
| 8 | **Cross-file symbol search** | `Ctrl+Alt+Shift+N` finds a procedure or variable by name across the project | `stub/` + `index/` |
| 9 | **Smart completion (type-aware)** | `Ctrl+Shift+Space` filters completions by the expected type | `completion/` + `types/` |
| 10 | **Type-aware inspections** | Red squigglies on `INT not assignable to CHAR` and similar mistakes | `types/` + custom inspections |
| 11 | **Parameter info hints** | `Ctrl+P` shows the signature of the call the cursor is inside | `types/` + `reference/` |
| 12 | **Quick documentation** | `Ctrl+Q` shows docs for a symbol (including builtins) | `reference/` + `builtins/` |

---

## Investment blocks

The 12 features above are unlocked by 5 incremental investment blocks. Each block delivers multiple features at once.

### Block A — Lexer config + basic completion + templates

**Components:** `lexer/BbkWordsScanner`, `completion/` (keywords, modifiers, types providers — no scope-dependent), `templates/Bbk.xml`

**Unlocks:** features #1, #4

**What it does not do:** does not suggest user-declared identifiers (variables, procedures). Only static lists (keywords, type names, modifiers, BIFs by static catalog).

**Effort:** ~3-5 days

### Block B — Reference + scope

**Components:** `reference/`, `scope/`

**Unlocks:** features #2, #3, #5, intra-file #6, #7

**What it adds:** the plugin now understands which identifier refers to which declaration. Autocomplete can suggest user identifiers. Go-to-declaration and rename work.

**Effort:** ~5-7 days (depends on number of context patterns to handle)

### Block C — Stubs + index

**Components:** `stub/` (compact serialization of top-level declarations), `index/` (StubIndex extensions by name)

**Unlocks:** cross-file scope for features #6, #8

**What it adds:** the plugin no longer needs to re-parse every file to find a symbol; it consults a persistent index. Required for performance on projects with hundreds of files.

**Effort:** ~4-6 days. Highest boilerplate density, also requires `BBK.bnf` annotations on rules that should be stub-backed.

### Block D — Type system

**Components:** `types/` (type representation + inference)

**Unlocks:** features #9, #10, #11

**What it adds:** the plugin can infer the type of any expression. Smart completion, type checking inspections, and parameter info hints all depend on this.

**Effort:** ~5-7 days. Requires implementing inference rules for BBK's type system (decimal precision rules, LIKE/LIKEDS resolution, etc.).

### Block E — Builtins catalog

**Components:** `builtins/` (registry of trim, substr, len, char, int, date, etc.)

**Unlocks:** improves #1, adds #12

**What it adds:** built-in functions appear in completion with their signatures and inline documentation. The type checker can validate calls against builtin signatures.

**Effort:** ~1-2 days, mostly catalog data entry.

---

## Recommended progression

For a usable IDE experience as fast as possible:

```
Block A         → autocomplete básico + snippets visible          (~1 week)
Block A + B     → autocomplete real con identificadores            (~2 weeks total)
+ Block E       → builtins decorados con docs                       (~2.5 weeks total)
+ Block C       → escala a proyectos grandes                        (~3.5 weeks total)
+ Block D       → smart completion + inspections semánticas         (~5 weeks total)
```

Each phase delivers tangible IDE features. The project can be released and demoed at any phase boundary.

---

## Components not strictly required for autocomplete

These appear in the infrastructure but serve broader purposes:

- `lexer/BbkWordsScanner` — also used by extend-selection, word-aware navigation, spell checker, find-usages text matching
- `reference/` — also used by go-to-declaration, find usages, rename
- `scope/` — also used by unused-variable inspections, shadow warnings
- `stub/` + `index/` — used by all cross-file features, project-wide search
- `types/` — also used by type-checking inspections, refactors like "extract variable"

In other words, only `completion/`, `templates/`, and parts of `builtins/` are *strictly* "autocomplete". Everything else is general language analysis infrastructure that autocomplete consumes alongside many other IDE features.

---

## File / package layout

```
plugin-bbk/src/main/java/com/larena/boxbreaker/plugin/bbk/
├── lexer/
│   └── BbkWordsScanner.java                              (Block A)
├── reference/                                            (Block B)
│   ├── BbkReferenceContributor.java
│   ├── BbkIdentReference.java
│   ├── BbkMemberReference.java
│   └── BbkSubroutineReference.java
├── scope/                                                (Block B)
│   ├── BbkScope.java
│   ├── BbkScopeWalker.java
│   ├── BbkModuleScope.java
│   ├── BbkProcedureScope.java
│   └── BbkBlockScope.java
├── stub/                                                 (Block C)
│   ├── BbkFileStub.java
│   ├── BbkVariableDeclarationStub.java
│   ├── BbkConstantDeclarationStub.java
│   ├── BbkDataStructureDeclarationStub.java
│   ├── BbkProcedureDeclarationStub.java
│   ├── BbkPrototypeDeclarationStub.java
│   ├── BbkFileDeclarationStub.java
│   └── BbkStubElementTypes.java
├── index/                                                (Block C)
│   ├── BbkProcedureIndex.java
│   ├── BbkVariableIndex.java
│   ├── BbkConstantIndex.java
│   └── BbkDataStructureIndex.java
├── completion/                                           (Block A + B)
│   ├── BbkCompletionContributor.java
│   ├── BbkCompletionPatterns.java
│   ├── matcher/
│   │   └── BbkHyphenAwarePrefixMatcher.java
│   └── providers/
│       ├── BbkKeywordCompletionProvider.java
│       ├── BbkTypeCompletionProvider.java
│       ├── BbkVarModifierCompletionProvider.java
│       ├── BbkDsModifierCompletionProvider.java
│       ├── BbkParamModifierCompletionProvider.java
│       ├── BbkFileKeywordCompletionProvider.java
│       ├── BbkProcModifierCompletionProvider.java
│       ├── BbkScopeCompletionProvider.java
│       ├── BbkMemberCompletionProvider.java
│       ├── BbkFileOpCompletionProvider.java
│       ├── BbkDirectiveCompletionProvider.java
│       └── BbkStarIdentCompletionProvider.java
├── templates/                                            (Block A)
│   └── BbkLiveTemplateContext.java
├── types/                                                (Block D)
│   ├── BbkType.java
│   ├── BbkPrimitiveType.java
│   ├── BbkStructType.java
│   ├── BbkProcedureType.java
│   └── BbkTypeInferrer.java
└── builtins/                                             (Block E)
    ├── BbkBuiltinFunction.java
    └── BbkBuiltinRegistry.java

plugin-bbk/src/main/resources/
└── liveTemplates/
    └── Bbk.xml                                           (Block A)
```

**Class count totals:**

| Block | New classes | Resources | plugin.xml additions |
|---|---|---|---|
| A | 14 (1 lexer + 12 completion providers + 1 contributor) + 1 patterns helper + 1 matcher + 1 template context | 1 XML | 2-3 extensions |
| B | 9 (4 reference + 5 scope) | 0 | 1 extension (referenceContributor) |
| C | 12 (8 stub + 4 index) | 0 | 5-6 extensions (stubElementTypeHolder + 4 indexes) + BNF annotations |
| D | 5 (types) | 0 | 0 |
| E | 2 (builtins) | 0 | 0 |
| **Total** | **~43** | **1** | **~10 extensions** |
