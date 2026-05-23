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
 * Suggests preprocessor directives: {@code PRE-IF}, {@code PRE-ELSEIF}, {@code PRE-ELSE},
 * {@code PRE-ENDIF}, {@code PRE-DEFINE}, {@code PRE-UNDEFINE}, {@code PRE-INCLUDE},
 * {@code PRE-EOF}. Valid both at module top level and inside a procedure body.
 */
public class BbkDirectiveProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.atTopLevelItemStart(position)
            || BbkCompletionPatterns.atStatementStart(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.directive");
        return List.of(
            Suggestion.full("PRE-IF",        " <cond>",          type, BbkIcons.Category.DIRECTIVE, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("PRE-ELSEIF",    " <cond>",          type, BbkIcons.Category.DIRECTIVE, InsertHandlers.TRAILING_SPACE),
            Suggestion.plain("PRE-ELSE",                          type, BbkIcons.Category.DIRECTIVE),
            Suggestion.plain("PRE-ENDIF",                         type, BbkIcons.Category.DIRECTIVE),
            Suggestion.full("PRE-DEFINE",    " <NAME> [<value>]", type, BbkIcons.Category.DIRECTIVE, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("PRE-UNDEFINE",  " <NAME>",          type, BbkIcons.Category.DIRECTIVE, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("PRE-INCLUDE",   " \"<file>\"",      type, BbkIcons.Category.DIRECTIVE, InsertHandlers.TRAILING_SPACE),
            Suggestion.plain("PRE-EOF",                           type, BbkIcons.Category.DIRECTIVE)
        );
    }
}
