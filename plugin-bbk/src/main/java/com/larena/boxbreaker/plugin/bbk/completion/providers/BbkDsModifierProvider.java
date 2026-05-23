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
 * Suggests modifiers that apply to a {@code DCL-DS} declaration header,
 * after the name and before the opening brace: {@code QUALIFIED}, {@code TEMPLATE},
 * {@code EXTNAME}, {@code LIKEDS}, {@code LIKEREC}, {@code ALIGN}, {@code INZ},
 * {@code BASED}, {@code DIM}.
 */
public class BbkDsModifierProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideDsHeader(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.dsModifier");
        return List.of(
            Suggestion.plain("QUALIFIED",                       type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("TEMPLATE",                        type, BbkIcons.Category.MODIFIER),
            Suggestion.full("EXTNAME",   "(\"<name>\")",       type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("LIKEDS",    "(<ds>)",             type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("LIKEREC",   "(<rec>)",            type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("ALIGN",                           type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("INZ",                             type, BbkIcons.Category.MODIFIER),
            Suggestion.full("BASED",     "(<ptr>)",            type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("DIM",       "(<n>)",              type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("INFDS",     "(<ds>)",             type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
