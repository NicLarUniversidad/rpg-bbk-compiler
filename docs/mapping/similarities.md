# Similarities between RPG and C

**Purpose:** identify constructs that translate almost 1:1 between RPG and C99. The `bbk-compiler` can emit them directly during BBK → C lowering, with no additional runtime library or semantic changes needed.

Related documents:
- [`translatable.md`](translatable.md) — different but translatable with explicit mapping
- [`runtime-required.md`](runtime-required.md) — not translatable on its own; requires runtime

---

## 1. Arithmetic operators

| RPG | C | Notes |
|---|---|---|
| `+` | `+` | Addition |
| `-` | `-` | Subtraction (binary and unary) |
| `*` | `*` | Multiplication |
| `/` | `/` | Division (semantics differ by type — see translatable) |
| `**` | `pow(a, b)` | Exponentiation. C has no operator; requires `<math.h>`. Trivial mapping. |
| unary `+` | unary `+` | Identical |
| unary `-` | unary `-` | Identical |

**Associativity and precedence:** equivalent for basic numeric types. `**` in RPG is right-associative, same as nested `pow()` in C.

---

## 2. Relational operators

| RPG | C |
|---|---|
| `=` | `==` |
| `<>` | `!=` |
| `<` | `<` |
| `>` | `>` |
| `<=` | `<=` |
| `>=` | `>=` |

**Only change:** `=` in RPG is comparison; in C it's assignment. The frontend has to distinguish by context (in RPG the syntactic context makes it clear because assignment is via `EVAL` or via an `<lvalue> = <expr>` statement).

---

## 3. Logical operators

| RPG | C |
|---|---|
| `AND` | `&&` |
| `OR` | `\|\|` |
| `NOT` | `!` |

**Short-circuit evaluation:** both languages implement it the same way for `AND`/`&&` and `OR`/`||`.

---

## 4. Control structures

### 4.1 If / Else

```rpg
IF <cond>;
  <statements>;
ELSEIF <cond>;
  <statements>;
ELSE;
  <statements>;
ENDIF;
```

```c
if (<cond>) {
    <statements>;
} else if (<cond>) {
    <statements>;
} else {
    <statements>;
}
```

1:1 mapping. `ELSEIF` becomes `else if`.

### 4.2 FOR

```rpg
FOR i = 1 TO 10;
  <statements>;
ENDFOR;
```

```c
for (int i = 1; i <= 10; i++) {
    <statements>;
}
```

With `BY n`:

```rpg
FOR i = 0 TO 100 BY 5;
```

```c
for (int i = 0; i <= 100; i += 5) {
```

With `DOWNTO`:

```rpg
FOR i = 10 DOWNTO 1;
```

```c
for (int i = 10; i >= 1; i--) {
```

**Caveat:** RPG indexes from 1 by cultural convention but `FOR` does not enforce this. Arrays do (see translatable).

### 4.3 DOW (do while — pre-test)

```rpg
DOW <cond>;
  <statements>;
ENDDO;
```

```c
while (<cond>) {
    <statements>;
}
```

### 4.4 DOU (do until — post-test)

```rpg
DOU <cond>;
  <statements>;
ENDDO;
```

```c
do {
    <statements>;
} while (!(<cond>));
```

Heads up: `DOU` continues **until** the condition is true (it terminates when true). `do { } while` continues **while** the condition is true. Hence the `!` in the translation.

### 4.5 LEAVE / ITER

| RPG | C |
|---|---|
| `LEAVE;` | `break;` |
| `ITER;` | `continue;` |

### 4.6 RETURN

```rpg
RETURN;             // void return
RETURN expr;        // return value
```

```c
return;
return expr;
```

---

## 5. Basic numeric types

Some RPG numeric types map directly to standard C types:

| RPG | C |
|---|---|
| `INT(3)` | `int8_t` |
| `INT(5)` | `int16_t` |
| `INT(10)` | `int32_t` |
| `INT(20)` | `int64_t` |
| `UNS(3)` | `uint8_t` |
| `UNS(5)` | `uint16_t` |
| `UNS(10)` | `uint32_t` |
| `UNS(20)` | `uint64_t` |
| `FLOAT(4)` | `float` |
| `FLOAT(8)` | `double` |

**Available via `<stdint.h>`** (C99). The lowering can emit these types directly.

**Caveat:** the `PACKED`, `ZONED`, `BINDEC` types do **NOT** map directly — they are exact decimals. They go in [`translatable.md`](translatable.md).

---

## 6. Procedures and functions

### 6.1 Sub-procedure with return

```rpg
DCL-PROC sumar EXPORT;
  DCL-PI *N INT(10);
    a INT(10) CONST;
    b INT(10) CONST;
  END-PI;

  RETURN a + b;
END-PROC;
```

```c
int32_t sumar(int32_t a, int32_t b) {
    return a + b;
}
```

### 6.2 Sub-procedure without return

```rpg
DCL-PROC saludar;
  DCL-PI *N;
  END-PI;
  // ...
END-PROC;
```

