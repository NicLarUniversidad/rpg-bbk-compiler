# Constructs not resolvable by translation

**Purpose:** identify the aspects of RPG / IBM i where the gap with C + Windows/Linux is **structural**, not syntactic. There's no way to close the gap by emitting C code. One (or more) of these is needed:

1. **A substantial emulation library** inside `bbk-runtime` that models IBM i operating-system primitives.
2. **External components** (embedded DB engine, terminal emulator, scheduler).
3. **Design decisions** that assume reduced or alternative semantics when the original isn't replicable.

The distinction from [`translatable.md`](translatable.md): the items in this file aren't solved with a math or string helper. They are about the **computation model** and the **operating system** that RPG assumes and that C + Windows/Linux don't provide.

Related documents:
- [`similarities.md`](similarities.md) — direct mapping
- [`translatable.md`](translatable.md) — different but translatable

---

## 1. RPG Cycle (implicit main loop)

**What it is:** RPG programs with a "primary" file (P in F-spec) run an **implicit runtime cycle**:

1. Read the next record from the primary file
2. Process control breaks (`L1`-`L9` level breaks)
3. Execute detail calculations (C-specs without restriction)
4. Process output
5. If `*INLR = *ON` → terminate; otherwise go back to step 1

**Why it's not translatable:** the control flow is not in the source code. **It's provided by the runtime.** The programmer writes calculations assuming "someone" handles reading, processing breaks, doing output, etc. A C mapping cannot ignore this because it would change what runs and when.

**What it requires:**

- `bbk-runtime` must provide a **cycle dispatcher** that the generated code invokes.
- `bbk-compiler` has to detect whether the source program uses the cycle (declared primary files, level breaks, `*INLR` indicators) and emit a call to the dispatcher instead of a linear `main`.
- Programs marked `NOMAIN` or pure procedures don't need this — they're normal C functions.

**Design implication:** two code-generation modes:
- "Linear main" mode — for modern programs with no cycle
- "Main with cycle" mode — emits `int main()` that calls `bbk_cycle_run(<config>)`, where `config` describes the primary files, breaks, etc.

---

## 2. Activation Groups

**What it is:** an IBM i execution context that isolates resources between programs. Each activation group has its own:

- Set of open files
- Static storage (`STATIC` variables per activation group)
- Commitment control boundary
- Active job overrides
- Error handlers

Programs can run in `*DFTACTGRP`, `*CALLER`, or in a named activation group.

**Why it's not translatable:** nothing equivalent exists in POSIX or Windows. Processes share a single global context of open files per process. C `static` is global to the process, not to a sub-scope.

**What it requires:**

- `bbk-runtime` needs its own **activation group manager**: a level of indirection between RPG code and OS APIs.
- Each activation group is a struct with its open-file table, static storage pool, etc.
- File operations and accesses to `STATIC` go through this manager instead of calling the OS directly.

**Realistic trade-off:** supporting activation groups with full fidelity is significant work. A minimal version could have a single implicit activation group (`*DFTACTGRP`) and leave multi-actgrp fidelity for a future iteration. Programs that don't explicitly depend on multi-actgrp would work just the same.

---

## 3. Library list

**What it is:** an ordered list of "libraries" (IBM i object namespaces) that the runtime consults to resolve unqualified object name references. It has several sections:

- System libraries (`QSYS`, etc.)
- Product libraries
- Current library
- User libraries (those that the job adds via `ADDLIBLE`)

When a program does `CHAIN ... CUSTOMER` (without qualifying the library), the runtime looks up `CUSTOMER` in each library on the list until it finds it.

**Why it's not translatable:** neither Windows nor Linux has a mechanism equivalent to "look up this symbol in an ordered list of namespaces at runtime". The filesystem is flat (with absolute or cwd-relative paths), and there is no search path for DB tables or for procedures.

**What it requires:**

- A **library-list implementation inside `bbk-runtime`** that maps "object name" → "file path / SQL table name / procedure reference".
- APIs equivalent to `ADDLIBLE`, `RMVLIBLE`, `CHGLIBL` so code can manipulate it.
- Persistence: the library list is job state, not OS process state.

**Design decision:** model "libraries" as on-disk directories + tables in an embedded DB. To resolve `CUSTOMER`, look first for a `CUSTOMER.dat` file in each directory on the list, or for a `CUSTOMER` table in the DB at the corresponding scope.

