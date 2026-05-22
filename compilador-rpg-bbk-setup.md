# Compilador RPG → BBK → C — Setup y guía

**Fecha:** Mayo 2026
**Estado:** en desarrollo activo, sin git/GitHub todavía
**Stack confirmado:** Java + Gradle para el compilador; Kotlin/Java + IntelliJ Platform SDK para el plugin
**Visibilidad inicial:** público desde día 1 (decisión confirmada)

---

## 1. ¿Qué es y por qué importa?

Toolchain de compilación de RPG (IBM i / AS/400) a binarios nativos en Windows mediante una IR propia (BBK) y `gcc`. Construido para acelerar el ciclo de desarrollo y testing local en proyectos de modernización de legacy.

**Pipeline:**

```
RPG → BBK (IR propio) → C → gcc → binario nativo (Windows/Linux/macOS)
```

**Componentes en desarrollo:**
- Parser RPG → AST
- IR BBK (lenguaje intermedio propio)
- Lowering BBK → C
- Servicio de runtime que emula primitivas de IBM i ausentes en Windows
- Plugin IntelliJ con soporte de BBK

**Por qué es portfolio gold:**
- Compiler design real (IR propio, no solo usar uno existente)
- IDE tooling (IntelliJ Platform SDK)
- Cross-language semantics complejos (RPG es raro: columnas fijas, indicadores, opcodes oscuros)
- Pragmatismo de arquitectura (target C → aprovechar backend maduro de gcc)
- Conecta con tesis sobre LLMs + generación de código
- Nicho rarísimo: pocos devs jóvenes hacen compiladores de lenguajes legacy

---

## 2. Estrategia de repos

**Decisión:** monorepo inicial. Escindir el plugin cuando llegue a JetBrains Marketplace.

### Estructura recomendada

```
<repo-name>/
├── README.md                       ← diagrama pipeline + arquitectura
├── LICENSE                         ← MIT recomendado
├── .gitignore
├── settings.gradle.kts             ← multi-project Gradle
├── build.gradle.kts                ← config raíz
├── docs/
│   ├── architecture.md             ← decisiones de diseño
│   ├── bbk-spec.md                 ← especificación formal de BBK
│   └── pipeline-diagram.png        ← visual del flujo
├── compiler/                       ← módulo Java principal
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/larena/bbk/
│       │   ├── parser/             ← RPG lexer + parser (ANTLR4 recomendado)
│       │   ├── ast/                ← AST de RPG
│       │   ├── ir/                 ← BBK como objetos Java
│       │   ├── lowering/           ← BBK → C generator
│       │   └── cli/                ← entry point CLI
│       └── test/java/...
├── runtime/                        ← capa emulación IBM i (Java o C)
│   ├── build.gradle.kts
│   └── src/...
├── intellij-plugin/                ← plugin BBK
│   ├── build.gradle.kts            ← usa gradle-intellij-plugin
│   ├── src/main/
│   │   ├── kotlin/                 ← código del plugin
│   │   └── resources/META-INF/plugin.xml
│   └── ...
├── examples/                       ← .rpg de prueba
│   ├── hello-world/
│   ├── customer-master/            ← ejemplo con DDS, indicadores
│   └── ...
└── tests/
    └── e2e/                        ← compilar RPG → ejecutar → verificar output
```

### Cuándo escindir el plugin a repo propio

- Cuando subas a JetBrains Marketplace (build/release pipeline propio)
- Cuando aparezcan contributors que solo toquen el plugin
- Cuando los builds del monorepo sean lentos

---

## 3. Naming del repo — PENDIENTE DE DECISIÓN

Pendiente. El usuario quería pensarlo más. Opciones evaluadas:

- `bbk-lang` — foco en el IR, brandeable, corto
- `bbk-toolchain` — brand BBK + descriptivo
- `bbk-rpg-toolchain` — descriptivo, SEO friendly para "RPG modernization"
- `rpg-modernization-toolkit` — marketing/SEO máximo
- `rpg-bbk` — relación explícita RPG-BBK
- `bbk` — minimalista, brand puro
- `rpg-to-native` — action-centric

URL final será: `github.com/NicLarUniversidad/<repo-name>`

---

## 4. .gitignore listo para Java + Gradle + IntelliJ

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

# Maven (por si combinás)
target/

# Java
*.class
*.log
*.jar
*.war
*.ear
hs_err_pid*

# Compiler output / nativos
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

# Plugin signing (cuando lo publiques en Marketplace)
*.token
plugin-distribution/
```

---

## 5. README — template para máximo impacto

Debe ser scaneable en 30 segundos. Estructura sugerida:

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

(actualizar checkboxes según avance real)

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

(detalle más allá del status — features grandes a 6-12 meses)

- Cross-compile to ARM (Raspberry Pi, Apple Silicon)
- COBOL front-end (reusing BBK back-end)
- Web playground (compile RPG → BBK → C → WASM in browser)
- LLM-assisted RPG generation (link with thesis work)

## Contributing

(después, cuando sea relevante)

## License

MIT
```

---

## 6. Stack técnico recomendado

| Componente | Tecnología | Por qué |
|--|--|--|
| Parser RPG | **ANTLR4** | Estándar de oro para compiladores en Java. JetBrains lo usa. Conocerlo es señal en CV |
| Build | **Gradle (Kotlin DSL)** multi-project | Estándar moderno, mejor que Maven para multi-módulo |
| Plugin IntelliJ | **gradle-intellij-plugin** | Oficial de JetBrains |
| Test framework | **JUnit 5 + AssertJ** | Estándar moderno Java |
| Code style | **google-java-format** o **spotless** | Auto-formato, evita bikeshedding |
| CI | **GitHub Actions** | Free para repos públicos, integración nativa |
| Documentation | **MkDocs** o markdown plano en `/docs` | Empezar simple, escalar si crece |
| Runtime emulation lang | **Java o C** | Si C, podés linkearlo directo al binario compilado. Si Java, podés correrlo como servicio aparte |

