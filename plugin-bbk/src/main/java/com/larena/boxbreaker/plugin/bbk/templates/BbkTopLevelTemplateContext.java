package com.larena.boxbreaker.plugin.bbk.templates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.larena.boxbreaker.plugin.bbk.BbkFileType;
import com.larena.boxbreaker.plugin.bbk.completion.BbkCompletionPatterns;
import org.jetbrains.annotations.NotNull;

/**
 * Narrower template context: cursor is at module top level (outside any procedure
 * body, parameter list, or modifier). Used by templates like {@code dcls}, {@code dclc},
 * {@code dclds}, {@code dclf}, {@code dclpr}, {@code proc}, {@code ctlopt}.
 */
public class BbkTopLevelTemplateContext extends BbkLiveTemplateContext {

    public BbkTopLevelTemplateContext() {
        super("BBK Top Level");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext context) {
        PsiFile file = context.getFile();
        if (!file.getFileType().equals(BbkFileType.INSTANCE)) return false;
        PsiElement el = file.findElementAt(context.getStartOffset());
        if (el == null) return true; // empty file → top-level position
        return BbkCompletionPatterns.atTopLevelItemStart(el);
    }
}
