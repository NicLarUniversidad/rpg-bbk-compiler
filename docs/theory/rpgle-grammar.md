# RPG IV / ILE RPG (RPGLE) Grammar

**Language:** RPG IV, also called **ILE RPG** or **RPGLE** (after the `.RPGLE` file extension on IBM i / AS/400).
**Reference compiler:** IBM ILE RPG Compiler (part of ILE — Integrated Language Environment).
**Platform:** IBM i (formerly OS/400, previously AS/400).

---

## Note on the nature of this grammar

Unlike C99, **RPG has no ISO standard**. The authoritative language specification is IBM's official documentation:

- *ILE RPG Language Reference* (publication SC09-2508 or successors)
- *ILE RPG Programmer's Guide* (SC09-2507 or successors)

That documentation describes the syntax in **prose + railroad diagrams**, not in BNF. The BNF grammar that follows is a **reasoned reconstruction** from those sources, sufficient to implement a parser but not equivalent to a formal standard. It is the best that can be obtained: there is no officially published BNF for the language.

---

## Notation conventions

Same format as in [`c99-grammar.md`](c99-grammar.md):

- `<non-terminal>` — syntactic category.
- `literal` — exact terminal.
- `a | b` — alternative.
- `a?` — optional.
- `{ a }` — repetition (0 or more).
- `one of: x y z` — simple alternative between terminals.

---

## 1. General structure of an RPG program

An RPG module is made up of a sequence of **specifications** (or "specs"). Each spec has a type identified by a letter. Historically all of them were fixed-form (fixed columns on 80-character lines). Since **RPG IV V5R1** there is free-form for calculations (`/FREE` ... `/END-FREE`), and since **7.1 TR7** there is **fully free-form** (with the `**FREE` directive at the start of the file) which removes fixed columns for all specs.

### 1.1 Types of specifications

| Letter | Name | Role |
|---|---|---|
| **H** | Header / Control | Global program options (data type defaults, optimization, debug) |
| **F** | File description | Files used (input, output, update, combined) |
| **D** | Definition | Variables, constants, data structures, prototypes |
| **I** | Input | (Legacy) input record descriptions |
| **C** | Calculation | Calculation logic (legacy fixed-form or free-form `/FREE`) |
| **O** | Output | (Legacy) output record descriptions |
| **P** | Procedure | Delimits a sub-procedure |

### 1.2 Syntax modes

```
<source-file>
    : <fully-free-source>             // **FREE on line 1
    | <mixed-form-source>             // mixed fixed-form + /FREE blocks in C-specs
    | <fixed-form-source>             // all fixed-form (pure legacy)
```

#### 1.2.1 Fully free-form (modern, >=7.1 TR7)

```
<fully-free-source>
    : **FREE <newline> <free-form-statement-list>

<free-form-statement-list>
    : <free-form-statement>
    | <free-form-statement-list> <free-form-statement>
```

(In this mode there are no fixed columns; statements end with `;`.)

#### 1.2.2 Mixed-form (living legacy)

Combination: H/F/D/P in fixed-form, logic in `/FREE` blocks.

```
<mixed-form-source>
    : <fixed-spec-section>?
      { /FREE <free-form-statement-list> /END-FREE }*
      <fixed-spec-section>?
```

#### 1.2.3 Fixed-form (pure legacy)

Each line has 80 columns, column 6 indicates the spec type, and the remaining columns have positional meaning. Detailed in §3.

---

## 2. Lexicon (common to all modes)

### 2.1 General token

```
<token>
    : <reserved-word>
    | <opcode>
    | <built-in-function>
    | <identifier>
    | <literal>
    | <punctuator>
    | <operator>
```

### 2.2 Identifiers (names)

```
<identifier>
    : <letter> { <letter> | <digit> | _ | # | $ | @ }*

<letter> one of:
    A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
    a b c d e f g h i j k l m n o p q r s t u v w x y z

<digit> one of:
    0 1 2 3 4 5 6 7 8 9
```

**Notes:**
- RPG is **case-insensitive** for identifiers and reserved words.
- Maximum length: 4096 characters (in practice, legacy identifiers are up to 6 or 10 characters for fixed-form compatibility).
- The characters `#`, `$`, `@` are accepted for historical compatibility with EBCDIC encodings.

### 2.3 Reserved words (declaration keywords, free-form)

