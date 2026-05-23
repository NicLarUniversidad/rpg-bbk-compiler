package com.larena.boxbreaker.plugin.bbk.templates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.larena.boxbreaker.plugin.bbk.BbkFileType;
import com.larena.boxbreaker.plugin.bbk.completion.BbkCompletionPatterns;
import org.jetbrains.annotations.NotNull;

/**
 * Narrower template context: cursor is inside a procedure body where a statement
 * could legally begin. Used by templates like {@code ifb}, {@code whileb},
 * {@code forb}, {@code dowb}, {@code selectb}, {@code monitorb}, {@code preif}.
 */
public class BbkProcedureBodyTemplateContext extends BbkLiveTemplateContext {

    public BbkProcedureBodyTemplateContext() {
        super("BBK Procedure Body");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext context) {
        PsiFile file = context.getFile();
        if (!file.getFileType().equals(BbkFileType.INSTANCE)) return false;
        PsiElement el = file.findElementAt(context.getStartOffset());
        if (el == null) return false;
        return BbkCompletionPatterns.atStatementStart(el);
    }
}
