package com.larena.boxbreaker.plugin.bbk.lexer;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.psi.tree.TokenSet;
import com.larena.boxbreaker.plugin.bbk.BbkLexerAdapter;
import com.larena.boxbreaker.plugin.bbk.psi.BbkTypes;

/**
 * Tells IntelliJ which BBK tokens count as identifiers, keywords, literals and comments.
 *
 * <p>Without this, hyphenated keywords like {@code DCL-S} are split on {@code -} by the
 * default word logic, which breaks find-usages-by-text, extend selection, default word
 * completion, and the spell checker.
 */
public class BbkWordsScanner extends DefaultWordsScanner {

    public BbkWordsScanner() {
        super(
            new BbkLexerAdapter(),
            // identifiers
            TokenSet.create(BbkTypes.IDENT, BbkTypes.STAR_IDENT),
            // comments
            TokenSet.create(BbkTypes.LINE_COMMENT, BbkTypes.BLOCK_COMMENT),
            // literals
            TokenSet.create(BbkTypes.STR_LIT,
                BbkTypes.INT_LIT, BbkTypes.INT_LIT_HEX, BbkTypes.INT_LIT_OCT,
                BbkTypes.FLOAT_LIT, BbkTypes.DEC_LIT)
        );
    }
}
