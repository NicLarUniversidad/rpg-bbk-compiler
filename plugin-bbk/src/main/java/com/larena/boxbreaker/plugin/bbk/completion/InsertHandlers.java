package com.larena.boxbreaker.plugin.bbk.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;

/**
 * Reusable post-insertion handlers shared by every provider.
 *
 * <p>All handlers respect the user's intent: they only insert characters that the user
 * has not already typed, and they always reposition the caret to the natural next
 * editing point. After insertion, where useful, they re-trigger the auto-popup so
 * the next context's suggestions appear without a second {@code Ctrl+Space}.
 */
public final class InsertHandlers {

    private InsertHandlers() {}

    /** Inserts {@code ()} and places the caret inside. Used for parameterised types. */
    public static final InsertHandler<LookupElement> PARENS_AND_CARET_INSIDE = (ctx, item) -> {
        Document doc = ctx.getDocument();
        int tail = ctx.getTailOffset();
        if (charAt(doc, tail) != '(') {
            doc.insertString(tail, "()");
        }
        ctx.getEditor().getCaretModel().moveToOffset(tail + 1);
        popup(ctx);
    };

    /** Inserts a trailing space and re-pops completion. Used for declaration keywords. */
    public static final InsertHandler<LookupElement> TRAILING_SPACE = (ctx, item) -> {
        Document doc = ctx.getDocument();
        int tail = ctx.getTailOffset();
        if (charAt(doc, tail) != ' ') {
            doc.insertString(tail, " ");
        }
        ctx.getEditor().getCaretModel().moveToOffset(tail + 1);
        popup(ctx);
    };

    /**
     * Expands a control-flow keyword to {@code KW (|) {\n  \n}} with the caret at the
     * condition. Used for {@code if}, {@code while}, {@code for}, {@code select},
     * {@code monitor}.
     */
    public static final InsertHandler<LookupElement> CONTROL_FLOW_BLOCK = (ctx, item) -> {
        Document doc = ctx.getDocument();
        int tail = ctx.getTailOffset();
        String skeleton = " (\n) {\n  \n}";
        doc.insertString(tail, skeleton);
        // Caret to first newline inside parens.
        ctx.getEditor().getCaretModel().moveToOffset(tail + 2);
    };

    /**
     * Variant for keywords that take no condition: opens just the braces ({@code do},
     * {@code else}, body of {@code other}).
     */
    public static final InsertHandler<LookupElement> BLOCK_ONLY = (ctx, item) -> {
        Document doc = ctx.getDocument();
        int tail = ctx.getTailOffset();
        String skeleton = " {\n  \n}";
        doc.insertString(tail, skeleton);
        ctx.getEditor().getCaretModel().moveToOffset(tail + 4); // inside the braces
    };

    private static char charAt(Document doc, int offset) {
        return (offset < doc.getTextLength()) ? doc.getCharsSequence().charAt(offset) : '\0';
    }

    private static void popup(InsertionContext ctx) {
        Editor editor = ctx.getEditor();
        AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(editor, null);
    }
}