---

## 7. Secuencia de comandos para inicializar

Cuando el nombre esté definido:

```bash
# 1. En la carpeta donde tenés el proyecto local
cd <ruta-de-tu-proyecto-local>

# 2. Inicializar git
git init -b main

# 3. Crear .gitignore (copiar el contenido de la sección 4 de este doc)
# Crear README.md (copiar y adaptar el template de la sección 5)
# Crear LICENSE (MIT — buscar template estándar)

# 4. Primer commit con estructura
git add .gitignore README.md LICENSE
git commit -m "Initial commit: project structure and README"

# 5. Agregar el código existente
git add .
git commit -m "Initial code drop: compiler, runtime, intellij plugin"

# 6. Crear repo público en GitHub
gh repo create NicLarUniversidad/<repo-name> --public \
  --description "RPG to native compiler via BBK IR for Windows"

# 7. Linkear remoto y push
git remote add origin git@github.com:NicLarUniversidad/<repo-name>.git
git push -u origin main
```

Si no tenés `gh` CLI: crear el repo desde la web UI de GitHub primero, después linkear y push.

---

## 8. Quick wins post-creación del repo

Orden recomendado (1 cosa por sesión):

1. **README sólido** con diagrama del pipeline (sección 5 te lo deja resuelto)
2. **GitHub Actions** para CI básico (build + test en push/PR) — 30 min
3. **Badges en README** (build status, license, version) — 10 min
4. **`docs/architecture.md`** con las decisiones de diseño — 1-2 hs (justifica el IR, justifica C como target, justifica el plugin)
5. **`docs/bbk-spec.md`** con la especificación formal de BBK — esto se vuelve la pieza más impresionante del repo
6. **Diagrama del pipeline en SVG o PNG** dentro de `/docs` — visual matters
7. **Plugin en JetBrains Marketplace** (cuando esté estable) — eso te da perfil público en marketplace.jetbrains.com

---

## 9. CV — cómo se va a ver cuando sumemos

**Cuando llegue a MVP funcional, va en sección "Proyectos personales destacados" del CV ES y EN.**

### Borrador ES (para revisar al agregar)

```
### <nombre del proyecto> — Compilador RPG para Windows con IR propio · 2026 – Actualidad
github.com/NicLarUniversidad/<repo-name>

Toolchain de compilación de RPG (IBM i / AS/400) a binarios nativos en Windows,
diseñado para acelerar el ciclo de desarrollo y testing local en proyectos de
modernización de legacy.

Pipeline: RPG → BBK (lenguaje intermedio propio) → C → gcc.

- Parser RPG construido con ANTLR4
- Diseño e implementación de BBK como IR propio
- Lowering BBK → C
- Capa de servicio que emula primitivas del runtime IBM i ausentes en Windows
- Plugin de IntelliJ para soporte de BBK

Stack: Java, Gradle, ANTLR4, JUnit 5, Kotlin (plugin), IntelliJ Platform SDK,
GitHub Actions, gcc.
```

### Borrador EN (para revisar al agregar)

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

## 10. Pendientes inmediatos (próxima sesión sobre este tema)

1. **Definir el nombre del repo** ← bloqueante para todo
2. Crear estructura de carpetas en local
3. Inicializar git
4. Escribir README v1
5. Crear repo en GitHub público
6. Push inicial
7. Setup GitHub Actions CI (build + test)
8. Empezar a documentar BBK en `docs/bbk-spec.md`

---

## 11. Conexiones estratégicas

Este proyecto se conecta con tu narrativa profesional así:

```
IAM (modernización RPG → RDMLX, actual)
                ↓
        Vieron el dolor real
                ↓
  Compilador RPG-Windows-BBK (esto)
                ↓
    Resuelve el dolor de manera generalizable
                ↓
        Tesis IA generativa
                ↓
LLM genera RPG → tu compilador lo ejecuta sin IBM
                ↓
    Tesis con caso de uso productivo end-to-end
```

**Esto NO es 3 proyectos sueltos — es UN arco coherente.** Vendelo así en entrevistas, posts y postulaciones futuras.

---

## 12. Posts de LinkedIn que se desprenden naturalmente

Cuando el repo esté arriba y el README sea presentable:

- **Post:** *"Por qué decidí diseñar un IR propio en vez de compilar RPG directamente a C"*
- **Post:** *"Construyendo un plugin de IntelliJ para mi propio lenguaje — lecciones del Platform SDK"*
- **Post:** *"Qué aprendí compilando RPG (un lenguaje de los 70) a binarios modernos"*
- **Post:** *"Diseño de BBK: cómo modelé un lenguaje intermedio para business languages legacy"*
- **Post (con cuidado):** *"Vibecoding un BPMS mientras escribo un compilador 'a mano': dos disciplinas opuestas en paralelo"* — bridging post

Cada post genera tracción independiente. Espaciar cada 2-3 semanas.

---

## 13. Conferencias / publicaciones potenciales (largo plazo)

Para tener en el radar:

- **SLE (Software Language Engineering)** — exactamente el tipo de paper
- **MODELS** — Model-Driven Engineering, encaja
- **ICSE Software Engineering in Practice (SEIP)** track
- **IBM TechXchange Conference** — IBM Champions program
- **Argentina Symposium on Software Engineering (ASSE)**
- **JConf Argentina** — talk técnica local

No urgente, pero tener una versión "publicable" del trabajo te abre puertas académicas si querés el doctorado.