```
<reserved-word> one of:
    CTL-OPT       DCL-S         DCL-C         DCL-DS
    DCL-PR        DCL-PI        DCL-F         DCL-PROC
    END-DS        END-PR        END-PI        END-PROC
    BEGSR         ENDSR         EXSR
    IF            ELSE          ELSEIF        ENDIF
    SELECT        WHEN          OTHER         ENDSL
    DOW           ENDDO         DOU           DOWEQ
    FOR           ENDFOR        TO            DOWNTO       BY
    ITER          LEAVE         LEAVESR       RETURN
    MONITOR       ON-ERROR      ON-EXIT       ENDMON
    INZ           BASED         POS           OVERLAY
    LIKE          LIKEDS        LIKEREC       TEMPLATE
    EXPORT        IMPORT        STATIC        AUTO
    OPDESC        OPTIONS       RTNPARM       CONST       VALUE
    GLOBAL        QUALIFIED     ALIGN         EXTPGM      EXTPROC
    PSDS          INFDS         INDARA
    USROPN        DISK          PRINTER       WORKSTN     SEQ
    USAGE         RENAME        PREFIX        EXTNAME     EXTFILE
```

(Not exhaustive; IBM adds keywords between releases.)

### 2.4 Opcodes (operation codes)

Operation codes of the language. The most used ones are alive in free-form; some exist only in fixed-form C-spec.

```
<opcode> one of:
    // Arithmetic (legacy)
    ADD        SUB        MULT       DIV        SQRT       MVR
    Z-ADD      Z-SUB

    // Assignment
    EVAL       EVALR      EVAL-CORR  MOVE       MOVEL      MOVEA
    CLEAR      RESET

    // Flow control
    IF         ELSE       ELSEIF     ENDIF
    SELECT     WHEN       OTHER      ENDSL
    FOR        ENDFOR     DOW        DOU        ENDDO
    DO         END        ITER       LEAVE
    GOTO       TAG                                    // legacy
    CASxx                                             // legacy comparison branch
    RETURN

    // Subroutines and procedures
    BEGSR      ENDSR      EXSR       LEAVESR
    CALLP      CALL       CALLB                       // CALL/CALLB legacy
    PARM       PLIST                                  // legacy parameter list

    // Error handling
    MONITOR    ON-ERROR   ENDMON
    ON-EXIT                                           // >=7.5

    // Files
    READ       READE      READP      READPE     CHAIN
    WRITE      UPDATE     DELETE     UNLOCK
    OPEN       CLOSE      FEOD       SETLL      SETGT
    EXFMT      EXCEPT
    POST       NEXT       ACQ        REL

    // Strings (legacy; BIFs are now used)
    CAT        SUBST      SCAN       XLATE      CHECK     CHECKR

    // Indicators and bits
    BITON      BITOFF     TESTB

    // Date/Time (legacy)
    ADDDUR     SUBDUR     EXTRCT     TIME

    // Miscellaneous
    DSPLY      DUMP       SHTDN      DEBUG     IN        OUT
    SORTA      LOOKUP     DEFINE     KFLD     KLIST
    OCCUR
```

(Representative list; the IBM reference maintains the complete enumeration.)

### 2.5 Built-in Functions (BIFs)

All begin with `%`. They replace legacy opcodes in modern code.

```
<built-in-function>
    : % <identifier>

// The most common, grouped by category:

// Numeric
%ABS        %DIV        %REM        %INT        %INTH
%UNS        %UNSH       %FLOAT      %DEC        %DECH
%DECPOS     %INTH       %SIGNED     %SQRT

// Strings
%CHAR       %CHECK      %CHECKR     %EDITC      %EDITW
%LEN        %LOWER      %UPPER      %REPLACE    %SCAN
%SCANR      %SPLIT      %STR        %SUBST      %TRIM
%TRIML      %TRIMR      %XLATE      %CONCAT     %CONCATARR

// Date/Time
%DATE       %DAYS       %DIFF       %HOURS      %MINUTES
%MONTHS     %MSECONDS   %SECONDS    %SUBDT      %TIME
%TIMESTAMP  %YEARS

// Arrays / Tables
%ELEM       %LOOKUP     %LOOKUPLT   %LOOKUPGE   %LOOKUPGT
%LOOKUPLE   %TLOOKUP    %XFOOT      %SUBARR     %SORTA

// Files
%EOF        %EQUAL      %ERROR      %FOUND      %OPEN
%STATUS     %SIZE       %PARMS      %PARMNUM    %PARMSNUM

// Pointers and addresses
%ADDR       %ALLOC      %REALLOC    %TYPEOF
%ELEM       %SIZE

// Conversion / format
%BITAND     %BITOR      %BITXOR     %BITNOT
%CHAR       %DEC        %INT        %FLOAT      %BIN

// Others
%NULL       %NULLIND    %HANDLER    %PROC       %THIS
%KDS        %TIMESTAMP
```

(IBM adds BIFs in each release; list not exhaustive.)

### 2.6 Literals

