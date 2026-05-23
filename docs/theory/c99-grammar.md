# C99 Grammar

**Source:** ISO/IEC 9899:1999 (C99), based on the public draft N1256 / Annex A.
**Purpose:** reference for the `bbk-compiler` module when lowering BBK to C99.

---

## Notation conventions

- `<non-terminal>` — syntactic category (production rule).
- `literal` — exact terminal (lexical token that appears verbatim in code).
- `a | b` — alternative: `a` or `b`.
- `a?` — optional (0 or 1 occurrences).
- `{ a }` — repetition (0 or more occurrences).
- `<a>_opt` — equivalent to `<a>?`, the standard's notation.
- `one of: x y z` — shorthand for `x | y | z` when they are standalone terminals.

---

## 1. Lexical grammar (tokens)

C99 tokens fall into six categories. They are what the preprocessor and the compiler consume.

### 1.1 General token

```
<token>
    : <keyword>
    | <identifier>
    | <constant>
    | <string-literal>
    | <punctuator>
```

### 1.2 Keywords

```
<keyword> one of:
    auto       break      case       char       const      continue
    default    do         double     else       enum       extern
    float      for        goto       if         inline     int
    long       register   restrict   return     short      signed
    sizeof     static     struct     switch     typedef    union
    unsigned   void       volatile   while      _Bool      _Complex
    _Imaginary
```

(`inline`, `restrict`, `_Bool`, `_Complex`, `_Imaginary` are C99-only.)

### 1.3 Identifiers

```
<identifier>
    : <identifier-nondigit>
    | <identifier> <identifier-nondigit>
    | <identifier> <digit>

<identifier-nondigit>
    : <nondigit>
    | <universal-character-name>

<nondigit> one of:
    _ a b c d e f g h i j k l m n o p q r s t u v w x y z
      A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

<digit> one of:
    0 1 2 3 4 5 6 7 8 9
```

### 1.4 Universal character names (C99)

```
<universal-character-name>
    : \u <hex-quad>
    | \U <hex-quad> <hex-quad>

<hex-quad>
    : <hex-digit> <hex-digit> <hex-digit> <hex-digit>
```

### 1.5 Constants

```
<constant>
    : <integer-constant>
    | <floating-constant>
    | <enumeration-constant>
    | <character-constant>
```

#### 1.5.1 Integer constants

```
<integer-constant>
    : <decimal-constant> <integer-suffix>?
    | <octal-constant>   <integer-suffix>?
    | <hexadecimal-constant> <integer-suffix>?

<decimal-constant>
    : <nonzero-digit>
    | <decimal-constant> <digit>

<octal-constant>
    : 0
    | <octal-constant> <octal-digit>

<hexadecimal-constant>
    : <hexadecimal-prefix> <hexadecimal-digit>
    | <hexadecimal-constant> <hexadecimal-digit>

<hexadecimal-prefix> one of:
    0x 0X

<nonzero-digit> one of:
    1 2 3 4 5 6 7 8 9

<octal-digit> one of:
    0 1 2 3 4 5 6 7

<hexadecimal-digit> one of:
    0 1 2 3 4 5 6 7 8 9 a b c d e f A B C D E F

<integer-suffix>
    : <unsigned-suffix> <long-suffix>?
    | <unsigned-suffix> <long-long-suffix>
    | <long-suffix> <unsigned-suffix>?
    | <long-long-suffix> <unsigned-suffix>?

<unsigned-suffix> one of:    u U
<long-suffix>     one of:    l L
<long-long-suffix> one of:   ll LL
```

#### 1.5.2 Floating constants

