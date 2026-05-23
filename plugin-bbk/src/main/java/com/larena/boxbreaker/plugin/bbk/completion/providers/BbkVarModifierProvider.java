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
 * Suggests {@code DCL-S} / DS-subfield modifiers (INZ, DIM, BASED, STATIC, EXPORT,
 * OVERLAY, POS, QUALIFIED, TEMPLATE, ALIGN, OPTIONS).
 *
 * <p>The grammar permits these after a complete type specification; rather than
 * try to detect "after type" precisely, we fire whenever the caret is inside a
 * variable declaration or a DS subfield context — overlapping with type
 * suggestions in some positions is acceptable (IntelliJ ranks alphabetically and
 * the user filters by typing).
 */
public class BbkVarModifierProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.afterDclSIdent(position)
            || BbkCompletionPatterns.insideDsBody(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.varModifier");
        return List.of(
            Suggestion.full("INZ",       "(<value>)",        type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("DIM",       "(<n>)",            type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("BASED",     "(<ptr>)",          type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("STATIC",                        type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("EXPORT",                        type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("IMPORT",                        type, BbkIcons.Category.MODIFIER),
            Suggestion.full("OVERLAY",   "(<parent>:<pos>)", type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("POS",       "(<pos>)",          type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("QUALIFIED",                     type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("TEMPLATE",                      type, BbkIcons.Category.MODIFIER),
            Suggestion.plain("ALIGN",                         type, BbkIcons.Category.MODIFIER),
            Suggestion.full("OPTIONS",   "(*FLAG:*FLAG)",    type, BbkIcons.Category.MODIFIER, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