---

## 4. Record-level file access (DB2/400)

**What it is:** RPG accesses physical files (PF) and logical files (LF) of DB2/400 with low-level primitives:

- `READ` — next sequential record
- `READE key` — next with matching key
- `CHAIN key` — random access by key
- `WRITE` — append record
- `UPDATE` — update the last one read
- `DELETE` — delete the last one read
- `SETLL` / `SETGT` — position the cursor

Files have an external description (DDS) that defines the fields.

**Why it's not translatable (directly):** standard SQL does not expose cursor primitives at that granularity. The "read next, update what you just read" philosophy is closer to ISAM (a '70s model) than to modern SQL.

**What it requires:**

- An **embedded DB engine** inside the runtime that provides record-level access. Options:
  - **SQLite** — embedded, no server. Cursor-based access can be emulated on top of SQL cursors.
  - **A custom ISAM-style implementation** — more faithful but a lot of work.
  - **Hybrid** — SQLite for storage, a custom layer that exposes `CHAIN`/`READ`/`UPDATE`-style primitives on top of SQLite cursors.
- **External schemas** (DDS) must map to SQL table definitions + runtime metadata so the compiler knows field names and types.

**Massive implication:** this point alone is a project in itself. The choice of DB engine and the modeling of keyed access is probably the biggest technical decision in the project after the IR.

---

## 5. DDS (Data Description Specifications)

**What it is:** a language separate from RPG that describes file schemas. Defines fields, keys, joins, validations, presentation formats. Compiled to `*FILE` objects that RPG references via `EXTNAME`.

**Why it's not translatable:** DDS is its own language, not part of RPG. RPG only consumes it.

**What it requires:**

- Either an **embedded DDS compiler** in the project that converts DDS to:
  - SQL table definitions for the chosen DB engine
  - Metadata so `rpg-frontend` can resolve `EXTNAME` and know the fields
- Or **decide that the project only supports native SQL schemas**, not DDS. Simpler, less faithful.

**Scope decision:** supporting DDS in full is a huge sub-project. For MVP, support **basic physical files defined in SQL** (CREATE TABLE) that RPG code accesses via `EXTNAME`. DDS goes on the roadmap.

---

## 6. Display files (5250 screens)