```
<floating-constant>
    : <decimal-floating-constant>
    | <hexadecimal-floating-constant>     // C99-only

<decimal-floating-constant>
    : <fractional-constant> <exponent-part>? <floating-suffix>?
    | <digit-sequence> <exponent-part> <floating-suffix>?

<hexadecimal-floating-constant>
    : <hexadecimal-prefix> <hexadecimal-fractional-constant>
          <binary-exponent-part> <floating-suffix>?
    | <hexadecimal-prefix> <hexadecimal-digit-sequence>
          <binary-exponent-part> <floating-suffix>?

<fractional-constant>
    : <digit-sequence>? . <digit-sequence>
    | <digit-sequence> .

<exponent-part>
    : e <sign>? <digit-sequence>
    | E <sign>? <digit-sequence>

<sign> one of:    + -

<digit-sequence>
    : <digit>
    | <digit-sequence> <digit>

<hexadecimal-fractional-constant>
    : <hexadecimal-digit-sequence>? . <hexadecimal-digit-sequence>
    | <hexadecimal-digit-sequence> .

<binary-exponent-part>
    : p <sign>? <digit-sequence>
    | P <sign>? <digit-sequence>

<hexadecimal-digit-sequence>
    : <hexadecimal-digit>
    | <hexadecimal-digit-sequence> <hexadecimal-digit>

<floating-suffix> one of:    f l F L
```

#### 1.5.3 Enumeration constants

```
<enumeration-constant>
    : <identifier>
```

(The identifier must be declared as a member of an `enum`.)

#### 1.5.4 Character constants

```
<character-constant>
    : ' <c-char-sequence> '
    | L' <c-char-sequence> '

<c-char-sequence>
    : <c-char>
    | <c-char-sequence> <c-char>

<c-char>
    : (any character from the source set except: ' \ and newline)
    | <escape-sequence>

<escape-sequence>
    : <simple-escape-sequence>
    | <octal-escape-sequence>
    | <hexadecimal-escape-sequence>
    | <universal-character-name>

<simple-escape-sequence> one of:
    \' \" \? \\ \a \b \f \n \r \t \v

<octal-escape-sequence>
    : \ <octal-digit>
    | \ <octal-digit> <octal-digit>
    | \ <octal-digit> <octal-digit> <octal-digit>

<hexadecimal-escape-sequence>
    : \x <hexadecimal-digit>
    | <hexadecimal-escape-sequence> <hexadecimal-digit>
```

### 1.6 String literals

```
<string-literal>
    : " <s-char-sequence>? "
    | L" <s-char-sequence>? "

<s-char-sequence>
    : <s-char>
    | <s-char-sequence> <s-char>

<s-char>
    : (any character from the source set except: " \ and newline)
    | <escape-sequence>
```

### 1.7 Punctuators

```
<punctuator> one of:
    [   ]   (   )   {   }   .   ->
    ++  --  &   *   +   -   ~   !
    /   %   <<  >>  <   >   <=  >=  ==  !=  ^   |   &&  ||
    ?   :   ;   ...
    =   *=  /=  %=  +=  -=  <<=  >>=  &=  ^=  |=
    ,   #   ##
    <:  :>  <%  %>  %:  %:%:     // digraphs
```

### 1.8 Comments

C99 allows both styles:

```
<comment>
    : /* (any character sequence except */) */
    | //  (any sequence up to newline)                // C99-only
```

---

## 2. Syntactic grammar (phrase structure)

### 2.1 Expressions

#### 2.1.1 Primary expressions

```
<primary-expression>
    : <identifier>
    | <constant>
    | <string-literal>
    | ( <expression> )
```

#### 2.1.2 Postfix expressions

```
<postfix-expression>
    : <primary-expression>
    | <postfix-expression> [ <expression> ]
    | <postfix-expression> ( <argument-expression-list>? )
    | <postfix-expression> . <identifier>
    | <postfix-expression> -> <identifier>
    | <postfix-expression> ++
    | <postfix-expression> --
    | ( <type-name> ) { <initializer-list> }            // C99 compound literal
    | ( <type-name> ) { <initializer-list> , }          // C99 compound literal

<argument-expression-list>
    : <assignment-expression>
    | <argument-expression-list> , <assignment-expression>
```

#### 2.1.3 Unary expressions

```
<unary-expression>
    : <postfix-expression>
    | ++ <unary-expression>
    | -- <unary-expression>
    | <unary-operator> <cast-expression>
    | sizeof <unary-expression>
    | sizeof ( <type-name> )

<unary-operator> one of:
    & * + - ~ !
```

#### 2.1.4 Cast expressions

```
<cast-expression>
    : <unary-expression>
    | ( <type-name> ) <cast-expression>
```

