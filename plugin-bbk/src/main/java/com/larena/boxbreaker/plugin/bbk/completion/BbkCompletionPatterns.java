package com.larena.boxbreaker.plugin.bbk.completion;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Centralised PSI patterns and ancestor-walk helpers shared by every provider.
 *
 * <p>The {@link #anyBbkElement()} pattern is intentionally broad: each provider does
 * the precise context check inside {@code applies(...)} via the helpers below. This
 * trades a bit of dispatch overhead for robustness against PSI shape variations
 * around the dummy identifier IntelliJ injects at the caret.
 */
public final class BbkCompletionPatterns {

    private BbkCompletionPatterns() {}

    public static PsiElementPattern.Capture<PsiElement> anyBbkElement() {
        return PlatformPatterns.psiElement().withLanguage(com.larena.boxbreaker.plugin.bbk.BbkLanguage.INSTANCE);
    }

    // ----- Ancestor-walk helpers -----

    public static <T extends PsiElement> @Nullable T ancestor(@NotNull PsiElement position, @NotNull Class<T> type) {
        return PsiTreeUtil.getParentOfType(position, type);
    }

    public static <T extends PsiElement> boolean inside(@NotNull PsiElement position, @NotNull Class<T> type) {
        return ancestor(position, type) != null;
    }

    /**
     * True if the caret is in a position where a brand-new top-level item starts:
     * outside any procedure body, DS body, parameter list or modifier, with no
     * structured statement currently containing the caret beyond the file itself.
     */
    public static boolean atTopLevelItemStart(@NotNull PsiElement position) {
        if (inside(position, BbkProcedureDeclaration.class)) {
            // Inside a procedure header (before the body) is also top-level-ish, but for
            // top-level keywords specifically we want strictly module scope.
            BbkBlockStatement body = ancestor(position, BbkBlockStatement.class);
            if (body != null) return false;
        }
        if (inside(position, BbkDsBody.class)) return false;
        if (inside(position, BbkInlineParamList.class)) return false;
        if (inside(position, BbkFileDeclaration.class)) return false;
        if (inside(position, BbkDataStructureDeclaration.class)) return false;
        if (inside(position, BbkVariableDeclaration.class)) return false;
        if (inside(position, BbkConstantDeclaration.class)) return false;
        if (inside(position, BbkPrototypeDeclaration.class)) return false;
        if (inside(position, BbkCtlOptStatement.class)) return false;
        return true;
    }

    /**
     * True if the caret is in a position where a statement keyword (if, while, ...)
     * or a file-op keyword (read, chain, ...) is grammatically valid: inside the
     * {@code { ... }} of a procedure body or any nested block.
     */
    public static boolean atStatementStart(@NotNull PsiElement position) {
        BbkBlockStatement block = ancestor(position, BbkBlockStatement.class);
        if (block == null) return false;
        // Exclude positions still inside an inner declaration/expression.
        return !inside(position, BbkVariableDeclaration.class)
            && !inside(position, BbkConstantDeclaration.class)
            && !inside(position, BbkDataStructureDeclaration.class)
            && !inside(position, BbkInlineParamList.class);
    }

    /** True if the caret is inside a {@code DCL-S} body and the type slot is the next thing expected. */
    public static boolean afterDclSIdent(@NotNull PsiElement position) {
        return inside(position, BbkVariableDeclaration.class)
            && !inside(position, BbkInzModifier.class)
            && !inside(position, BbkDimModifier.class);
    }

    /** True if the caret is inside a {@code DCL-DS} header (before {@code {}). */
    public static boolean insideDsHeader(@NotNull PsiElement position) {
        return inside(position, BbkDataStructureDeclaration.class)
            && !inside(position, BbkDsBody.class);
    }

    /** True if the caret is inside the {@code { ... }} of a DS. */
    public static boolean insideDsBody(@NotNull PsiElement position) {
        return inside(position, BbkDsBody.class);
    }

    /** True if the caret is inside an inline param list (procedure or prototype). */
    public static boolean insideParamList(@NotNull PsiElement position) {
        return inside(position, BbkInlineParamList.class);
    }

    /** True if the caret is inside a {@code DCL-F} declaration. */
    public static boolean insideFileDeclaration(@NotNull PsiElement position) {
        return inside(position, BbkFileDeclaration.class);
    }

    /** True if the caret is inside a {@code DCL-PROC} header (before its body). */
    public static boolean insideProcHeader(@NotNull PsiElement position) {
        BbkProcedureDeclaration proc = ancestor(position, BbkProcedureDeclaration.class);
        if (proc == null) return false;
        return ancestor(position, BbkBlockStatement.class) == null;
    }

    /** True if the caret is inside a {@code DCL-PR} declaration. */
    public static boolean insidePrototype(@NotNull PsiElement position) {
        return inside(position, BbkPrototypeDeclaration.class);
    }

    /** True if the caret is inside a {@code CTL-OPT} statement. */
    public static boolean insideCtlOpt(@NotNull PsiElement position) {
        return inside(position, BbkCtlOptStatement.class);
    }

    /** True if the caret is inside a {@code USAGE(...)} argument list. */
    public static boolean insideUsageArgs(@NotNull PsiElement position) {
        return inside(position, BbkUsageFKeyword.class);
    }

    /** True if the caret is inside an {@code OPTIONS(...)} argument list. */
    public static boolean insideOptionsArgs(@NotNull PsiElement position) {
        return inside(position, BbkOptionsModifier.class);
    }

    /** True if the caret is inside a {@code CTL-OPT} keyword argument list (e.g. {@code DFTACTGRP(*NO)}). */
    public static boolean insideCtlOptArgs(@NotNull PsiElement position) {
        return inside(position, BbkCtlOptArgs.class);
    }

    /** True if the caret is inside the {@code { ... }} of a {@code select} block (between when/other). */
    public static boolean insideSelectBlock(@NotNull PsiElement position) {
        BbkSelectStatement select = ancestor(position, BbkSelectStatement.class);
        if (select == null) return false;
        // Must be inside the select's own block — not nested inside a when's body.
        BbkBlockStatement enclosingBlock = ancestor(position, BbkBlockStatement.class);
        return enclosingBlock != null && enclosingBlock.getParent() == select;
    }

    /** True if the caret is inside a {@code monitor} construct, after the main block. */
    public static boolean afterMonitorBlock(@NotNull PsiElement position) {
        return inside(position, BbkMonitorStatement.class);
    }

    /** True if the caret is inside a {@code begsr ... endsr} subroutine. */
    public static boolean insideSubroutine(@NotNull PsiElement position) {
        return inside(position, BbkSubroutineDefinition.class);
    }
}
