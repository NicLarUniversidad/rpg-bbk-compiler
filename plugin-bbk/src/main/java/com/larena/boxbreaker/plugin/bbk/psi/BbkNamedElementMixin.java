package com.larena.boxbreaker.plugin.bbk.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.util.IncorrectOperationException;
import com.larena.boxbreaker.plugin.bbk.psi.factory.BbkElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for every BBK declaration that has a name (DCL-S, DCL-C, DCL-DS, DCL-F,
 * DCL-PR, DCL-PROC, DS subfield, inline param, subroutine).
 *
 * <p>The class is referenced from the {@code mixin=} attribute of each named-declaration
 * rule in {@code BBK.bnf}, so Grammar-Kit emits each generated PSI impl as a subclass
 * of this mixin. Because the implementation of {@code getName} / {@code setName} /
 * {@code getNameIdentifier} is identical for every BBK declaration (find the first
 * {@code IDENT} child), we implement them here once instead of routing through
 * {@code methods=[...]} per rule — Grammar-Kit cannot resolve those during code
 * generation because the utility class is compiled in the same module.
 */
public abstract class BbkNamedElementMixin extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {

    protected BbkNamedElementMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode node = getNode().findChildByType(BbkTypes.IDENT);
        return node != null ? node.getPsi() : null;
    }

    /**
     * Required for IntelliJ's {@code TargetElementUtilBase.getNamedElement} fallback,
     * which gates rename / Go-to-Declaration on
     * {@code parent.getTextOffset() == leaf.getTextRange().getStartOffset()}.
     *
     * <p>The default {@code getTextOffset()} returns the start of the whole composite
     * (e.g., position of {@code DCL-S}). The leaf at the caret is the IDENT inside —
     * those offsets never match, so Rename From Declaration silently fails. Reporting
     * the IDENT's offset here fixes it.
     */
    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getTextOffset() : super.getTextOffset();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement oldId = getNameIdentifier();
        if (oldId == null) return this;
        PsiElement newId = BbkElementFactory.createIdentifier(getProject(), name);
        oldId.replace(newId);
        return this;
    }

    /**
     * Delegate to the reference registry so {@link com.larena.boxbreaker.plugin.bbk.reference.BbkReferenceContributor}
     * can attach references to these elements. See {@link BbkPsiElementBase} for the
     * rationale.
     */
    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