```
<literal>
    : <character-literal>
    | <numeric-literal>
    | <hex-literal>
    | <date-literal>
    | <time-literal>
    | <timestamp-literal>
    | <ucs2-literal>
    | <graphic-literal>
    | <indicator-literal>

<character-literal>
    : ' { <char-or-escape> }* '

<char-or-escape>
    : (any character except unescaped ')
    | ''                              // escaped quote

<numeric-literal>
    : <digit-sequence> ( . <digit-sequence> )?      // decimal, packed or zoned depending on context
    | -<digit-sequence> ( . <digit-sequence> )?
    | +<digit-sequence> ( . <digit-sequence> )?

<digit-sequence>
    : <digit>
    | <digit-sequence> <digit>

<hex-literal>
    : X' <hex-digit-pairs> '

<hex-digit-pairs>
    : <hex-digit> <hex-digit>
    | <hex-digit-pairs> <hex-digit> <hex-digit>

<hex-digit> one of:
    0 1 2 3 4 5 6 7 8 9 A B C D E F a b c d e f

<date-literal>
    : D' YYYY-MM-DD '                 // ISO format; other formats per DATFMT

<time-literal>
    : T' HH.MM.SS '                   // ISO format; others per TIMFMT

<timestamp-literal>
    : Z' YYYY-MM-DD-HH.MM.SS.mmmmmm ' // ISO with microseconds

<ucs2-literal>
    : U' <hex-digit-pairs> '

<graphic-literal>
    : G' <DBCS-bytes> '

<indicator-literal>                   // *ON / *OFF / *BLANKS / etc.
    : *ON | *OFF | *BLANK | *BLANKS | *ZERO | *ZEROS | *HIVAL | *LOVAL
    | *NULL | *OMIT | *START | *END | *ALL ' <char-sequence> '
```

**Figurative constants** (special literals): `*ON`, `*OFF`, `*BLANK(S)`, `*ZERO(S)`, `*HIVAL`, `*LOVAL`, `*NULL`, `*OMIT`, `*ALL'x'`, `*START`, `*END`, `*LOOPCOUNT`.

### 2.7 Operators

```
<operator> one of:
    +    -    *    /    **                           // arithmetic
    =    <>   <    >    <=   >=                       // relational
    AND  OR   NOT                                     // logical (words)
    AND  OR   NOT  XOR                                // bit-level: via BIFs %BITAND etc.
```

### 2.8 Punctuators

```
<punctuator> one of:
    ;    :    ,    (    )    *
```

(`;` ends statements in free-form. `:` separates parameters in many BIFs and opcodes. `*` introduces figurative constants.)

### 2.9 Comments

```
<comment>
    : // (until end of line)                          // free-form
    | * (fixed-form: column 7 = *, rest of the line is a comment)
    | /* ... */                                       // non-standard; some variants accept it
```

---

## 3. Fixed-form: positional layout

In fixed-form **every column matters**. Lines of 80 characters. Column 6 (1-indexed, the character at position 6) defines the spec type.

### 3.1 H-spec (Control/Header) — column 6 = `H`

Defines global options. Free syntax after column 7 with keywords such as:

```
<H-spec>
    : H { <H-keyword> }*

<H-keyword> one of:
    DATFMT(<format>)        TIMFMT(<format>)
    DECEDIT('<char>')       ALTSEQ(<table>)
    DEBUG                   DFTACTGRP(*NO|*YES)
    ACTGRP('<name>'|*NEW|*CALLER)
    BNDDIR('<name>')        OPTION(*SRCSTMT:*NODEBUGIO)
    THREAD(*SERIALIZE|*CONCURRENT)
    FIXNBR(*ZONED:*INPUTPACKED)
    EXTBINX                 NOMAIN
```

### 3.2 F-spec (File description) — column 6 = `F`

```
Columns:
  6       = F
  7-16    = File name
  17      = Type: I (Input) | O (Output) | U (Update) | C (Combined) | D (Display)
  18      = Designation: P (primary) | S (secondary) | F (full procedural) | R (record)
  19      = End-of-file: E (designated end)
  20      = Sequence: A (ascending) | D (descending)
  21      = Access: F (sequential) | K (keyed)
  22-27   = Record length
  28-32   = Key length
  33      = Key type: A | P | B | I (alphanumeric/packed/binary/integer)
  34      = I/O access: D (disk) | T (table)
  35-41   = Reserved
  42      = Device: DISK | PRINTER | WORKSTN | SEQ | SPECIAL
  44-80   = Additional keywords: PREFIX, RENAME, EXTFILE, USROPN, etc.
```

### 3.3 D-spec (Definition) — column 6 = `D`

```
Columns:
  6       = D
  7-21    = Name (15 chars)
  22      = External description (E) or not
  23      = Declaration type: S (standalone) | DS (data structure) | C (constant) | PR (prototype) | PI (procedure interface)
  24-25   = Reserved
  26-32   = From position (starting position in structures)
  33-39   = To position / length
  40      = Data type: A | B | C | D | F | G | I | N | O | P | S | T | U | Z | * (pointer) | <object>
  41-42   = Decimals
  44-80   = Keywords: INZ, BASED, OVERLAY, LIKE, DIM, etc.
```

