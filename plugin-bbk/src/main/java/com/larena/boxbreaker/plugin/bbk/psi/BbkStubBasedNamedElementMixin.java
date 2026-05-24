package com.larena.boxbreaker.plugin.bbk.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin parent for BBK declarations that are <em>stub-backed</em> (visible across
 * files via the project-wide index): DCL-S, DCL-C, DCL-DS, DS subfield, DCL-PR,
 * DCL-PROC, DCL-F.
 *
 * <p>Grammar-Kit emits each stub-backed PSI impl with two constructors — one from an
 * AST node, one from a stub. This mixin provides both so the generated impls can call
 * {@code super(...)} unchanged.
 *
 * <p>{@link #getName()} prefers reading the name from the stub (cheap, no PSI build)
 * when one is available; otherwise it falls back to walking the AST for the IDENT child.
 * That fallback is the same as {@link BbkNamedElementMixin}'s.
 *
 * <p>Non-stub-backed named declarations (inline parameters, subroutines) keep using
 * {@link BbkNamedElementMixin} — extending {@link StubBasedPsiElementBase} for items
 * that never have a stub would force pointless stub infrastructure on them.
 */
public abstract class BbkStubBasedNamedElementMixin<S extends StubElement<?>>
        extends StubBasedPsiElementBase<S> implements PsiNameIdentifierOwner {

    protected BbkStubBasedNamedElementMixin(@NotNull ASTNode node) {
        super(node);
    }

    protected BbkStubBasedNamedElementMixin(@NotNull S stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    @Override
    public @Nullable String getName() {
        S stub = getStub();
        if (stub instanceof NamedStub<?> named) {
            return named.getName();
        }
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode node = getNode().findChildByType(BbkTypes.IDENT);
        return node != null ? node.getPsi() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return this;
    }

    /**
     * Delegate to the reference registry so {@link com.larena.boxbreaker.plugin.bbk.reference.BbkReferenceContributor}
     * can attach references to these elements.
     */
    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
