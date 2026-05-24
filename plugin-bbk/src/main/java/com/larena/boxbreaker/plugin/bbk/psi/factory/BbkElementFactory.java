package com.larena.boxbreaker.plugin.bbk.psi.factory;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.larena.boxbreaker.plugin.bbk.BbkFileType;
import com.larena.boxbreaker.plugin.bbk.psi.BbkFile;
import com.larena.boxbreaker.plugin.bbk.psi.BbkTypes;
import com.larena.boxbreaker.plugin.bbk.psi.BbkVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory that synthesises BBK PSI nodes from text. The only correct way in IntelliJ
 * to mint a new PSI element (e.g., a renamed IDENT) is to parse a tiny BBK snippet
 * and pluck the desired node out of the resulting tree.
 *
 * <p>Currently exposes {@link #createIdentifier(Project, String)} for Rename;
 * additional factory methods (literals, type specs, ...) can be added as quick-fixes
 * and intentions appear.
 */
public final class BbkElementFactory {

    private BbkElementFactory() {}

    /**
     * Returns the IDENT leaf for {@code name}. Parses a synthetic
     * {@code DCL-S <name> INT(10);} file and returns the IDENT child of the
     * resulting {@link BbkVariableDeclaration}.
     *
     * @throws IllegalArgumentException if {@code name} cannot be parsed as a valid IDENT
     */
    public static @NotNull PsiElement createIdentifier(@NotNull Project project, @NotNull String name) {
        BbkFile file = createDummyFile(project, "DCL-S " + name + " INT(10);");
        BbkVariableDeclaration decl = PsiTreeUtil.findChildOfType(file, BbkVariableDeclaration.class);
        if (decl == null) {
            throw new IllegalArgumentException("Could not parse '" + name + "' as a BBK identifier");
        }
        com.intellij.lang.ASTNode node = decl.getNode().findChildByType(BbkTypes.IDENT);
        if (node == null) {
            throw new IllegalArgumentException("'" + name + "' did not produce an IDENT leaf");
        }
        return node.getPsi();
    }

    /** Parses arbitrary BBK source as an in-memory file (not persisted). */
    public static @NotNull BbkFile createDummyFile(@NotNull Project project, @NotNull String source) {
        return (BbkFile) PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.bbk", BbkFileType.INSTANCE, source);
    }

    /** Looks up an IDENT leaf inside {@code parent} that exactly matches the given text. */
    public static @Nullable PsiElement findIdent(@NotNull PsiElement parent, @NotNull String text) {
        for (PsiElement child : parent.getChildren()) {
            if (child.getNode().getElementType() == BbkTypes.IDENT
                && text.equals(child.getText())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Finds the IDENT leaf inside {@code composite} whose offset within the composite
     * matches {@code rangeInElement}. Used by references whose owning element is a
     * composite (e.g., BbkPrimary, BbkLikeReference) and whose range points at the
     * IDENT inside.
     */
    public static @Nullable PsiElement findIdentInRange(@NotNull PsiElement composite,
                                                         @NotNull com.intellij.openapi.util.TextRange rangeInElement) {
        com.intellij.lang.ASTNode node = composite.getNode();
        if (node == null) return null;
        com.intellij.lang.ASTNode child = node.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() == BbkTypes.IDENT) {
                com.intellij.openapi.util.TextRange childRange = new com.intellij.openapi.util.TextRange(
                    child.getStartOffset() - node.getStartOffset(),
                    child.getStartOffset() - node.getStartOffset() + child.getTextLength()
                );
                if (childRange.equals(rangeInElement) || rangeInElement.contains(childRange)) {
                    return child.getPsi();
                }
            }
            child = child.getTreeNext();
        }
        // Fallback: any IDENT leaf inside the composite.
        com.intellij.lang.ASTNode any = node.findChildByType(BbkTypes.IDENT);
        return any != null ? any.getPsi() : null;
    }
}