#### Data type codes

```
A    Alphanumeric (character)
B    Binary numeric (legacy)
C    UCS-2 character
D    Date
F    Float
G    Graphic
I    Integer (signed)
N    Indicator
O    Object (Java)
P    Packed decimal
S    Zoned decimal
T    Time
U    Unsigned integer
Z    Timestamp
*    Pointer (basing pointer / procedure pointer)
```

### 3.4 C-spec (Calculation) — column 6 = `C`

Historically the heart of RPG. Structure:

```
Columns:
  6       = C
  7-8     = Conditional indicator (N01: if NOT indicator 01)
  9-11    = Conditional indicator 2
  12-25   = Factor 1 (left operand)
  26-35   = Operation code (opcode)
  36-49   = Factor 2 (right operand)
  50-63   = Result field
  64-68   = Length
  69-70   = Decimal positions
  71-72   = Indicator hi (result > Factor 2)
  73-74   = Indicator lo (result < Factor 2)
  75-76   = Indicator eq (result = Factor 2)
```

Example:
```
     C                   EVAL      X = Y + Z
     C                   IF        A > B
     C                   READ      MYFILE
     C                   EXCEPT    MYEXC
```

#### 3.4.1 Free-form in C-spec (`/FREE` ... `/END-FREE`)

Allows writing the contents of the C-spec with statements:

```
     C/FREE
     X = Y + Z;
     IF A > B;
       READ MYFILE;
     ENDIF;
     C/END-FREE
```

(Historical: introduced in V5R1. Replaced by fully-free-form in 7.1 TR7.)

### 3.5 P-spec (Procedure) — column 6 = `P`

Delimits a sub-procedure within the module:

```
Columns:
  6       = P
  7-21    = Procedure name
  24      = B (begin) | E (end)
  44-80   = Keywords: EXPORT, IMPORT
```

Example:
```
     P MyProc          B
     D MyProc          PI
     D   parm1                       10A
     C                   ...
     P MyProc          E
```

### 3.6 I-spec and O-spec (legacy)

Specific to "RPG cycle" style programs with files described in the program. In modern code they are barely used — files are described externally and accessed via F-spec with the `EXTNAME` keyword. The complete grammar is not included here; refer to the IBM reference.

### 3.7 Indicators (legacy flow-control mechanism)

**Indicators** are global boolean flags. There are 99 numeric indicators (`*IN01` ... `*IN99`), plus special indicators (`*INLR` = Last Record, `*INRT`, `*INH1`-`*INH9`, etc.).

Legacy opcodes set indicators in their columns 71-76 according to the result of the operation. Example:

```
     C     KEY1          CHAIN     MYFILE                            50
```

If CHAIN does not find the record, indicator `*IN50` is turned on. It is then tested:

```
     C                   IF        *IN50 = *OFF
       (record found)
     C                   ENDIF
```

In modern code this is replaced by the BIFs `%FOUND`, `%EOF`, `%ERROR`, etc.

---

## 4. Free-form: modern syntax

Applies both to `**FREE` mode (fully free-form) and inside `/FREE` blocks. Statements end with `;` and there are no significant columns.

### 4.1 Control specification (free-form)

```
<ctl-opt-statement>
    : CTL-OPT { <ctl-opt-keyword> }* ;
```

Equivalent to H-spec. Same keywords.

### 4.2 Declarations

```
<declaration>
    : <dcl-s>
    | <dcl-c>
    | <dcl-ds>
    | <dcl-pr>
    | <dcl-pi>
    | <dcl-f>
    | <dcl-proc>
    | <dcl-subf>
    | <dcl-parm>
```

#### 4.2.1 Standalone variable

```
<dcl-s>
    : DCL-S <identifier> <type-spec> { <var-keyword> }* ;

<type-spec>
    : <type-name>                                    // e.g. INT, CHAR, PACKED, ZONED
    | <type-name> ( <length-spec> )                  // CHAR(10), PACKED(7:2)
    | LIKE ( <identifier> )                          // same shape as another variable
    | LIKEDS ( <identifier> )                        // same shape as a DS
    | LIKEREC ( <record-format> { : <part> } )

<type-name> one of:
    CHAR    VARCHAR  UCS2    VARUCS2  GRAPH   VARGRAPH
    PACKED  ZONED    BINDEC  INT      UNS     FLOAT
    DATE    TIME     TIMESTAMP
    IND     POINTER  OBJECT

<var-keyword> one of:
    INZ ( <expr> )      INZ ( *LIKEDS )
    BASED ( <pointer> )
    EXPORT              IMPORT             STATIC          TEMPLATE
    DIM ( <const> )     OVERLAY ( <var> { : <pos> } )
    POS ( <const> )     CCSID ( <id> )
    CONST                                           // only on parameters
    VALUE
    OPTIONS ( <opt> { : <opt> }* )
```

