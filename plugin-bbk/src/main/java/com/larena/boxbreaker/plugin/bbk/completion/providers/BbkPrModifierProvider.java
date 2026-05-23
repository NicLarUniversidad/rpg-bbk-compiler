package com.larena.boxbreaker.plugin.bbk.completion.providers;

import com.larena.boxbreaker.plugin.bbk.BbkBundle;
import com.larena.boxbreaker.plugin.bbk.completion.BbkCompletionPatterns;
import com.larena.boxbreaker.plugin.bbk.completion.InsertHandlers;
import com.larena.boxbreaker.plugin.bbk.completion.Suggestion;
import com.larena.boxbreaker.plugin.bbk.icons.BbkIcons;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Suggests prototype-level modifiers inside a {@code DCL-PR} declaration:
 * {@code EXTPGM}, {@code EXTPROC}, {@code OPDESC}, {@code RTNPARM}.
 */
public class BbkPrModifierProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insidePrototype(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.prModifier");
        return List.of(
            Suggestion.full("EXTPGM",    "(\"<PGMNAME>\")",    type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("EXTPROC",   "(\"<name>\")",       type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("OPDESC",                            type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("RTNPARM",                           type, BbkIcons.Category.MODIFIER)
        );
    }
}
