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
 * Suggests primitive types and {@code LIKE}/{@code LIKEDS}/{@code LIKEREC} in any
 * position that expects a type specification: {@code DCL-S}, {@code DCL-DS} subfield,
 * inline parameter, {@code for}-init declaration.
 */
public class BbkTypeProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.afterDclSIdent(position)
            || BbkCompletionPatterns.insideDsBody(position)
            || BbkCompletionPatterns.insideParamList(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.primitiveType");
        return List.of(
            Suggestion.full("INT",       "(precision)",       type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("UNS",       "(precision)",       type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("CHAR",      "(length)",          type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("VARCHAR",   "(length)",          type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("PACKED",    "(precision:scale)", type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("ZONED",     "(precision:scale)", type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("BINDEC",    "(precision:scale)", type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("FLOAT",     "(4|8)",             type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("DATE",                           type, BbkIcons.Category.TYPE),
            Suggestion.plain("TIME",                           type, BbkIcons.Category.TYPE),
            Suggestion.plain("TIMESTAMP",                      type, BbkIcons.Category.TYPE),
            Suggestion.plain("BOOL",                           type, BbkIcons.Category.TYPE),
            Suggestion.plain("POINTER",                        type, BbkIcons.Category.TYPE),
            Suggestion.plain("VOID",                           type, BbkIcons.Category.TYPE),
            Suggestion.full("LIKE",      "(<var>)",           type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("LIKEDS",    "(<ds>)",            type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("LIKEREC",   "(<rec>)",           type, BbkIcons.Category.TYPE, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