#### 2.1.5 Multiplicative expressions

```
<multiplicative-expression>
    : <cast-expression>
    | <multiplicative-expression> * <cast-expression>
    | <multiplicative-expression> / <cast-expression>
    | <multiplicative-expression> % <cast-expression>
```

#### 2.1.6 Additive expressions

```
<additive-expression>
    : <multiplicative-expression>
    | <additive-expression> + <multiplicative-expression>
    | <additive-expression> - <multiplicative-expression>
```

#### 2.1.7 Shift expressions

```
<shift-expression>
    : <additive-expression>
    | <shift-expression> << <additive-expression>
    | <shift-expression> >> <additive-expression>
```

#### 2.1.8 Relational expressions

```
<relational-expression>
    : <shift-expression>
    | <relational-expression> <  <shift-expression>
    | <relational-expression> >  <shift-expression>
    | <relational-expression> <= <shift-expression>
    | <relational-expression> >= <shift-expression>
```

#### 2.1.9 Equality expressions

```
<equality-expression>
    : <relational-expression>
    | <equality-expression> == <relational-expression>
    | <equality-expression> != <relational-expression>
```

#### 2.1.10 Bitwise AND / XOR / OR

```
<AND-expression>
    : <equality-expression>
    | <AND-expression> & <equality-expression>

<exclusive-OR-expression>
    : <AND-expression>
    | <exclusive-OR-expression> ^ <AND-expression>

<inclusive-OR-expression>
    : <exclusive-OR-expression>
    | <inclusive-OR-expression> | <exclusive-OR-expression>
```

#### 2.1.11 Logical AND / OR

```
<logical-AND-expression>
    : <inclusive-OR-expression>
    | <logical-AND-expression> && <inclusive-OR-expression>

<logical-OR-expression>
    : <logical-AND-expression>
    | <logical-OR-expression> || <logical-AND-expression>
```

#### 2.1.12 Conditional expression

```
<conditional-expression>
    : <logical-OR-expression>
    | <logical-OR-expression> ? <expression> : <conditional-expression>
```

#### 2.1.13 Assignment expression

```
<assignment-expression>
    : <conditional-expression>
    | <unary-expression> <assignment-operator> <assignment-expression>

<assignment-operator> one of:
    =  *=  /=  %=  +=  -=  <<=  >>=  &=  ^=  |=
```

#### 2.1.14 Expression (comma operator)

```
<expression>
    : <assignment-expression>
    | <expression> , <assignment-expression>
```

#### 2.1.15 Constant expression

```
<constant-expression>
    : <conditional-expression>
```

---

### 2.2 Declarations

```
<declaration>
    : <declaration-specifiers> <init-declarator-list>? ;

<declaration-specifiers>
    : <storage-class-specifier> <declaration-specifiers>?
    | <type-specifier>          <declaration-specifiers>?
    | <type-qualifier>          <declaration-specifiers>?
    | <function-specifier>      <declaration-specifiers>?

<init-declarator-list>
    : <init-declarator>
    | <init-declarator-list> , <init-declarator>

<init-declarator>
    : <declarator>
    | <declarator> = <initializer>
```

#### 2.2.1 Storage class specifiers

```
<storage-class-specifier> one of:
    typedef  extern  static  auto  register
```

#### 2.2.2 Type specifiers

```
<type-specifier>
    : void
    | char
    | short
    | int
    | long
    | float
    | double
    | signed
    | unsigned
    | _Bool                                 // C99
    | _Complex                              // C99
    | _Imaginary                            // C99
    | <struct-or-union-specifier>
    | <enum-specifier>
    | <typedef-name>
```

#### 2.2.3 Struct / union specifiers

```
<struct-or-union-specifier>
    : <struct-or-union> <identifier>? { <struct-declaration-list> }
    | <struct-or-union> <identifier>

<struct-or-union> one of:
    struct  union

<struct-declaration-list>
    : <struct-declaration>
    | <struct-declaration-list> <struct-declaration>

<struct-declaration>
    : <specifier-qualifier-list> <struct-declarator-list> ;

<specifier-qualifier-list>
    : <type-specifier> <specifier-qualifier-list>?
    | <type-qualifier> <specifier-qualifier-list>?

<struct-declarator-list>
    : <struct-declarator>
    | <struct-declarator-list> , <struct-declarator>

<struct-declarator>
    : <declarator>
    | <declarator>? : <constant-expression>
```