#### 4.2.2 Constants

```
<dcl-c>
    : DCL-C <identifier> <const-value> ;
    | DCL-C <identifier> CONST ( <const-value> ) ;

<const-value>
    : <literal>
    | <figurative-constant>
    | ( <expression> )                              // constant expression
```

#### 4.2.3 Data structure

```
<dcl-ds>
    : DCL-DS <identifier> { <ds-keyword> }* ;
        { <ds-subfield> }*
      END-DS ;
    | DCL-DS <identifier> { <ds-keyword> }* END-DS ;     // no subfields

<ds-keyword> one of:
    QUALIFIED    TEMPLATE    EXTNAME ( <file> )
    LIKEDS ( <ds> )          LIKEREC ( <rec> )
    INZ          BASED ( <ptr> )
    DIM ( <const> )          OCCURS ( <const> )           // legacy
    ALIGN
    PSDS                                                    // program status DS
    INFDS ( <file> )                                        // file info DS

<ds-subfield>
    : <identifier> <type-spec> { <var-keyword> }* ;
```

#### 4.2.4 Prototype and Procedure Interface

```
<dcl-pr>
    : DCL-PR <identifier> <return-type-spec>? { <pr-keyword> }* ;
        { <dcl-parm> }*
      END-PR ;
    | DCL-PR <identifier> <return-type-spec>? { <pr-keyword> }* END-PR ;

<dcl-pi>
    : DCL-PI <identifier> <return-type-spec>? { <pi-keyword> }* ;
        { <dcl-parm> }*
      END-PI ;
    | DCL-PI <identifier> <return-type-spec>? { <pi-keyword> }* END-PI ;

<return-type-spec>
    : <type-spec>

<pr-keyword>
    : EXTPGM ( '<program-name>' )                   // call to *PGM object
    | EXTPROC ( '<procedure-name>' )                // explicit external name
    | OPDESC                                         // pass operational descriptors

<pi-keyword>
    : (same as dcl-s; e.g. STATIC, EXPORT, etc.)

<dcl-parm>
    : DCL-PARM <identifier> <type-spec> { <parm-keyword> }* ;

<parm-keyword> one of:
    CONST     VALUE     OPDESC     OPTIONS ( *NOPASS | *OMIT | *VARSIZE | *STRING )
    RTNPARM
```

#### 4.2.5 File declaration

```
<dcl-f>
    : DCL-F <file-name> { <f-keyword> }* ;

<f-keyword> one of:
    USAGE ( *INPUT | *OUTPUT | *UPDATE | *DELETE )
    KEYED     RECNO ( <var> )       PREFIX ( <pfx> { : <n> } )
    RENAME ( <old> : <new> )        IGNORE ( <fmt> )       INCLUDE ( <fmt> )
    EXTFILE ( '<file>' )            EXTMBR ( '<mbr>' )     EXTDESC ( '<file>' )
    DEVID ( <var> )                 USROPN
    DISK      PRINTER     WORKSTN     SEQ     SPECIAL
    BLOCK ( *YES | *NO )
    INDDS ( <ds-name> )             INFDS ( <ds-name> )    INFSR ( <sr> )
```

#### 4.2.6 Procedure declaration

```
<dcl-proc>
    : DCL-PROC <identifier> { <proc-keyword> }* ;
        <dcl-pi>?
        { <declaration> | <statement> }*
      END-PROC ;
    | DCL-PROC <identifier> { <proc-keyword> }* ;
        <dcl-pi>?
        { <declaration> | <statement> }*
      END-PROC <identifier> ;

<proc-keyword> one of:
    EXPORT
    EXTPROC ( '<name>' )
```

### 4.3 Statements

```
<statement>
    : <assignment-statement>
    | <if-statement>
    | <select-statement>
    | <dow-statement>
    | <dou-statement>
    | <for-statement>
    | <monitor-statement>
    | <subroutine-statement>
    | <return-statement>
    | <leave-statement>
    | <iter-statement>
    | <leavesr-statement>
    | <call-statement>
    | <file-op-statement>
    | <opcode-statement>
    | <bif-statement>
    | <expression-statement>
```

#### 4.3.1 Assignment (EVAL)

```
<assignment-statement>
    : <lvalue> = <expression> ;
    | EVAL <lvalue> = <expression> ;
    | EVAL ( H )   <lvalue> = <expression> ;        // half-adjust (round)
    | EVAL ( R )   <lvalue> = <expression> ;        // truncate
    | EVAL-CORR <ds-lvalue> = <ds-rvalue> ;          // matching subfields

<lvalue>
    : <identifier>
    | <identifier> ( <subscript-list> )              // array element
    | <identifier> . <identifier>                    // qualified DS subfield
    | %SUBST ( <var> : <start> : <length>? )
```

