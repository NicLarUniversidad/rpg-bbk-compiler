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
 * Suggests record-level file operations inside a procedure body:
 * {@code read}, {@code reade}, {@code readp}, {@code readpe}, {@code chain},
 * {@code write}, {@code update}, {@code delete}, {@code setll}, {@code setgt},
 * {@code open}, {@code close}, {@code exfmt}, {@code unlock}, {@code callp}.
 */
public class BbkFileOpProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.atStatementStart(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.fileOp");
        return List.of(
            Suggestion.full("read",   " <file> [<ds>];",     type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("reade",  " <key> <file> [<ds>];", type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("readp",  " <file> [<ds>];",     type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("readpe", " <key> <file> [<ds>];", type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("chain",  " <key> <file> [<ds>];", type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("write",  " <file> [<ds>];",     type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("update", " <file> [<ds>];",     type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("delete", " <key> <file>;",      type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("setll",  " <key> <file>;",      type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("setgt",  " <key> <file>;",      type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("open",   " <file>;",            type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("close",  " <file>;",            type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("exfmt",  " <file> <ds>;",       type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("unlock", " <file>;",            type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE),
            Suggestion.full("callp",  " <proc>(<args>);",    type, BbkIcons.Category.FILE_OP, InsertHandlers.TRAILING_SPACE)
        );
    }
}