#### 2.2.4 Enum specifiers

```
<enum-specifier>
    : enum <identifier>? { <enumerator-list> }
    | enum <identifier>? { <enumerator-list> , }       // C99 trailing comma
    | enum <identifier>

<enumerator-list>
    : <enumerator>
    | <enumerator-list> , <enumerator>

<enumerator>
    : <enumeration-constant>
    | <enumeration-constant> = <constant-expression>
```

#### 2.2.5 Type qualifiers

```
<type-qualifier> one of:
    const  restrict  volatile
```

(`restrict` is C99-only.)

#### 2.2.6 Function specifiers

```
<function-specifier>
    : inline                                // C99
```

#### 2.2.7 Declarators

```
<declarator>
    : <pointer>? <direct-declarator>

<direct-declarator>
    : <identifier>
    | ( <declarator> )
    | <direct-declarator> [ <type-qualifier-list>? <assignment-expression>? ]
    | <direct-declarator> [ static <type-qualifier-list>? <assignment-expression> ]
    | <direct-declarator> [ <type-qualifier-list> static <assignment-expression> ]
    | <direct-declarator> [ <type-qualifier-list>? * ]      // C99 VLA
    | <direct-declarator> ( <parameter-type-list> )
    | <direct-declarator> ( <identifier-list>? )

<pointer>
    : * <type-qualifier-list>?
    | * <type-qualifier-list>? <pointer>

<type-qualifier-list>
    : <type-qualifier>
    | <type-qualifier-list> <type-qualifier>

<parameter-type-list>
    : <parameter-list>
    | <parameter-list> , ...

<parameter-list>
    : <parameter-declaration>
    | <parameter-list> , <parameter-declaration>

<parameter-declaration>
    : <declaration-specifiers> <declarator>
    | <declaration-specifiers> <abstract-declarator>?

<identifier-list>
    : <identifier>
    | <identifier-list> , <identifier>
```

#### 2.2.8 Type names and abstract declarators

```
<type-name>
    : <specifier-qualifier-list> <abstract-declarator>?

<abstract-declarator>
    : <pointer>
    | <pointer>? <direct-abstract-declarator>

<direct-abstract-declarator>
    : ( <abstract-declarator> )
    | <direct-abstract-declarator>? [ <type-qualifier-list>? <assignment-expression>? ]
    | <direct-abstract-declarator>? [ static <type-qualifier-list>? <assignment-expression> ]
    | <direct-abstract-declarator>? [ <type-qualifier-list> static <assignment-expression> ]
    | <direct-abstract-declarator>? [ * ]                   // C99 VLA
    | <direct-abstract-declarator>? ( <parameter-type-list>? )

<typedef-name>
    : <identifier>
```

#### 2.2.9 Initializers

```
<initializer>
    : <assignment-expression>
    | { <initializer-list> }
    | { <initializer-list> , }

<initializer-list>
    : <designation>? <initializer>                          // C99 designated init
    | <initializer-list> , <designation>? <initializer>

<designation>                                               // C99-only
    : <designator-list> =

<designator-list>
    : <designator>
    | <designator-list> <designator>

<designator>
    : [ <constant-expression> ]
    | . <identifier>
```

---

### 2.3 Statements

```
<statement>
    : <labeled-statement>
    | <compound-statement>
    | <expression-statement>
    | <selection-statement>
    | <iteration-statement>
    | <jump-statement>
```

#### 2.3.1 Labeled statements

```
<labeled-statement>
    : <identifier> : <statement>
    | case <constant-expression> : <statement>
    | default : <statement>
```

#### 2.3.2 Compound statement

```
<compound-statement>
    : { <block-item-list>? }

<block-item-list>
    : <block-item>
    | <block-item-list> <block-item>

<block-item>                                                // C99: free mixing
    : <declaration>
    | <statement>
```

