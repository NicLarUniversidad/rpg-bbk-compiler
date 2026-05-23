# Basic Autocomplete — Theory

**Status:** design / not implemented
**Scope:** the IntelliJ Platform concepts that underpin Block A (basic autocomplete + live templates) of [`../functionalities.md`](../functionalities.md)
**Plugin module:** `plugin-bbk/`

---

## 1. The central concept

IntelliJ does not have "an autocomplete". It has an **extension framework**: you register a `CompletionContributor` and the plugin is called every time the user requests suggestions (auto-popup while typing, or explicit `Ctrl+Space`).

The flow is always the same:

```
User types         →    IntelliJ identifies position and prefix
                   →    Calls your contributors
                   →    Each one contributes LookupElements
                   →    IntelliJ orders, deduplicates, presents
                   →    User picks → InsertHandler runs the insertion
```

---

## 2. The "dummy identifier" — the trick that holds it all together

**Problem:** when the user has the cursor at `DCL-S name |` (pipe = cursor), the file **does not parse cleanly** because the type is missing. If you asked for completion there, your PSI is broken and you don't know which element contains the cursor.

**IntelliJ's solution:** before parsing, it **inserts a dummy identifier** (`IntellijIdeaRulezzz` by default) at the cursor position. The file becomes `DCL-S name IntellijIdeaRulezzz`, parses as a `variable_declaration` with a broken type, and you can inspect the PSI around that dummy identifier to know **where you are**.

This is invisible to the user. For your plugin it's the only thing that makes autocomplete tractable.

---

## 3. PSI Patterns — declaring "I apply here"

Each context where you want to suggest different things is expressed with a **pattern** from the `PlatformPatterns` DSL:

```
psiElement().withParent(BbkVariableDeclaration.class)
            .afterSibling(BbkIdent)
```

Meaning: "the element under the cursor must be a child of a variable declaration, right after an IDENT". When the pattern matches, your `CompletionProvider` runs and contributes the primitive types.

The practical design is **one provider per syntactic context**. In BBK's Block A you would have ~12:

| Context | What to suggest |
|---|---|
| Top-level start of declaration | `DCL-S`, `DCL-C`, `DCL-DS`, `DCL-F`, `DCL-PR`, `DCL-PROC`, `CTL-OPT` |
| After `DCL-S name` | primitive types (`INT`, `CHAR`, `PACKED`, etc.) |
| After a type with args | modifiers (`INZ`, `DIM`, `STATIC`, `BASED`, etc.) |
| After `DCL-F name` | file keywords (`USAGE`, `KEYED`, `DISK`, `PRINTER`) |
| Inside `USAGE(` | `*INPUT`, `*OUTPUT`, `*UPDATE`, `*DELETE` |
| Inside `OPTIONS(` | `*NOPASS`, `*OMIT`, `*VARSIZE`, etc. |
| After `DCL-DS name` with `{` | (DS subfield context) |
| Inside procedure body | statement keywords (`if`, `while`, `return`, etc.) + file ops |
| Top-level directives | `PRE-IF`, `PRE-DEFINE`, `PRE-INCLUDE`, etc. |
| Etc. |

Keeping providers separated is what makes the plugin **maintainable**: when someone breaks the modifiers provider, the types provider does not break.

---

## 4. LookupElement — what appears in the list

Each suggestion is a `LookupElement`. The standard builder:

```
LookupElementBuilder.create("INT")
    .withIcon(BbkIcons.TYPE)
    .withTypeText("primitive")
    .withTailText("(precision)")
    .withInsertHandler((ctx, item) -> { /* opens parens, positions cursor */ })
```

The icon, the type text on the right, the tail text — everything you see in IntelliJ's popup is composed here. For Block A it's all static metadata (does not depend on scope), but you already set the visual pattern that identifier providers reuse later.

---

## 5. PrefixMatcher — matching what was typed against candidates

By default IntelliJ matches by case-sensitive prefix, CamelHumps style. For BBK you need two tweaks:

- **Case-insensitive**: BBK treats `dcl-s` and `DCL-S` the same. Without this, typing `dcl` does not suggest `DCL-S`.
- **Hyphen-aware**: the prefix `dcl` must match `DCL-S`. The default matcher treats `-` as a word separator, and sometimes a custom matcher that knows to handle the whole word as a unified token is better.

This is solved with a custom `PrefixMatcher` that you wrap around the `result` passed to the provider.

---

## 6. InsertHandler — what happens on confirm

When the user hits Tab/Enter, you can run post-insertion logic:

- `INT` inserted → open `()` and leave the cursor inside
- `DCL-S` inserted → insert a space and position the cursor to type the name
- `if` inserted → complete to `if () {\n  \n}` and position the cursor on the condition