#### 4.3.2 IF / ELSE

```
<if-statement>
    : IF <expression> ;
        { <statement> }*
      { ELSEIF <expression> ;
        { <statement> }* }*
      { ELSE ;
        { <statement> }* }?
      ENDIF ;
```

#### 4.3.3 SELECT / WHEN / OTHER

```
<select-statement>
    : SELECT ;
        { WHEN <expression> ;
          { <statement> }* }*
        { OTHER ;
          { <statement> }* }?
      ENDSL ;
```

#### 4.3.4 DOW (do while)

```
<dow-statement>
    : DOW <expression> ;
        { <statement> }*
      ENDDO ;
```

#### 4.3.5 DOU (do until)

```
<dou-statement>
    : DOU <expression> ;
        { <statement> }*
      ENDDO ;
```

(DOU evaluates the condition at the end of the block; DOW at the beginning.)

#### 4.3.6 FOR

```
<for-statement>
    : FOR <identifier> = <expression> ( TO | DOWNTO ) <expression> ( BY <expression> )? ;
        { <statement> }*
      ENDFOR ;
```

Example:
```rpg
FOR i = 1 TO 10;
  count = count + i;
ENDFOR;
```

#### 4.3.7 MONITOR (error handling)

```
<monitor-statement>
    : MONITOR ;
        { <statement> }*
      { ON-ERROR <status-list>? ;
        { <statement> }* }*
      { ON-EXIT ;                                    // >=7.5
        { <statement> }* }?
      ENDMON ;

<status-list>
    : <expression>
    | <status-list> : <expression>
```

#### 4.3.8 Subroutine

```
<subroutine-statement>
    : BEGSR <identifier> ;
        { <statement> }*
      ENDSR ( <identifier> )? ;

<exsr-statement>
    : EXSR <identifier> ;
```

#### 4.3.9 Return / Leave / Iter / Leavesr

```
<return-statement>
    : RETURN <expression>? ;

<leave-statement>     : LEAVE ;          // exits the DO/FOR
<iter-statement>      : ITER ;           // next iteration
<leavesr-statement>   : LEAVESR ;        // exits the subroutine
```

#### 4.3.10 Procedure call

```
<call-statement>
    : CALLP <procedure-name> ( <arg-list>? ) ;
    | <procedure-name> ( <arg-list>? ) ;             // short form (free-form)

<arg-list>
    : <expression>
    | <arg-list> : <expression>
```

(`CALLP` is optional when the call is used as a statement; required if there is ambiguity.)

#### 4.3.11 File operations

```
<file-op-statement>
    : READ   <file>  <ds>? ;
    | READE  <key>   <file>  <ds>? ;
    | READP  <file>  <ds>? ;
    | READPE <key>   <file>  <ds>? ;
    | CHAIN  <key>   <file>  <ds>? ;
    | SETLL  <key>   <file> ;
    | SETGT  <key>   <file> ;
    | WRITE  <format-or-file>  <ds>? ;
    | UPDATE <format-or-file>  <ds>? ;
    | DELETE <key>?  <file-or-format> ;
    | UNLOCK <file> ;
    | OPEN   <file> ;
    | CLOSE  <file> ;
    | EXFMT  <format>  <ds>? ;
```

(Multipart keys are built with `%KDS(<ds>)` or with a key-array literal.)

### 4.4 Expressions

```
<expression>
    : <or-expression>

<or-expression>
    : <and-expression>
    | <or-expression> OR <and-expression>

<and-expression>
    : <not-expression>
    | <and-expression> AND <not-expression>

<not-expression>
    : <comparison-expression>
    | NOT <not-expression>

<comparison-expression>
    : <additive-expression>
    | <additive-expression> <comparison-op> <additive-expression>

<comparison-op> one of:
    =   <>   <   >   <=   >=

<additive-expression>
    : <multiplicative-expression>
    | <additive-expression> + <multiplicative-expression>
    | <additive-expression> - <multiplicative-expression>

<multiplicative-expression>
    : <power-expression>
    | <multiplicative-expression> * <power-expression>
    | <multiplicative-expression> / <power-expression>

<power-expression>
    : <unary-expression>
    | <unary-expression> ** <power-expression>           // right-associative

<unary-expression>
    : <postfix-expression>
    | + <unary-expression>
    | - <unary-expression>

<postfix-expression>
    : <primary>
    | <postfix-expression> ( <argument-list>? )          // BIF or proc call
    | <postfix-expression> ( <subscript-list> )          // array indexing
    | <postfix-expression> . <identifier>                // qualified DS access

<primary>
    : <identifier>
    | <literal>
    | <figurative-constant>
    | <indicator-ref>                                    // *IN01, *INLR, etc.
    | <built-in-function-call>
    | ( <expression> )

<built-in-function-call>
    : % <identifier> ( <argument-list>? )

<argument-list>
    : <expression>
    | <argument-list> : <expression>

<subscript-list>
    : <expression>
    | <subscript-list> : <expression>                    // multi-dim
```