```c
static void saludar(void) {
    // ...
}
```

(No `EXPORT` → `static` in C.)

### 6.3 Parameters

| RPG | C |
|---|---|
| `VALUE` | parameter by value (default in C) |
| no `VALUE` (by reference) | pointer (`T *`) |
| `CONST` | `const T` or `const T *` |

```rpg
DCL-PI proc;
  x INT(10) VALUE;        // by value
  y INT(10);              // by reference
  z INT(10) CONST;        // const, by reference
END-PI;
```

```c
void proc(int32_t x, int32_t *y, const int32_t *z) {
```

---

## 7. Constants

### 7.1 Literal constants

```rpg
DCL-C MAX_LEN 100;
DCL-C PI 3.14159;
DCL-C SALUDO 'Hola';
```

```c
#define MAX_LEN 100
#define PI 3.14159
static const char SALUDO[] = "Hola";
```

Or with C's `const`:

```c
static const int32_t MAX_LEN = 100;
static const double PI = 3.14159;
```

(Mapping to `#define` vs `const` is a style decision. `const` is more type-safe.)

### 7.2 Directly translatable figurative constants

| RPG | C |
|---|---|
| `*ON` | `1` (or `true` with `<stdbool.h>`) |
| `*OFF` | `0` (or `false`) |
| `*ZERO` / `*ZEROS` | `0` |
| `*NULL` | `NULL` |

(Other figurative constants like `*BLANK`, `*HIVAL`, `*LOVAL` depend on the target's type — they go in translatable.)

---

## 8. Storage classes

| RPG | C |
|---|---|
| `STATIC` (on a local variable) | `static` |
| Module-global variable | `static` (file-scope) by default; `extern` if `EXPORT` |
| `EXPORT` | (no `static`) — visible to the linker |
| `IMPORT` | `extern` |
| Local variable (default `AUTOMATIC`) | C local variable |

---

## 9. Identifiers

Similar lexical rules: start with a letter, then letters/digits/`_`. RPG adds `#`, `$`, `@` for EBCDIC compatibility. The frontend can transliterate them to `_` for C.

**Only material difference:** RPG is **case-insensitive**. Normalization to a canonical form (typically lowercase or uppercase) is done in the frontend. After that, C emission is trivial.

---

## 10. Comments

| RPG | C |
|---|---|
| `// comment to end of line` | `// comment to end of line` (C99) |
| `/* ... */` (not standard in RPG, but some editors accept it) | `/* ... */` |

Direct mapping.

---

## 11. Pointers (basic semantics)

```rpg
DCL-S p POINTER;
DCL-S x INT(10) BASED(p);

p = %ADDR(otroVar);
x = 42;                    // writes to *p treated as int32_t
```

```c
void *p;
int32_t *x_p;              // p typed at point of use

p = &otroVar;
x_p = (int32_t *)p;
*x_p = 42;
```

**Mapping limitation:** RPG with `BASED` allows dynamic re-aliasing that C requires explicit casts for. Basic pointer arithmetic (adding offsets) is similar.

---

## 12. Sequence points in expressions

C99 defines sequence points at `&&`, `||`, `?:`, `,`, end of full expression, and function calls.

RPG doesn't use the terminology, but its expression-evaluation behavior coincides at the practical points: `AND`/`OR` with short-circuit, generally left-to-right evaluation. RPG's simple expressions without side effects (RPG has no `++`/`--` and no assignments embedded in expressions) avoid the typical C undefined behaviors.

**Advantage for lowering:** RPG expressions are more restrictive than C's, so generating correct C from them is easier than the reverse direction.

---

## 13. Modularity

| RPG | C |
|---|---|
| Module (`*MODULE`) | Translation unit (`.c` + headers) |
| Program (`*PGM`) — binds one or more modules | Executable (`.exe`) — links one or more `.o` |
| Service program (`*SRVPGM`) — binds modules for shared use | Shared library (`.dll` / `.so`) — links shareable modules |
| Main procedure | `int main(void)` or `int main(int argc, char *argv[])` |

**Caveat:** the `*PGM` ↔ `*MODULE` ↔ `*SRVPGM` relationship with activation groups has semantic complexities that do not map cleanly. The **file** structure does.

---

## Summary

What maps directly between RPG and C99:

- **Expression syntax:** arithmetic, relational, logical operators.
- **Control syntax:** `IF`/`FOR`/`DOW`/`DOU`/`LEAVE`/`ITER`/`RETURN`.
- **Integer and floating-point numeric types** (`INT`, `UNS`, `FLOAT`).
- **Procedures with parameters and return.**
- **Storage classes** (`STATIC`, `EXPORT`).
- **Literal constants** (`DCL-C`).
- **Identifiers** (modulo case and special characters).
- **Comments.**
- **Basic pointers.**
- **File-level modularity** (module, program, srvpgm).

For the `bbk-compiler`, this set is the easy part of the lowering: it emits standard C without needing the `bbk-runtime` library.
