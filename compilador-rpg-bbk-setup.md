# RPG → BBK → C Compiler — Setup and guide

**Date:** May 2026
**Status:** in active development, no git/GitHub yet
**Confirmed stack:** Java + Gradle for the compiler; Kotlin/Java + IntelliJ Platform SDK for the plugin
**Initial visibility:** public from day 1 (confirmed decision)

---

## 1. What is it and why does it matter?

A toolchain that compiles RPG (IBM i / AS/400) to native binaries on Windows via a custom IR (BBK) and `gcc`. Built to accelerate the local development and testing cycle in legacy modernization projects.

**Pipeline:**

```
RPG → BBK (custom IR) → C → gcc → native binary (Windows/Linux/macOS)
```

**Components under development:**
- RPG → AST parser
- BBK IR (custom intermediate language)
- BBK → C lowering
- Runtime service that emulates IBM i primitives missing on Windows
- IntelliJ plugin with BBK support

**Why it's portfolio gold:**
- Real compiler design (custom IR, not just using an existing one)
- IDE tooling (IntelliJ Platform SDK)
- Complex cross-language semantics (RPG is weird: fixed columns, indicators, obscure opcodes)
- Architectural pragmatism (target C → leverage gcc's mature backend)
- Connects with thesis on LLMs + code generation
- Very rare niche: few young devs build compilers for legacy languages

---

## 2. Repo strategy

**Decision:** monorepo initially. Spin the plugin off once it reaches the JetBrains Marketplace.

### Recommended structure

```
<repo-name>/
├── README.md                       ← pipeline diagram + architecture
├── LICENSE                         ← MIT recommended
├── .gitignore
├── settings.gradle.kts             ← multi-project Gradle
├── build.gradle.kts                ← root config
├── docs/
│   ├── architecture.md             ← design decisions
│   ├── bbk-spec.md                 ← formal BBK specification
│   └── pipeline-diagram.png        ← visual of the flow
├── compiler/                       ← main Java module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/larena/bbk/
│       │   ├── parser/             ← RPG lexer + parser (ANTLR4 recommended)
│       │   ├── ast/                ← RPG AST
│       │   ├── ir/                 ← BBK as Java objects
│       │   ├── lowering/           ← BBK → C generator
│       │   └── cli/                ← CLI entry point
│       └── test/java/...
├── runtime/                        ← IBM i emulation layer (Java or C)
│   ├── build.gradle.kts
│   └── src/...
├── intellij-plugin/                ← BBK plugin
│   ├── build.gradle.kts            ← uses gradle-intellij-plugin
│   ├── src/main/
│   │   ├── kotlin/                 ← plugin code
│   │   └── resources/META-INF/plugin.xml
│   └── ...
├── examples/                       ← .rpg test programs
│   ├── hello-world/
│   ├── customer-master/            ← example with DDS, indicators
│   └── ...
└── tests/
    └── e2e/                        ← compile RPG → run → verify output
```

### When to spin the plugin into its own repo

- When you publish to the JetBrains Marketplace (its own build/release pipeline)
- When contributors who only touch the plugin show up
- When the monorepo builds get slow

---

## 3. Repo naming — PENDING DECISION

Pending. The user wanted more time to think about it. Options evaluated:

- `bbk-lang` — focus on the IR, brandable, short
- `bbk-toolchain` — BBK brand + descriptive
- `bbk-rpg-toolchain` — descriptive, SEO friendly for "RPG modernization"
- `rpg-modernization-toolkit` — max marketing/SEO
- `rpg-bbk` — explicit RPG-BBK relationship
- `bbk` — minimalist, pure brand
- `rpg-to-native` — action-centric

Final URL will be: `github.com/NicLarUniversidad/<repo-name>`

---

## 4. .gitignore ready for Java + Gradle + IntelliJ

```gitignore
# IntelliJ IDEA / JetBrains IDEs
.idea/
*.iml
*.ipr
*.iws
out/
.intellijPlatform/

# Gradle
.gradle/
build/
gradle-app.setting
!gradle-wrapper.jar
!gradle-wrapper.properties

# Maven (in case you combine)
target/

# Java
*.class
*.log
*.jar
*.war
*.ear
hs_err_pid*

# Compiler output / natives
*.o
*.exe
*.dll
*.so
*.dylib
dist/
bin/

# Tests / coverage
*.log
coverage/
.nyc_output/
jacoco.exec

# OS
.DS_Store
Thumbs.db
desktop.ini

# Environment
.env
.env.local
*.local

# Plugin signing (when you publish to Marketplace)
*.token
plugin-distribution/
```

---

## 5. README — template for maximum impact

It must be scannable in 30 seconds. Suggested structure:

```markdown
# <repo-name> — RPG to Native Compiler for Windows

> Compile IBM i RPG programs to native Windows binaries via a custom IR (BBK) and gcc.

[![Build](https://img.shields.io/github/actions/workflow/status/NicLarUniversidad/<repo>/ci.yml)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)]()

## Pipeline

\`\`\`
RPG → BBK (custom IR) → C → gcc → native binary (Windows/Linux/macOS)
\`\`\`

## Why

Legacy RPG codebases are tied to IBM i hardware for development, build, and test.
This toolchain decouples the dev cycle from IBM hardware, enabling local iteration
on Windows, Linux, or macOS while preserving RPG semantics through BBK and a
runtime emulation layer.

## Status

**Alpha — work in progress.**

- [x] BBK language spec v0.1
- [x] RPG lexer
- [x] RPG parser (basic)
- [ ] RPG parser (full coverage of dialects)
- [ ] BBK → C lowering (core opcodes)
- [ ] BBK → C lowering (DDS, indicators)
- [ ] Runtime: job queues emulation
- [ ] Runtime: activation groups
- [ ] Runtime: library list
- [ ] IntelliJ plugin: syntax highlighting
- [ ] IntelliJ plugin: autocomplete
- [ ] IntelliJ plugin: published to JetBrains Marketplace

(update checkboxes as real progress happens)

## Quick start

\`\`\`bash
# Build
./gradlew build

# Compile a sample RPG program to native binary
./gradlew :compiler:run --args="examples/hello-world/hello.rpg --output dist/hello.exe"

# Run the resulting binary
./dist/hello.exe
\`\`\`

## Architecture

The compiler is structured as a three-stage pipeline:

1. **Front-end (RPG → AST):** lexer + parser built with ANTLR4
2. **Middle-end (AST → BBK):** semantic analysis, lowering RPG idioms to BBK
3. **Back-end (BBK → C):** code generation targeting C99, compiled via gcc

The runtime emulation layer provides Windows implementations of IBM i primitives
that the compiled binaries depend on (job queues, activation groups, DDS data
access, library lists, spool files).

See `docs/architecture.md` for details.

## BBK — the intermediate representation

BBK is a small, statically-typed language designed specifically as a compilation
target for legacy business languages (currently RPG; future: COBOL).

See `docs/bbk-spec.md` for the language specification.

## IntelliJ plugin

A companion plugin for IntelliJ IDEA provides BBK language support:

- Syntax highlighting
- (planned) Autocomplete
- (planned) Inline error detection
- (planned) Debugger integration

See `intellij-plugin/README.md`.

## Roadmap

(detail beyond status — big features 6-12 months out)

- Cross-compile to ARM (Raspberry Pi, Apple Silicon)
- COBOL front-end (reusing the BBK back-end)
- Web playground (compile RPG → BBK → C → WASM in browser)
- LLM-assisted RPG generation (link with thesis work)

## Contributing

(later, when relevant)

## License

MIT
```

---

## 6. Recommended tech stack

| Component | Technology | Why |
|--|--|--|
| RPG parser | **ANTLR4** | Gold standard for Java compilers. JetBrains uses it. Knowing it is a CV signal |
| Build | **Gradle (Kotlin DSL)** multi-project | Modern standard, better than Maven for multi-module |
| IntelliJ plugin | **gradle-intellij-plugin** | JetBrains official |
| Test framework | **JUnit 5 + AssertJ** | Modern Java standard |
| Code style | **google-java-format** or **spotless** | Auto-format, avoids bikeshedding |
| CI | **GitHub Actions** | Free for public repos, native integration |
| Documentation | **MkDocs** or plain markdown in `/docs` | Start simple, scale if it grows |
| Runtime emulation language | **Java or C** | If C, you can link it directly into the compiled binary. If Java, you can run it as a separate service |

---

## 7. Command sequence to initialize

Once the name is defined:

```bash
# 1. In the folder where you have the local project
cd <path-to-your-local-project>

# 2. Initialize git
git init -b main

# 3. Create .gitignore (copy the contents of section 4 of this doc)
# Create README.md (copy and adapt the template from section 5)
# Create LICENSE (MIT — find the standard template)

# 4. First commit with structure
git add .gitignore README.md LICENSE
git commit -m "Initial commit: project structure and README"

# 5. Add the existing code
git add .
git commit -m "Initial code drop: compiler, runtime, intellij plugin"

# 6. Create public repo on GitHub
gh repo create NicLarUniversidad/<repo-name> --public \
  --description "RPG to native compiler via BBK IR for Windows"

# 7. Link remote and push
git remote add origin git@github.com:NicLarUniversidad/<repo-name>.git
git push -u origin main
```

If you don't have the `gh` CLI: create the repo from GitHub's web UI first, then link and push.

---

## 8. Quick wins after creating the repo

Recommended order (one thing per session):

1. **Solid README** with a pipeline diagram (section 5 solves this for you)
2. **GitHub Actions** for basic CI (build + test on push/PR) — 30 min
3. **Badges in README** (build status, license, version) — 10 min
4. **`docs/architecture.md`** with design decisions — 1-2 hrs (justifies the IR, justifies C as target, justifies the plugin)
5. **`docs/bbk-spec.md`** with the formal BBK specification — this becomes the most impressive piece of the repo
6. **Pipeline diagram in SVG or PNG** inside `/docs` — visual matters
7. **Plugin on JetBrains Marketplace** (when stable) — this gives you a public profile at marketplace.jetbrains.com

---

## 9. CV — how it will look when we add it

**When it reaches a functional MVP, it goes in the "Featured personal projects" section of the ES and EN CV.**

### ES draft (review when adding)

```
### <project name> — RPG-to-Windows Compiler with Custom IR · 2026 – Present
github.com/NicLarUniversidad/<repo-name>

A compilation toolchain for RPG (IBM i / AS/400) to native binaries on Windows,
designed to accelerate the local development and testing cycle in legacy
modernization projects.

Pipeline: RPG → BBK (custom intermediate language) → C → gcc.

- RPG parser built with ANTLR4
- Design and implementation of BBK as the custom IR
- BBK → C lowering
- Service layer that emulates IBM i runtime primitives missing on Windows
- IntelliJ plugin for BBK support

Stack: Java, Gradle, ANTLR4, JUnit 5, Kotlin (plugin), IntelliJ Platform SDK,
GitHub Actions, gcc.
```

### EN draft (review when adding)

```
### <project-name> — RPG-to-Windows Compiler with Custom IR · 2026 – Present
github.com/NicLarUniversidad/<repo-name>

Cross-platform compilation toolchain for IBM i RPG programs, designed to
accelerate the local development and testing cycle in legacy modernization
projects.

Pipeline: RPG → BBK (custom intermediate language) → C → gcc.

- RPG parser built with ANTLR4
- Designed and implemented BBK as the custom IR
- BBK → C lowering
- Service layer emulating IBM i runtime primitives missing on Windows
- IntelliJ plugin providing BBK language support

Tech: Java, Gradle, ANTLR4, JUnit 5, Kotlin (plugin), IntelliJ Platform SDK,
GitHub Actions, gcc.
```

---

## 10. Immediate pending items (next session on this topic)

1. **Define the repo name** ← blocking everything
2. Create folder structure locally
3. Initialize git
4. Write README v1
5. Create the public repo on GitHub
6. Initial push
7. Set up GitHub Actions CI (build + test)
8. Start documenting BBK in `docs/bbk-spec.md`

---

## 11. Strategic connections

This project connects with your professional narrative like this:

```
IAM (RPG → RDMLX modernization, current)
                ↓
        Saw the real pain
                ↓
  RPG-Windows-BBK compiler (this)
                ↓
   Solves the pain in a generalizable way
                ↓
        Generative-AI thesis
                ↓
LLM generates RPG → your compiler runs it without IBM
                ↓
    Thesis with a productive end-to-end use case
```

**This is NOT 3 separate projects — it's ONE coherent arc.** Sell it that way in interviews, posts, and future job applications.

---

## 12. LinkedIn posts that come out of this naturally

When the repo is up and the README is presentable:

- **Post:** *"Why I decided to design my own IR instead of compiling RPG directly to C"*
- **Post:** *"Building an IntelliJ plugin for my own language — lessons from the Platform SDK"*
- **Post:** *"What I learned compiling RPG (a '70s language) into modern binaries"*
- **Post:** *"BBK design: how I modeled an intermediate language for legacy business languages"*
- **Post (carefully):** *"Vibecoding a BPMS while writing a compiler 'by hand': two opposite disciplines in parallel"* — bridging post

Each post generates independent traction. Space them every 2-3 weeks.

---

## 13. Potential conferences / publications (long term)

To keep on the radar:

- **SLE (Software Language Engineering)** — exactly this type of paper
- **MODELS** — Model-Driven Engineering, a fit
- **ICSE Software Engineering in Practice (SEIP)** track
- **IBM TechXchange Conference** — IBM Champions program
- **Argentina Symposium on Software Engineering (ASSE)**
- **JConf Argentina** — local technical talk

Not urgent, but having a "publishable" version of the work opens academic doors if you want a PhD.
