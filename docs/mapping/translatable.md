# RPG → C translatable constructs

**Purpose:** identify constructs where RPG and C **differ** but the `bbk-compiler` can resolve the difference through explicit translation in the lowering, possibly leaning on general-purpose helper functions from `bbk-runtime` (not IBM i emulation).

The distinction from [`runtime-required.md`](runtime-required.md): the items in this file are translated with standard C plus, at most, numeric/string helpers. They don't need to model exotic IBM i operating-system concepts.

Related documents:
- [`similarities.md`](similarities.md) — direct 1:1 mapping
- [`runtime-required.md`](runtime-required.md) — not resolvable by translation alone

---

## 1. Exact decimal types: PACKED, ZONED, BINDEC

**The problem:** C has no exact decimal type in the standard language. `float`/`double` are binary and lose precision in decimal operations (classic: `0.1 + 0.2 != 0.3`). RPG uses packed/zoned decimal up to 63 digits with fixed scale.

**The translation:** implement `bbk_decimal_t` in a C library inside the runtime. Operations like add, subtract, multiply, divide, compare exposed as functions.

```rpg
DCL-S precio PACKED(7:2);
DCL-S iva   PACKED(7:2) INZ(1.21);
DCL-S total PACKED(9:2);

total = precio * iva;
```

```c
bbk_decimal_t precio = bbk_dec_zero(7, 2);
bbk_decimal_t iva   = bbk_dec_from_str("1.21", 7, 2);
bbk_decimal_t total = bbk_dec_zero(9, 2);

bbk_dec_mul(&total, &precio, &iva);
```

**Known alternatives for the library:** libdecimal, libgmp, or a custom implementation (BCD package).

**This is not IBM i execution, it's math.** That's why it goes here, not in runtime-required.

---

## 2. EVAL(H) — half-adjust

```rpg
EVAL(H) precio = total / cantidad;
```

```c
bbk_dec_div_round(&precio, &total, &cantidad, BBK_ROUND_HALF_UP);
```

Same pattern: the rounding rule goes as a flag to the decimal-library helper.

---

## 3. VARCHAR (variable-length strings)

**The problem:** RPG `VARCHAR(n)` stores current length + data. C has `char[]` with `\0` terminator.

**The translation:** struct with length prefix:

```c
typedef struct {
    uint16_t length;
    uint16_t capacity;
    char data[];        // flexible array member, C99
} bbk_varchar_t;
```

```rpg
DCL-S nombre VARCHAR(50);
nombre = 'Nicolas';
```

```c
bbk_varchar_t *nombre = bbk_varchar_alloc(50);
bbk_varchar_set(nombre, "Nicolas");
```

**Alternatively** (simpler, worse performance):

```c
typedef struct {
    uint16_t length;
    char data[51];      // capacity + 1
} bbk_varchar_50_t;     // one type per declared capacity
```

---

## 4. CHAR (fixed-length strings)

**The problem:** RPG `CHAR(n)` pads with blanks on the right up to length `n`. C `char[n]` has no such convention.

**The translation:** CHAR assignments pad explicitly:

```rpg
DCL-S codigo CHAR(5);
codigo = 'AB';
// codigo is now 'AB   ' (with 3 blanks)
```

```c
char codigo[5];
bbk_char_assign(codigo, 5, "AB");
// internally: strncpy + pad with ' ' up to 5
```

---

## 5. Date / Time / Timestamp

**The problem:** C has `time_t` (seconds since epoch) and `struct tm`, but does not directly support dates before 1970 or operations like "add 3 months".

**The translation:** custom struct with helpers:

```c
typedef struct { int16_t year; int8_t month; int8_t day; } bbk_date_t;
typedef struct { int8_t hour; int8_t min; int8_t sec; int32_t usec; } bbk_time_t;
typedef struct { bbk_date_t date; bbk_time_t time; } bbk_timestamp_t;

bbk_date_t bbk_date_add_days(bbk_date_t d, int32_t days);
int32_t    bbk_date_diff_days(bbk_date_t a, bbk_date_t b);
// etc.
```

BIF mapping:

| RPG BIF | C helper |
|---|---|
| `%DATE(s:fmt)` | `bbk_date_parse(s, fmt)` |
| `%DAYS(n)` | (returns a duration, internally an int32_t) |
| `%DIFF(a:b:fmt)` | `bbk_date_diff_X(a, b)` |

---

## 6. Indicators (legacy *IN01-*IN99 style)

**The problem:** the 99 numeric RPG indicators are global `'1'`/`'0'` booleans (character, not bit). Legacy opcodes set them implicitly. Legacy expressions do `*IN50` to read indicator 50.

**The translation:**

```c
// In bbk-runtime, declared in bbk-runtime.h:
extern bbk_indicator_t bbk_indicators[100];   // *IN00 to *IN99
extern bbk_indicator_t bbk_inlr;              // *INLR
// etc.

#define _IN(n) (bbk_indicators[n])
```

