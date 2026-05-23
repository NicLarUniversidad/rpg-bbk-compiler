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
 * Suggests built-in functions (BIFs) inside expression positions. Block A treats
 * them as plain keyword-style suggestions with auto-inserted parens; Block E will
 * later upgrade them with proper signatures and type-aware filtering.
 *
 * <p>BIFs live in expressions, so this provider fires anywhere a statement could
 * start (procedure body) — the user's surrounding tokens will determine whether
 * they're actually inside an expression.
 */
public class BbkBifProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.atStatementStart(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.bif");
        return List.of(
            Suggestion.full("trim",      "(<s>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("triml",     "(<s>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("trimr",     "(<s>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("substr",    "(<s>, <start>, <len>)", type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("scan",      "(<needle>, <hay>)", type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("len",       "(<s>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("char",      "(<n>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("dec",       "(<s>, <p>, <s>)",  type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("int",       "(<n>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("inth",      "(<n>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("abs",       "(<n>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("elem",      "(<arr>)",          type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("addr",      "(<v>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("size",      "(<v>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("date",      "(\"<yyyy-mm-dd>\")", type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("time",      "(\"<hh:mm:ss>\")", type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("timestamp", "(\"<iso>\")",      type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("days",      "(<n>)",            type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("diff",      "(<a>, <b>)",       type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("lookup",    "(<key>, <arr>)",   type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("xlate",     "(<from>, <to>, <s>)", type, BbkIcons.Category.BIF, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