#### Precedence (highest to lowest)

```
1. ( )  BIF/proc calls  indexing
2. ** (right-assoc)
3. unary + / -
4. * /
5. + -
6. = <> < > <= >=
7. NOT
8. AND
9. OR
```

---

## 5. Semantic rules

As with C99, these cannot be expressed in BNF. Summary:

### 5.1 Types and conversion

- **Numeric:** `PACKED`, `ZONED`, `BINDEC`, `INT`, `UNS`, `FLOAT`. Conversion between numerics preserves value but may truncate decimals (configurable with `EVAL(H)` to round).
- **Strings:** `CHAR` (fixed), `VARCHAR` (variable), `UCS2`, `GRAPH`. Concatenation with `+` or `%CONCAT`.
- **Date/time:** `DATE`, `TIME`, `TIMESTAMP`. Arithmetic with `%DIFF`, `%DATE`, `%TIME`, `+ <duration>`.
- **Indicators:** `IND` (1-char boolean `'1'`/`'0'`). Convertible with figurative constants `*ON`/`*OFF`.
- **Pointers:** `POINTER` (basing pointer), `*` for procedure pointer. Limited arithmetic.

### 5.2 Scope and visibility

- **Module-level (global):** declarations outside any `DCL-PROC`. Visible throughout the module.
- **Procedure-level (local):** declarations inside a `DCL-PROC`. Only visible within that procedure.
- **Export/Import:** symbols marked `EXPORT` in a module can be referenced with `IMPORT` from another module of the same program (after binding).
- **Static / Automatic storage:** local variables are **automatic** by default. With `STATIC` they retain their value between calls.

### 5.3 Indicators (legacy mechanism)

- 99 numeric indicators `*IN01`-`*IN99`, all global.
- Special indicators: `*INLR` (Last Record, marks end of program), `*INRT`, `*INH1`-`*INH9`, `*INU1`-`*INU8`.
- In modern code: replace with BIFs (`%FOUND`, `%EOF`, `%ERROR`, `%EQUAL`, `%STATUS`) or with an indicator data structure (`INDDS`) with descriptive names.

### 5.4 RPG Cycle (main cycle)

A legacy RPG program with a "primary" file (P in F-spec): the runtime executes an implicit cycle:

1. Read the next record from the primary.
2. Process control breaks.
3. Execute the detail calculations (C-specs without a break condition).
4. Process output.
5. If `*INLR = *ON` -> terminate, otherwise return to step 1.

**Importance for lowering:** programs with the cycle require generating a main loop in C, not a linear main. `NOMAIN` programs or pure-procedure programs have no cycle.

### 5.5 Sub-procedures vs. main procedure

- **Main procedure:** the program's entry point (`*PGM`). It is the code not wrapped in `DCL-PROC`. In `NOMAIN` mode (in H-spec) there is no main.
- **Sub-procedure:** internal functions/procedures, declared with `DCL-PROC ... END-PROC`. They may or may not return a value, and may receive parameters with `CONST`/`VALUE`/by-reference.
- **By-reference vs by-value:** the default is by-reference. `VALUE` passes by value; `CONST` allows passing literals and constant expressions but the callee cannot modify them.

### 5.6 Activation groups and binding

- ILE RPG runs inside **activation groups** — execution contexts that isolate resources (open files, job overrides, static storage).
- Binding combines compiled modules (`*MODULE`) into a program (`*PGM`) or service program (`*SRVPGM`).
- Relevant for **emulation on Windows/Linux**: it is necessary to decide how to model activation groups and service programs in the runtime.

### 5.7 Result Data Structure (RDS) for file ops

File operations accept an optional "result data structure" as the destination for the read record:

```rpg
DCL-DS empRec EXTNAME('EMPLOYEE') QUALIFIED;
END-DS;

CHAIN keyValue EMPLOYEE empRec;
```

The record is copied into `empRec` instead of into global variables with inherited names.

### 5.8 PSDS and INFDS

- **PSDS (Program Status Data Structure):** a special structure declared with `PSDS`. Receives program-state information (status code, current library, job name, etc.) at fixed positions.
- **INFDS (File Information Data Structure):** a structure associated with a file via `INFDS(<ds>)`. Receives information from the file's last I/O (status, RRN, format, etc.).

### 5.9 Initialization

- `STATIC` and global variables without `INZ` -> initialized to the type default (numerics to 0, alphanumerics to blanks, dates to 0001-01-01).
- `AUTO` variables (the local default) without `INZ` -> also initialized to the default (this differs from C; in RPG they are never indeterminate).

### 5.10 Half-adjust and decimal precision

