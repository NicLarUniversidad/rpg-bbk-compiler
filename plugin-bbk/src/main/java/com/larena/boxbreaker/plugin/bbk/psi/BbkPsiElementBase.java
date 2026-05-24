package com.larena.boxbreaker.plugin.bbk.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for every BBK PSI element that is NOT stub-backed and NOT a named
 * declaration (those go through {@link BbkStubBasedNamedElementMixin} and
 * {@link BbkNamedElementMixin} respectively).
 *
 * <p>The whole point of this class is to override {@link #getReferences()} so it
 * actually delegates to the {@link ReferenceProvidersRegistry}. By default
 * {@code ASTWrapperPsiElement.getReferences()} returns an empty array — it does not
 * consult the registry. That breaks {@code findReferenceAt(offset)} (the API behind
 * {@code Ctrl+B} / {@code Ctrl+Click}) for every composite that holds an IDENT but
 * does not own a "direct" {@code getReference()} singular method.
 *
 * <p>Routing through the registry here makes all BBK composites (BbkPrimary,
 * BbkPostfixSuffix, BbkLikeReference, BbkExsrStatement, ...) eligible to receive
 * references from {@link com.larena.boxbreaker.plugin.bbk.reference.BbkReferenceContributor}.
 */
public class BbkPsiElementBase extends ASTWrapperPsiElement {

    public BbkPsiElementBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
