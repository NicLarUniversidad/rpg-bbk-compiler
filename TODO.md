# TODO — BoxBreaker

In-progress and pending work list. Live document, updated as development advances.

For the architectural context of each item see [`docs/architecture.md`](docs/architecture.md).

---

## Done

- [x] Initial architecture documented (`docs/architecture.md`)
- [x] Monorepo module structure (9 subprojects)
- [x] README + `.gitignore` + `LICENSE`
- [x] Public repo created on GitHub: `NicLarUniversidad/rpg-bbk-compiler`

## In progress

(empty for now)

## Next

- [ ] Initial BBK spec (`docs/bbk-spec.md`) — define grammar, types, minimal opcodes
- [ ] Decide language for `bbk-core` (Java vs Kotlin)
- [ ] Decide language for `bbk-runtime` (Java vs C)

## Backlog — compiler core

- [ ] RPG lexer
- [ ] RPG parser (basic subset — fixed columns, core opcodes)
- [ ] RPG AST
- [ ] RPG → BBK translation (core opcodes)
- [ ] BBK AST/IR in `bbk-core`
- [ ] BBK semantic analysis
- [ ] BBK → C lowering (core opcodes)
- [ ] `gcc` invocation from `bbk-compiler`
- [ ] BBK interpreter (dev mode)

## Backlog — runtime

- [ ] Job queues emulation
- [ ] Activation groups emulation
- [ ] Library lists emulation
- [ ] DDS-style data access
- [ ] Spool files

## Backlog — IntelliJ tooling

- [ ] `plugin-bbk`: syntax highlighting
- [ ] `plugin-bbk`: editor parser (inline errors)
- [ ] `plugin-bbk`: basic autocomplete (see `docs/boxbreaker/autocomplete/basic-autocomplete/`)
  - [ ] Decide: single shared icon (reuse `bbk.svg`) vs differentiated icons per category (keyword / type / modifier / directive / file-op / star-ident)
- [ ] `plugin-rpg`: syntax highlighting
- [ ] `plugin-rpg`: editor parser
- [ ] `plugin-rpg`: "translate to BBK" command

## Backlog — IDE

- [ ] `boxbreaker-ide`: initial bundle on IntelliJ Platform SDK
- [ ] `boxbreaker-ide`: entry points integration for interpreter and AOT
- [ ] `boxbreaker-ide`: branding (splash, icon, name)
- [ ] `boxbreaker-ide`: distribution build (installer)

## Backlog — testing

- [ ] E2E suite: compile RPG → run → verify output
- [ ] Interpreter vs AOT equivalence (same RPG, same output)

## Distribution

- [ ] Publish BBK plugin on JetBrains Marketplace
- [ ] Publish RPG plugin on JetBrains Marketplace
- [ ] Distribute BoxBreaker IDE (Windows/Linux installers)

---

## Roadmap (long-term)

Ideas beyond the MVP, no timeline commitment:

- Cross-compile to ARM (Raspberry Pi, Apple Silicon)
- COBOL front-end (reusing the BBK back-end)
- Web playground (RPG → BBK → C → WASM in the browser)
- Integration with LLM code generation (link with thesis)
