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
 * Suggests procedure-level modifiers inside the header of a {@code DCL-PROC}
 * (between its signature and its body): {@code EXPORT}, {@code EXTPROC}.
 */
public class BbkProcModifierProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideProcHeader(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.procModifier");
        return List.of(
            Suggestion.plain("EXPORT",                            type, BbkIcons.Category.MODIFIER),
            Suggestion.full("EXTPROC",   "(\"<name>\")",         type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
