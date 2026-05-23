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
 * Suggests the recognised CTL-OPT keyword names inside a {@code CTL-OPT} statement.
 *
 * <p>The grammar treats the keyword identifier inside CTL-OPT as a generic IDENT, so this
 * provider's role is purely UX: surfacing the well-known names. The catalog mirrors the
 * one in {@code docs/theory/boxbreaker/grammar.md} §2.
 */
public class BbkCtlOptKeywordProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideCtlOpt(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.ctlOptKeyword");
        return List.of(
            Suggestion.full("MAIN",        "(<procName>)",     type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("NOMAIN",                          type, BbkIcons.Category.KEYWORD),
            Suggestion.full("OPTION",      "(<opt>:<opt>)",    type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("BNDDIR",      "(\"<name>\")",     type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("DFTACTGRP",   "(*NO|*YES)",       type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("ACTGRP",      "(\"<name>\")",     type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("DEBUG",                           type, BbkIcons.Category.KEYWORD),
            Suggestion.full("DECEDIT",     "(\"<spec>\")",     type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("DATFMT",      "(<fmt>)",          type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("TIMFMT",      "(<fmt>)",          type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("COPYRIGHT",   "(\"<text>\")",     type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("EXTBININT",                       type, BbkIcons.Category.KEYWORD),
            Suggestion.full("FIXNBR",      "(<spec>)",         type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("THREAD",                          type, BbkIcons.Category.KEYWORD),
            Suggestion.full("ALLOC",       "(<spec>)",         type, BbkIcons.Category.KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
