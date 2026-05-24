package com.larena.boxbreaker.plugin.bbk.scope;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Scope of the enclosing {@link BbkProcedureDeclaration}: ONLY the inline parameters.
 *
 * <p>Locals declared in the procedure body (DCL-S, DCL-C, DCL-DS, DCL subroutines) are
 * handled by the {@link BbkBlockScope} that wraps the procedure's body, NOT by this
 * scope. Walking both would double-list every local — that is what made Ctrl+B show a
 * "Choose declaration" popup with duplicate entries.
 */
public class BbkProcedureScope implements BbkScope {

    private final @NotNull BbkProcedureDeclaration procedure;
    private final @NotNull BbkScope parent;

    public BbkProcedureScope(@NotNull BbkProcedureDeclaration procedure, @NotNull BbkScope parent) {
        this.procedure = procedure;
        this.parent = parent;
    }

    @Override
    public @NotNull List<PsiNamedElement> getDeclarations() {
        List<PsiNamedElement> out = new ArrayList<>();

        // Inline parameters only — body locals are reached via BbkBlockScope(s).
        BbkInlineParamList paramList = PsiTreeUtil.findChildOfType(procedure, BbkInlineParamList.class);
        if (paramList != null) {
            for (BbkInlineParam p : PsiTreeUtil.findChildrenOfType(paramList, BbkInlineParam.class)) {
                out.add(p);
            }
        }

        return out;
    }

    @Override
    public @Nullable BbkScope getParent() {
        return parent;
    }
}