This is what makes autocomplete **fluid**: not just suggesting names, but completing the usage skeleton.

---

## 7. Auto-popup vs explicit completion

IntelliJ pops up:
- **Automatically** while you type (configurable, usually after one or two chars)
- **Explicitly** with `Ctrl+Space` (basic) or `Ctrl+Shift+Space` (smart, comes in Block D)

The same `CompletionContributor` covers both cases. The difference is set by IntelliJ; from your code's perspective it's transparent.

---

## 8. Why Block A "does not understand identifiers"

The critical theory point: Block A **does not consult scope**. Providers only know static catalogs (language keywords, primitive types, fixed modifiers). This is deliberate:

- It's much less work (no need to walk the PSI looking for declarations)
- It covers ~60% of perceived autocomplete (keywords are what the user types most)
- It's what IntelliJ already does for free for many languages via lexer word-completion

The jump to "real autocomplete" — suggesting `customerName` when the user types `cust` — requires Reference + Scope (Block B). That's why they are separated.

---

## 9. Interaction with the lexer

The `BbkWordsScanner` (part of Block A) tells IntelliJ what counts as a word for auto-completion. Without it, default word-completion treats `DCL-S` as two tokens (`DCL` and `S`) and breaks the suggestion. It's trivial config but essential for hyphenated keywords to work.

---

## 10. Live templates (also Block A)

Live templates are conceptually separate from `CompletionContributor` but are bundled with Block A because they share the lexer config and complement the keyword suggestions.

A template is an XML resource declaring:
- An **abbreviation** (e.g. `dcls`)
- An **expansion** with placeholders (e.g. `DCL-S $NAME$ $TYPE$;`)
- A **context** (where the template is offered: top-level, procedure body, etc.)

When the user types the abbreviation and hits Tab, IntelliJ expands the template and lets them tab through the placeholders.

For BBK, useful initial templates:
- `dcls` → `DCL-S $NAME$ $TYPE$;`
- `dclc` → `DCL-C $NAME$ $VALUE$;`
- `dclds` → `DCL-DS $NAME$ {\n  $END$\n}`
- `proc` → `DCL-PROC $NAME$ {\n  $END$\n}`
- `ifb` → `if ($COND$) {\n  $END$\n}`
- `whileb`, `forb`, `selectb`, `monitorb` — equivalents for the other control structures
- `preif` → `PRE-IF $COND$\n  $END$\nPRE-ENDIF`

Templates are registered via a `BbkLiveTemplateContext` class plus an XML resource at `resources/liveTemplates/Bbk.xml`.

---

## 11. The minimal class set for Block A

Putting the theory into a concrete file map:

```
plugin-bbk/src/main/java/com/larena/boxbreaker/plugin/bbk/
├── lexer/
│   └── BbkWordsScanner.java                — tells IntelliJ what a "word" is
├── completion/
│   ├── BbkCompletionContributor.java       — registers all providers and patterns
│   ├── BbkCompletionPatterns.java          — pattern definitions reused by providers
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
│       ├── BbkFileOpCompletionProvider.java
│       ├── BbkDirectiveCompletionProvider.java
│       └── BbkStarIdentCompletionProvider.java
└── templates/
    └── BbkLiveTemplateContext.java

plugin-bbk/src/main/resources/
└── liveTemplates/
    └── Bbk.xml
```

Plus the `plugin.xml` declarations:
- `<completion.contributor language="BBK" implementationClass="...BbkCompletionContributor"/>`
- `<lang.wordScanner language="BBK" implementationClass="...BbkWordsScanner"/>`
- `<defaultLiveTemplates file="/liveTemplates/Bbk.xml"/>`
- `<liveTemplateContext implementation="...BbkLiveTemplateContext"/>`

---

## 12. What Block A does NOT cover (intentionally)

To stay honest about scope, Block A leaves these out:

- **User identifiers** — variables, procedures, constants declared in the file. Needs Block B (reference + scope).
- **Member access** — `employee.|` does not list `firstName`, `lastName`. Needs Block B + DS scope.
- **Type-aware filtering** — `Ctrl+Shift+Space` to filter by expected type. Needs Block D.
- **Cross-file symbols** — procedures declared in another file are not suggested. Needs Block C (stubs + index).
- **Builtin function docs** — `trim`, `substr`, etc. appear but without signatures or docs. Needs Block E.

Each of those is a separate block with its own theory document (to be written).

---

## 13. Related documents

- [`../functionalities.md`](../functionalities.md) — full feature map, all blocks
- (future) `../reference-scope/theory.md` — Block B
- (future) `../stubs-index/theory.md` — Block C
- (future) `../type-system/theory.md` — Block D
- (future) `../builtins/theory.md` — Block E