- Packed/zoned arithmetic is **exact decimal** (not floating point), with configurable precision.
- `EVAL(H)` indicates "half-adjust" — round to the nearest decimal. Default is to truncate.
- `EVAL(R)` indicates explicit truncation.
- Promotion rules are defined by the language: the result is computed with sufficient precision and then adjusted to the destination.

---

## 6. Built-in Functions — quick reference

| BIF | Description |
|---|---|
| `%ABS(n)` | Absolute value |
| `%ADDR(v)` | Memory address of v |
| `%ALLOC(n)` | Allocates n bytes and returns a pointer |
| `%CHAR(v {:fmt})` | Converts to string |
| `%CHECK(set:str {:start})` | Position of the first char of str not in set |
| `%CHECKR(set:str {:start})` | Like CHECK but from the right |
| `%DATE(v {:fmt})` | Converts to DATE |
| `%DAYS(n)` | Creates a duration of n days |
| `%DEC(v {:prec:dec})` | Converts to packed decimal |
| `%DECH(...)` | Like %DEC with half-adjust |
| `%DIFF(d1:d2:fmt)` | Difference between two dates/timestamps |
| `%DIV(a:b)` | Integer division |
| `%EDITC(n:'code')` | Formats numeric with edit code |
| `%EDITW(n:'mask')` | Formats numeric with edit word |
| `%ELEM(arr)` | Number of elements in the array |
| `%EOF({file})` | Indicates whether the last I/O hit end-of-file |
| `%EQUAL({file})` | Indicates whether SETLL/SETGT found an exact match |
| `%ERROR()` | Indicates whether the last opcode with (E) errored |
| `%FLOAT(v)` | Converts to float |
| `%FOUND({file})` | Indicates whether CHAIN/SETLL/SETGT found a record |
| `%INT(v)`, `%INTH(v)` | Converts to integer (with/without half-adjust) |
| `%LEN(v)` | Current length of a VARCHAR or declared length |
| `%LOOKUP(arg:arr {:start:nbr})` | Array search |
| `%LOWER(s)`, `%UPPER(s)` | Conversion to lower/upper case |
| `%NULL`, `%NULLIND(f)` | Null manipulation (DB) |
| `%OPEN(file)` | Indicates whether the file is open |
| `%PARMS()`, `%PARMNUM(n)` | Number of parameters passed / parameter number |
| `%REM(a:b)` | Division remainder |
| `%REPLACE(src:tgt {:start:len})` | Replaces substring |
| `%SCAN(needle:haystack {:start})` | Position of needle in haystack |
| `%SIZE(v {:*ALL})` | Size in bytes |
| `%STATUS({file})` | Status code of the last I/O |
| `%STR(ptr {:len})` | Null-terminated C string |
| `%SUBST(s:start:len)` | Substring |
| `%TIME(v {:fmt})` | Converts to TIME |
| `%TIMESTAMP(v {:fmt})` | Converts to TIMESTAMP |
| `%TRIM(s)`, `%TRIML(s)`, `%TRIMR(s)` | Trim |
| `%XFOOT(arr)` | Sum of all elements in the array |
| `%XLATE(from:to:str)` | Translates characters |

(Representative list. IBM publishes the complete reference in each release.)

---

## 7. Differences between RPG dialects

If `rpg-frontend` is aimed at parsing real-world code, it is worth keeping the dialects in mind:

| Dialect | Characteristics |
|---|---|
| **RPG II** | The original, '70s. Very rarely seen alive. Pure fixed-form. |
| **RPG III** | OS/400 up to the '90s. Fixed-form. |
| **RPG IV (V3R1+)** | Modern. Introduces D-spec, free-form C-spec, prototypes. |
| **RPG IV with `/FREE`** | V5R1+. Free-form inside C-specs. |
| **RPG IV fully-free** | 7.1 TR7+. `**FREE` at the start of the file. |
| **SQLRPGLE** | Embedded SQL with `EXEC SQL ... ;`. Pre-processed before RPG compilation. |

**Scope decision for `rpg-frontend`:**
- **Minimum viable:** parse fully-free-form (`**FREE`) — modern, cleaner, syntax closer to traditional BNF.
- **Realistic:** support for `/FREE` inside fixed-form (most legacy code).
- **Ambitious:** full support for pure fixed-form (legacy '90s programs that have not been modernized).

---

## 8. References

- IBM, *ILE RPG Language Reference* (SC09-2508). Official language documentation. Available at https://www.ibm.com/docs/en/i/<version>?topic=programming-rpg
- IBM, *ILE RPG Programmer's Guide* (SC09-2507). Programming guide with examples.
- IBM Redbooks on RPG IV modernization.
- Active communities: `midrange.com`, `rpgpgm.com`, `iSeriesGuru`.

**What does NOT exist:** a formal ISO-style standard with the grammar consolidated in BNF. This section is the closest thing to that, reconstructed from IBM's documentation.
