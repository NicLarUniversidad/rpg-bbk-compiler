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
 * Suggests file-spec keywords inside a {@code DCL-F} declaration:
 * {@code USAGE}, {@code KEYED}, {@code DISK}, {@code PRINTER}, {@code WORKSTN},
 * {@code SEQ}, {@code EXTNAME}, {@code EXTFILE}, {@code USROPN}, {@code PREFIX},
 * {@code RENAME}, {@code INFDS}, {@code INDDS}.
 */
public class BbkFileKeywordProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideFileDeclaration(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.fileKeyword");
        return List.of(
            Suggestion.full("USAGE",     "(*INPUT:*OUTPUT)",   type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("KEYED",                            type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.plain("DISK",                             type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.plain("PRINTER",                          type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.plain("WORKSTN",                          type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.plain("SEQ",                              type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.full("EXTNAME",   "(\"<name>\")",       type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("EXTFILE",   "(\"<path>\")",       type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.plain("USROPN",                           type, BbkIcons.Category.FILE_KEYWORD),
            Suggestion.full("PREFIX",    "(<prefix>)",         type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("RENAME",    "(<from>:<to>)",      type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("INFDS",     "(<ds>)",             type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE),
            Suggestion.full("INDDS",     "(<ds>)",             type, BbkIcons.Category.FILE_KEYWORD, InsertHandlers.PARENS_AND_CARET_INSIDE)
        );
    }
}
