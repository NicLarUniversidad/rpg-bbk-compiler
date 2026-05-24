package com.larena.boxbreaker.plugin.bbk.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.larena.boxbreaker.plugin.bbk.psi.BbkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tells IntelliJ which token pairs to match and auto-close in BBK.
 *
 * <p>Without this extension the editor cannot:
 *
 * <ul>
 *   <li>Auto-insert {@code }}/{@code )}/{@code ]} when the user types the opener.</li>
 *   <li>Highlight the matching counterpart when the caret is on a brace.</li>
 *   <li>Expand {@code &#123;}+Enter to {@code &#123;\n  |\n&#125;} (the EnterBetweenBracesDelegate
 *       only fires for pairs flagged as structural here).</li>
 * </ul>
 *
 * <p>Curly braces are marked structural ({@code true}) because they delimit blocks;
 * round and square braces are not (they delimit expressions and modifier args).
 */
public class BbkBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = {
        new BracePair(BbkTypes.LBRACE,   BbkTypes.RBRACE,   /*structural*/ true),
        new BracePair(BbkTypes.LPAREN,   BbkTypes.RPAREN,   /*structural*/ false),
        new BracePair(BbkTypes.LBRACKET, BbkTypes.RBRACKET, /*structural*/ false),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType,
                                                   @Nullable IElementType contextType) {
        // Permissive: allow auto-pairing before any context token. Refine later only
        // if a specific token causes friction (e.g. before an IDENT that's part of
        // an ongoing expression — for now this is fine in practice).
        return true;
    }

    @Override
    public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
        // For "structural" `{}` the IDE uses this to compute where to scroll when
        // jumping to a matching brace. Returning the opener's own offset is a safe
        // default; a smarter implementation could walk up to the parent declaration.
        return openingBraceOffset;
    }
}
