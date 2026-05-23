package com.larena.boxbreaker.plugin.bbk.templates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.larena.boxbreaker.plugin.bbk.BbkFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Base live-template context: any BBK file. Used as the parent context for the
 * narrower {@link BbkTopLevelTemplateContext} and {@link BbkProcedureBodyTemplateContext}.
 */
public class BbkLiveTemplateContext extends TemplateContextType {

    protected BbkLiveTemplateContext(@NotNull String presentableName) {
        super(presentableName);
    }

    public BbkLiveTemplateContext() {
        super("BBK");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext context) {
        return context.getFile().getFileType().equals(BbkFileType.INSTANCE);
    }
}