(In C89 declarations had to go at the beginning of the block. In C99 they can be interleaved with statements.)

#### 2.3.3 Expression statement

```
<expression-statement>
    : <expression>? ;
```

#### 2.3.4 Selection statements

```
<selection-statement>
    : if ( <expression> ) <statement>
    | if ( <expression> ) <statement> else <statement>
    | switch ( <expression> ) <statement>
```

#### 2.3.5 Iteration statements

```
<iteration-statement>
    : while ( <expression> ) <statement>
    | do <statement> while ( <expression> ) ;
    | for ( <expression>? ; <expression>? ; <expression>? ) <statement>
    | for ( <declaration> <expression>? ; <expression>? ) <statement>    // C99
```

(C99 allows declaring the variable in the init clause of the `for`.)

#### 2.3.6 Jump statements

```
<jump-statement>
    : goto <identifier> ;
    | continue ;
    | break ;
    | return <expression>? ;
```

---

### 2.4 External definitions (top-level)

```
<translation-unit>
    : <external-declaration>
    | <translation-unit> <external-declaration>

<external-declaration>
    : <function-definition>
    | <declaration>

<function-definition>
    : <declaration-specifiers> <declarator> <declaration-list>? <compound-statement>

<declaration-list>
    : <declaration>
    | <declaration-list> <declaration>
```

(`<declaration-list>` before the body is only for the old-style K&R parameter declaration.)

---

## 3. Preprocessor grammar

Conceptually separate from the compiler. The preprocessor operates over tokens and produces tokens.

```
<preprocessing-file>
    : <group>?

<group>
    : <group-part>
    | <group> <group-part>

<group-part>
    : <if-section>
    | <control-line>
    | <text-line>
    | # <non-directive>

<if-section>
    : <if-group> <elif-groups>? <else-group>? <endif-line>

<if-group>
    : # if      <constant-expression> <new-line> <group>?
    | # ifdef   <identifier> <new-line> <group>?
    | # ifndef  <identifier> <new-line> <group>?

<elif-groups>
    : <elif-group>
    | <elif-groups> <elif-group>

<elif-group>
    : # elif <constant-expression> <new-line> <group>?

<else-group>
    : # else <new-line> <group>?

<endif-line>
    : # endif <new-line>

<control-line>
    : # include <pp-tokens> <new-line>
    | # define  <identifier> <replacement-list> <new-line>
    | # define  <identifier> ( <identifier-list>? ) <replacement-list> <new-line>
    | # define  <identifier> ( ... ) <replacement-list> <new-line>           // C99 variadic
    | # define  <identifier> ( <identifier-list> , ... ) <replacement-list> <new-line>
    | # undef   <identifier> <new-line>
    | # line    <pp-tokens> <new-line>
    | # error   <pp-tokens>? <new-line>
    | # pragma  <pp-tokens>? <new-line>
    | #         <new-line>

<text-line>
    : <pp-tokens>? <new-line>

<non-directive>
    : <pp-tokens> <new-line>

<replacement-list>
    : <pp-tokens>?

<pp-tokens>
    : <preprocessing-token>
    | <pp-tokens> <preprocessing-token>

<preprocessing-token>
    : <header-name>
    | <identifier>
    | <pp-number>
    | <character-constant>
    | <string-literal>
    | <punctuator>
    | (any non-whitespace character that is none of the above)
```

---

## 4. Semantic rules — synthesis

Semantic rules are not expressible in BNF. Summary of the most relevant categories of the C99 standard for BBK lowering:

### 4.1 Types (§6.2.5)

- **Basic types:** `char`, `signed/unsigned char`, `short`, `int`, `long`, `long long` (C99), `_Bool` (C99), `float`, `double`, `long double`, `_Complex`/`_Imaginary` (C99).
- **Derived types:** pointers, arrays, structs, unions, functions.
- **Enumerated types:** compatible with an underlying integer type (implementation-defined).
- **Qualified types:** `const`, `volatile`, `restrict` (C99). The qualifier alters behavior but not representation.

### 4.2 Conversions (§6.3)