```rpg
IF *IN50 = *ON;
  // ...
ENDIF;
```

```c
if (_IN(50) == BBK_ON) {
    // ...
}
```

The opcodes that set indicators (`CHAIN`, `READ`, etc.) call helpers that update `bbk_indicators[]` as a side effect.

**In modern code (with BIFs `%FOUND`, `%EOF`, etc.):** the translation is cleaner — those BIFs return `bool` and the lowering emits `if (bbk_found(...))` without touching the indicator array.

---

## 7. SELECT / WHEN / OTHER → if/else chain

```rpg
SELECT;
WHEN tipo = 'A';
  procesarA();
WHEN tipo = 'B' OR tipo = 'C';
  procesarBC();
OTHER;
  procesarDefault();
ENDSL;
```

```c
if (bbk_char_eq(tipo, "A")) {
    procesarA();
} else if (bbk_char_eq(tipo, "B") || bbk_char_eq(tipo, "C")) {
    procesarBC();
} else {
    procesarDefault();
}
```

**It cannot be mapped to `switch`** because RPG `WHEN` takes an arbitrary boolean expression, not a value compared against the discriminant. Translation to a chained if/else is direct.

---

## 8. 1-indexed arrays → 0-indexed C arrays

```rpg
DCL-S nums INT(10) DIM(10);
nums(1) = 100;
nums(10) = 200;
```

```c
int32_t nums[10];
nums[0] = 100;     // nums(1) in RPG
nums[9] = 200;     // nums(10) in RPG
```

**Frontend design decision:** subtract 1 from the subscript during lowering. If the subscript is a variable, emit `nums[i - 1]`. Optional optimization: when it's a literal, compute at compile time.

**Uglier alternative, sometimes needed:** use C arrays of size N+1 and waste index 0. Easier for variable subscripts; worse memory usage.

---

## 9. Data structures with OVERLAY

```rpg
DCL-DS persona QUALIFIED;
  nombre   CHAR(50);
  apellido CHAR(50);
  fullName CHAR(101) POS(1) OVERLAY(persona);
END-DS;
```

```c
typedef struct {
    union {
        struct {
            char nombre[50];
            char apellido[50];
            char filler[1];     // to align to 101 total if needed
        };
        char fullName[101];
    };
} persona_ds_t;
```

C99 supports **anonymous unions** inside structs (a common extension since C11, but gcc accepts it as an extension in C99). Lets you keep the `persona.fullName` access with no extra prefix.

---

## 10. DCL-DS QUALIFIED and `.` access

```rpg
DCL-DS emp QUALIFIED;
  id   INT(10);
  nombre CHAR(50);
END-DS;

emp.id = 100;
```

```c
typedef struct {
    int32_t id;
    char nombre[50];
} emp_t;

emp_t emp;
emp.id = 100;
```

Direct mapping. C already has `.` for struct member access.

---

## 11. LIKE and LIKEDS — type inheritance

```rpg
DCL-S total LIKE(precio);                 // same type as precio
DCL-DS empClone LIKEDS(emp);              // same structure as emp
```

```c
// Resolved at compile time by the frontend:
bbk_decimal_t total;                      // copies the decimal type from precio
emp_t empClone;                           // alias of the emp typedef
```

**Frontend feature:** maintain a symbol table with types; when `LIKE` appears, substitute the resolved type.

---

## 12. BIFs translatable to C functions

Almost all BIFs are direct translations to runtime helper calls:

| BIF | C equivalent |
|---|---|
| `%TRIM(s)` | `bbk_trim(s)` |
| `%TRIML(s)` | `bbk_triml(s)` |
| `%TRIMR(s)` | `bbk_trimr(s)` |
| `%SUBST(s:start:len)` | `bbk_substr(s, start, len)` |
| `%SCAN(needle:hay)` | `bbk_scan(needle, hay)` |
| `%LEN(s)` | `bbk_len(s)` (for VARCHAR; for CHAR it's the known fixed size) |
| `%CHAR(n)` | `bbk_to_char(n)` (multiple overloads by type) |
| `%DEC(s)` | `bbk_to_dec(s, prec, scale)` |
| `%INT(n)`, `%INTH(n)` | `bbk_to_int(n)`, `bbk_to_int_round(n)` |
| `%ABS(n)` | `bbk_abs(n)` or `abs()`/`llabs()` for integers |
| `%ELEM(arr)` | compile-time constant (`sizeof(arr)/sizeof(arr[0])`) |
| `%ADDR(v)` | `&v` |
| `%SIZE(v)` | `sizeof(v)` or computed constant |

**Common pattern:** one BIF → one C function in the runtime library. The BIF's grammar is preserved as a function call. The only difference is RPG's `:` separator translated to C's `,`.

---

## 13. Initialization defaults

**RPG:** variables without `INZ` are auto-initialized:
- Numerics → 0
- Alphanumerics → blanks (`' '`)
- Date → `0001-01-01`
- Time → `00.00.00`

**C:** locals without an initializer are indeterminate. Globals and `static` are zero-initialized.

**The translation:** the frontend has to emit explicit initializers for local variables that are auto-initialized in RPG.

```rpg
DCL-S contador INT(10);            // 0 by default
DCL-S nombre   CHAR(50);           // blanks by default
```

```c
int32_t contador = 0;
char nombre[50];
memset(nombre, ' ', 50);           // or a helper bbk_char_clear(nombre, 50)
```

---

## 14. Numeric promotion / explicit conversions

RPG performs implicit conversions with defined decimal promotion rules (the result is computed at sufficient precision and adjusted to the target). C has its own rules but they differ (promotions to `int`, usual arithmetic conversions, etc.).

**The translation:** the frontend emits explicit conversions when needed to preserve RPG semantics.

```rpg
DCL-S x INT(10);
DCL-S y FLOAT(8);

x = y;     // float to int — RPG semantics: truncate (or round with (H))
```

```c
int32_t x;
double y;

x = (int32_t)y;          // truncate, equivalent semantics
// or:
x = (int32_t)bbk_round(y);    // if it was EVAL(H)
```

---

## 15. Free-form vs fixed-form → unified AST

**The problem:** the `rpg-frontend` receives code in three different forms (fully-free, mixed with `/FREE`, pure fixed-form). The lowering should work on **a single representation**.

**The translation:** the frontend normalizes all forms to the same AST. The parser detects the mode in the file header and dispatches to the appropriate sub-parser, but they all produce the same AST nodes.

It's not strictly an RPG → C translation; it's frontend work. But it's worth mentioning here because it removes a dimension of complexity before the lowering sees it.

---

## 16. Service program binding → linker

**RPG:** `*SRVPGM` are bound using *binding directories* that the compiler resolves while creating the `*PGM` or `*MODULE`.

**C:** standard linker (`gcc` links `.o` objects and `.so`/`.dll` libraries). Conceptual mapping:

| RPG | C (gcc + ld) |
|---|---|
| `*MODULE` | `.o` object file |
| `*PGM` | `.exe` |
| `*SRVPGM` | `.so` / `.dll` shared library |
| Binding directory | linker flags (`-l`, `-L`) |
| `EXPORT` | visible symbol (default, no `static`) |
| `IMPORT` | `extern` |

**It's not exactly the same** (no activation groups, different scope), but the model of separately compiled files linked together to produce an executable/library is analogous.

---

## 17. Multi-occurrence Data Structure (legacy OCCURS)

```rpg
DCL-DS reg OCCURS(100);
  campo1 INT(10);
  campo2 CHAR(20);
END-DS;
```

```c
typedef struct {
    int32_t campo1;
    char    campo2[20];
} reg_t;

reg_t reg[100];
int32_t reg_occur = 0;        // emulates RPG's OCCUR pointer
```

The legacy `OCCUR n reg` opcode translates to setting the index `reg_occur = n - 1`. Any access to `reg.campo1` translates to `reg[reg_occur].campo1`.

(Modern code avoids OCCURS and uses `DIM` with explicit indexed access — that case is already covered in §8.)

---

## 18. Embedded SQL (SQLRPGLE)

The statements `EXEC SQL ... END-EXEC` are **preprocessed** by the SQL precompiler before RPG compilation. They generate calls to SQLI APIs.

**Equivalent translation in C:** use an embedded SQL library (standard ESQL/C or equivalent). Ideally the frontend processes SQL statements in a separate phase and emits C code that uses a DB API.

**Pending design decision:** which DB engine `bbk-runtime` uses to emulate DB2/400. Options: SQLite (embedded, easy), PostgreSQL (more capabilities but requires a server). This leans more toward runtime-required, but the purely syntactic aspect of "EXEC SQL ... END-EXEC → API call" is translatable.

---

## Summary

Constructs that require mapping but are solvable by `bbk-compiler` + general-purpose C helpers:

- **Exact decimals** (packed/zoned) → decimal library
- **EVAL(H)** half-adjust → helper with rounding flag
- **VARCHAR / CHAR** with padding → structs / string helpers
- **Date / Time / Timestamp** → structs and helpers
- **Legacy indicators** → global boolean array
- **SELECT/WHEN/OTHER** → if/else chain
- **1-indexed arrays** → offset in the frontend
- **DS with OVERLAY** → union inside struct
- **DCL-DS QUALIFIED** → struct
- **LIKE / LIKEDS** → resolved in the frontend
- **BIFs** → runtime helper calls
- **Initialization defaults** → explicit initializers emitted by the frontend
- **Numeric conversions** → explicit casts with RPG rules
- **Free-form vs fixed-form** → unification to AST in the frontend
- **Service program binding** → linker
- **Multi-occurrence DS** → indexed array with a cursor
- **Embedded SQL** → DB API calls

All of this is "reasonable" work for a compiler. The associated runtime library (`bbk-runtime`) that supports these items is essentially a math/string/date library — it doesn't require emulating the IBM i operating system.
