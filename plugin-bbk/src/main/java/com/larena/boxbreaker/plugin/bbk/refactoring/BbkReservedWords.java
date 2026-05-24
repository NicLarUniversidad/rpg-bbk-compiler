package com.larena.boxbreaker.plugin.bbk.refactoring;

import java.util.Set;

/**
 * Case-insensitive catalog of every BBK keyword. Used by
 * {@link BbkRenameInputValidator} to reject rename targets that would shadow the
 * language itself.
 *
 * <p>Keep in sync with {@code BBK.bnf} / {@code BBK.flex} when new keywords are added.
 */
public final class BbkReservedWords {

    public static final Set<String> ALL = Set.of(
        // Declaration keywords
        "DCL-S", "DCL-C", "DCL-DS", "DCL-PR", "DCL-PROC", "DCL-F", "DCL-PARM", "DCL-SUBF",
        // Module directive
        "CTL-OPT",
        // Primitive types
        "CHAR", "VARCHAR", "PACKED", "ZONED", "BINDEC",
        "INT", "UNS", "FLOAT",
        "DATE", "TIME", "TIMESTAMP",
        "BOOL", "POINTER", "VOID",
        // Declaration modifiers
        "INZ", "BASED", "DIM", "OVERLAY", "POS",
        "LIKE", "LIKEDS", "LIKEREC",
        "TEMPLATE", "QUALIFIED", "ALIGN",
        "VALUE", "CONST", "OPTIONS", "RTNPARM", "OPDESC",
        "STATIC", "EXPORT", "IMPORT", "EXTPGM", "EXTPROC",
        // File-spec keywords
        "USAGE", "KEYED", "EXTNAME", "EXTFILE", "PREFIX", "RENAME",
        "DISK", "PRINTER", "WORKSTN", "SEQ", "USROPN", "INFDS", "INDDS",
        // Control flow (C-style)
        "if", "else", "while", "do", "for", "break", "continue", "return",
        // Control flow (RPG-style)
        "select", "when", "other", "monitor", "on-error", "on-exit",
        // Boolean and null literals
        "true", "false", "null",
        // File operations
        "read", "reade", "readp", "readpe", "chain",
        "write", "update", "delete", "setll", "setgt",
        "open", "close", "exfmt", "unlock", "callp",
        // Subroutines
        "BEGSR", "ENDSR", "EXSR", "LEAVESR",
        // Directives
        "PRE-IF", "PRE-ELSEIF", "PRE-ELSE", "PRE-ENDIF",
        "PRE-DEFINE", "PRE-UNDEFINE", "PRE-INCLUDE", "PRE-EOF"
    );

    /** True if {@code name} matches any keyword (case-insensitive). */
    public static boolean isReserved(String name) {
        if (name == null) return false;
        for (String kw : ALL) {
            if (kw.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private BbkReservedWords() {}
}