- **Integer promotions:** `char`, `short`, and bitfields are promoted to `int` (or `unsigned int`) in arithmetic contexts.
- **Usual arithmetic conversions:** given a binary arithmetic operator, both operands are converted to a common type (hierarchy: `long double` > `double` > `float` > integer types by ranking).
- **Pointers:** `NULL` is a pointer constant of value zero. Any pointer to an object can be converted to `void *` and back without loss.

### 4.3 Lvalues, rvalues, array decay (§6.3.2)

- An **lvalue** is an expression with accessible storage (not necessarily modifiable; e.g. `const int x` is an lvalue but not modifiable).
- **Array decay:** except in specific contexts (`sizeof`, `&`, initialization), an array is automatically converted to a pointer to its first element.
- **Function decay:** a function identifier is automatically converted to a function pointer.

### 4.4 Sequence points (§6.5)

Points at which all prior side effects are completed:
- After the first operand of `&&`, `||`, `?:`, `,` (operator).
- At the end of each full expression (statement).
- When calling a function (after evaluating all arguments).
- When returning from a function.

Modifying the same object more than once between two sequence points is **undefined behavior** (e.g. `i = i++`).

### 4.5 Initialization (§6.7.8)

- `static` and global variables without an explicit initializer → initialized to zero.
- `auto` variables without an initializer → indeterminate.
- C99 allows designated initializers: `struct Foo f = { .x = 1, .y = 2 };` and `int a[10] = { [3] = 5 };`.
- C99 allows **compound literals**: `(struct Foo){ .x = 1 }` creates a temporary object.

### 4.6 Storage durations (§6.2.4)

- **Static:** lives for the entire execution of the program. Global and `static` local variables.
- **Automatic:** lives from entry to the block until exit. Local `auto` variables (default) and `register`.
- **Allocated:** lives between `malloc` and `free`.

### 4.7 Linkage (§6.2.2)

- **External:** visible across translation units (`extern`, default for functions and global variables).
- **Internal:** visible only within its translation unit (`static` at global scope).
- **No linkage:** local variables and parameters.

### 4.8 Function calls (§6.5.2.2)

- Arguments are evaluated in unspecified order (no sequence point between them, except as introduced by `&&`/`||`/`?:`/`,`).
- Argument types are converted to the declared type of the parameter (default argument promotions if no prototype is present).
- C99: calling a function with an implicit declarator (without a prior prototype) is no longer legal.

### 4.9 `inline` (C99, §6.7.4)

- Suggests to the compiler that the function be inlined.
- An `inline` function may coexist with an external definition (using `extern inline`).
- No inlining is guaranteed; the standard only defines the semantics.

### 4.10 Variable-length arrays (C99, §6.7.5.2)

- `int a[n]` where `n` is not a compile-time constant.
- Only allowed in automatic scope (locals).
- The size is evaluated upon entry to the block.

---

## 5. Key differences C99 vs C89

Summary of what C99 added (relevant for lowering from BBK):

| Feature | Reference |
|---|---|
| `//` comments | §6.4.9 |
| `long long`, `_Bool`, `_Complex`, `_Imaginary` types | §6.2.5 |
| `restrict` qualifier | §6.7.3 |
| `inline` function | §6.7.4 |
| Free mixing of declarations and statements in blocks | §6.8.2 |
| Declaration in the init clause of `for` | §6.8.5.3 |
| VLAs (variable-length arrays) | §6.7.5.2 |
| Designated initializers | §6.7.8 |
| Compound literals | §6.5.2.5 |
| Variadic macros (`...` in `#define`) | §6.10.3 |
| Hex floating constants (`0x1.fp3`) | §6.4.4.2 |
| `_Pragma` operator | §6.10.9 |
| New headers: `<stdbool.h>`, `<stdint.h>`, `<inttypes.h>`, `<tgmath.h>`, `<complex.h>`, `<fenv.h>` | §7 |

---

## References

- ISO/IEC 9899:1999 — *Programming languages — C* (official standard, paid).
- Public draft N1256 — committee draft of C99 with TC1, TC2, TC3 incorporated. The closest free reference to the final standard: https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1256.pdf
- Annex A of the standard contains the consolidated grammar (which is replicated here).
