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
 * Suggests the declaration-introducer keywords at module top level:
 * {@code DCL-S}, {@code DCL-C}, {@code DCL-DS}, {@code DCL-F}, {@code DCL-PR},
 * {@code DCL-PROC}, {@code CTL-OPT}.
 */
public class BbkTopLevelKeywordProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.atTopLevelItemStart(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.keyword");
        return List.of(
            Suggestion.full("DCL-S",    " <name> <type>;",         type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("DCL-C",    " <name> <value>;",        type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("DCL-DS",   " <name> { ... }",         type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("DCL-F",    " <name> <opts>;",         type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("DCL-PR",   " <name>(<params>) -> T;", type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("DCL-PROC", " <name>(<params>) { ... }", type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("CTL-OPT",  " <keyword> ... ;",        type, BbkIcons.Category.KEYWORD, InsertHandlers.TRAILING_SPACE)
        );
    }
}
