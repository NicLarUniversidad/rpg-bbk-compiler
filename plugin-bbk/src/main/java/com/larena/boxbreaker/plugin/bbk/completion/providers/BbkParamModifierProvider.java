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
 * Suggests parameter modifiers inside an inline parameter list:
 * {@code VALUE}, {@code CONST}, {@code OPDESC}, {@code OPTIONS}.
 */
public class BbkParamModifierProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideParamList(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.paramModifier");
        return List.of(
            Suggestion.plain("VALUE",                            type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("CONST",                            type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("OPDESC",                           type, BbkIcons.Category.MODIFIER),
            Suggestion.full("OPTIONS",   "(*FLAG:*FLAG)",       type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