**What it is:** files of type "display file" (DSPF) that define screens for 5250 terminals (IBM i's standard terminal). RPG accesses them with `EXFMT`, `WRITE`/`READ` to a WORKSTN.

5250 characteristics:
- Layout based on fields at screen positions (rows × columns)
- Per-field editing, local validation
- Function keys (F1-F24) and attention keys
- Subfiles (paginated lists with cursor)

**Why it's not translatable:** Windows/Linux have no native equivalent of 5250. The "the program reads/writes a whole screen" paradigm doesn't look like stdin/stdout or a modern GUI.

**What it requires:**

- A **5250 emulation library** (or adapter to an existing emulator like `tn5250`).
- Or rendering to a **pseudo-screen in the console** (ncurses-like) for a minimal version.
- Or rewriting display files to a modern technology (HTML + server, or native GUI) — but that deviates from the "RPG runs unchanged" promise.

**Realistic MVP scope decision:** support only batch programs (no WORKSTN). Display files are deferred to a later phase, possibly with an external emulator.

---

## 7. Printer files and spool

**What it is:** printer output goes to a *spool file* in an *output queue* (OUTQ). The program does `WRITE` to a printer file (PRTF), the spool file stays in the queue, the operator releases it for printing, reprints it, or deletes it.

**Why it's not translatable:** Windows has printer queues but the model is different. Linux has no native equivalent (CUPS covers it but requires setup).

**What it requires:**

- A **spool emulation** inside `bbk-runtime` — a directory where outputs are saved as files, with metadata (status, date, user, etc.).
- APIs equivalent to `WRKSPLF`, `DLTSPLF`, etc., or deciding that we only support "print = generate file in /spool/<user>/<timestamp>.txt".

**Scope decision:** minimal version — `WRITE` to a printer file generates a `.txt` in a configurable directory. No metadata, no queue, no OUTQs. Enough for batch programs that generate reports.

---

## 8. IBM i objects (*PGM, *MODULE, *SRVPGM, *FILE, *DTAARA, *MSGF, *USRPRF, *JOBD, etc.)

**What it is:** IBM i has a **unified** namespace of objects. Every object has a type, name, owning library, authority, attributes. The filesystem and "objects" are the same thing from the OS's perspective.

Relevant types:

- `*PGM` — executable program
- `*MODULE` — compiled but not bound module
- `*SRVPGM` — service program (shared library)
- `*FILE` — data file (PF, LF, DSPF, PRTF, etc.)
- `*DTAARA` — data area (see §10)
- `*MSGF` — message file
- `*USRPRF` — user profile
- `*JOBD` — job description
- ... and ~100 more

**Why it's not translatable:** Windows/Linux do not have a typed unified namespace. They have a filesystem (files and directories) and separate namespaces for users, services, etc.

**What it requires:**

- Decide on a **mapping model**: how we represent each object type.
  - `*PGM`, `*SRVPGM` → executables and shared libraries (natural mapping)
  - `*FILE` (data) → SQL tables + metadata (related to §4)
  - `*DTAARA` → files on disk (related to §10)
  - `*MSGF` → files with messages (related to §13)
  - `*USRPRF` → OS users? A custom table? — pending decision
- A **naming and path convention** inside `bbk-runtime` that respects the "library" as a sub-directory or sub-schema.

**Scope decision:** a complete mapping is ambitious. MVP: support `*PGM`, `*SRVPGM`, `*MODULE` and `*FILE` (data). The rest is deferred.

---

## 9. Single-level Store (SLS)

**What it is:** IBM i's unified memory model. All memory — RAM and disk — is in the same addressable space. "Variables" on disk are accessed with the same semantics as those in memory. Conceptually, there's no distinction between "reading a file" and "reading a variable".

**Why it's not translatable:** no equivalent in any modern OS. It's a unique IBM i feature (inherited from System/38).

**What it requires:** **we don't emulate it.** This is a case where the sane decision is not to try to replicate the semantics. RPG programs that subtly depend on SLS (use of pointers that cross memory/disk boundaries, automatic persistence of variables) will require manual refactoring or won't work.

**Implication:** document this limit explicitly. Programs with "exotic" code that depends on SLS are not supported targets.

---

## 10. Data areas (*DTAARA)

**What it is:** named persistent storage. Has a type (CHAR, DEC, LGL) and declared length. Lives across program invocations. APIs: `*LOCK DTAARA`, `IN`/`OUT` for read/write, `UNLOCK`.

**Why it's not translatable:** C has `static` variables that persist within the process, but data areas **persist across processes**. When a process terminates, its `static` is lost. Data areas are not.

**What it requires:**

- Implement data areas as **files on disk** inside `bbk-runtime`, with:
  - Filesystem locking for `*LOCK`
  - Value serialization according to the declared type
  - APIs `bbk_dtaara_in()`, `bbk_dtaara_out()`, `bbk_dtaara_lock()`, etc.
- Decide where they live (a configurable directory like `~/.boxbreaker/dtaara/`).

**Reasonably simple** compared to other items in this file. I put it here and not in translatable because it requires cross-process persistence, which is a runtime primitive, not a math function.

---

## 11. Authority / per-object security

**What it is:** IBM i has a **OS-integrated** security system that assigns authority per object (READ, ADD, UPDATE, DELETE, etc.) to users and groups. RPG assumes the OS validates access when running `CHAIN`, `UPDATE`, etc., and if it doesn't have authority, it fails.

**Why it's not translatable:** Windows/Linux have filesystem permissions (rwx) but not at the DB-table level or for specific operations (UPDATE vs DELETE, for example, are the same at the POSIX "write" level).

**What it requires:**

- **Decision:** ignore authority (code runs with full permissions) in an initial version.
- Full version: emulate an authority table in `bbk-runtime`, validate before each operation. A lot of work.

**Realistic MVP:** ignore authority. Document that the security model isn't preserved.

---

## 12. Job queues and subsystems

**What it is:** IBM i has **subsystems** (work managers) that take jobs from **job queues** and run them according to priorities, profiles, etc. `SBMJOB` submits a job to a queue.

**Why it's not translatable:** no modern OS has a native equivalent. There are schedulers (cron, systemd timers, Windows Task Scheduler) and queues (RabbitMQ, etc.) but none match the "subsystem takes jobs and runs them" semantics.

**What it requires:**

- Decide whether to support `SBMJOB`. If yes: implement a mini-scheduler in the runtime (probably an on-disk queue + worker process).
- If no: document as unsupported.

**Realistic MVP:** don't support. Most RPG code that people want to modernize is batch or synchronous interactive, not job orchestration.

---

## 13. Message handling (CL-style messages)

**What it is:** RPG programs send and receive **messages** from the IBM i messaging system (`SNDPGMMSG`, `RCVMSG`). Messages live in a **message file** with identifiers and parameterizable texts. They have severity, type (info, inquiry, escape, etc.), and are placed on the job's **message queue**.

**Why it's not translatable:** stderr/stdout is not equivalent. Linux has `syslog` but the severity and queue model are different.

**What it requires:**

- A **messaging library** in the runtime — emulation of per-job (process) message queues, serialization to stdout/stderr or to a log file.
- If fidelity is needed: implementation of message files as JSON/YAML files with identifiers and texts.

**Realistic MVP:** message functions become `fprintf(stderr, ...)` calls with a prefix by severity. Enough for most use cases.

---

## 14. *PSSR and program exception handling

**What it is:** a special `*PSSR` subroutine that runs if the program receives an unhandled exception (division by zero, file error, etc.). It's program-level, not per-block.

**Why it's not directly translatable:** C has no exceptions. It has `setjmp`/`longjmp` (rudimentary) and `signal` (for OS signals).

**What it requires:**

- Implement `*PSSR` with `setjmp`/`longjmp` + signal handlers in the runtime.
- Opcodes that can fail (division, file ops) check errors and `longjmp` to the registered `*PSSR` handler if it's active.

**Reasonable to implement** but requires cross-cutting infrastructure in the generated code.

---

## 15. Commitment control

**What it is:** transactions at the activation-group level. `COMMIT` and `ROLBK` (rollback). Defines which updates are made atomic together.

**Why it's not translatable:** depends on the chosen DB engine. SQLite has transactions, but the "transaction per activation group" model is IBM i-specific.

**What it requires:** the runtime's DB engine (§4) must support transactions. The activation-group manager (§2) must open a transaction on entry into a commit-controlled actgrp and commit/rollback it as appropriate.

---

## 16. Time-of-day / job attributes

**What it is:** RPG accesses job attributes (user, system date, current library, etc.) via PSDS, BIFs like `%TIMESTAMP`, etc. Many assume a model "this program runs inside an IBM i job".

**Why it requires a runtime:** most are translatable (date → `time()`, user → `getenv("USER")` or equivalent), but some have no direct equivalent (job number, sub-system, active library list) or have artificial values.

**What it requires:**

- The runtime must simulate a "job context" with reasonable values: synthetic job name, number, current library, library list, user, etc.
- When a program reads PSDS or calls job-info BIFs, the runtime returns values from the simulated context.

---

## Summary — the runtime map

For RPG → C lowering to work seriously, `bbk-runtime` must implement (in a reasonable order of priority):

| Component | Complexity | Needed for |
|---|---|---|
| Cycle dispatcher | Medium | RPG programs with `*INLR` and primary files |
| Decimal arithmetic (from translatable) | Medium | Almost any RPG program |
| Record-level file access (DB engine) | **High** | Any program that touches the DB |
| Library list | Medium | Resolution of unqualified names |
| `*PSSR` and exception handling | Medium | Programs with legacy error handling |
| Data areas | Low | Programs that use simple persistence |
| Spool / printer files | Low-Medium | Reporting programs |
| Activation groups | **High** | Programs that depend on resource isolation |
| Display files (5250) | **Very high** | Interactive programs (lots of legacy code) |
| Job queues / scheduler | High | Programs that submit jobs |
| Authority | Medium | Security fidelity |
| Commitment control | Medium | Transactional programs |
| Single-level store | Impossible | (not supported — documented limitation) |

**What remains as "documented unsupported":**
- Single-level store (semantically incompatible)
- Authority/security model (defer to the OS or ignore)
- Some exotic job-context attributes

**MVP scope decision for the BoxBreaker project:**

Implement the medium-low complexity items first:
1. Cycle dispatcher
2. Decimal arithmetic (translatable)
3. Data areas
4. `*PSSR`
5. Spool / printer (minimal version)

Defer to later phases:
- DB engine (parallel to the above because it's blocking for almost everything)
- Activation groups (minimal version: just one)
- Display files (could be phase 2 or 3)

This defines what RPG subset the system supports at each milestone.
