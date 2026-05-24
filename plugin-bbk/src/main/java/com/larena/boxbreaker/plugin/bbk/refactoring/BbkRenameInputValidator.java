package com.larena.boxbreaker.plugin.bbk.refactoring;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Rejects rename targets that:
 *
 * <ul>
 *   <li>match a BBK reserved word (case-insensitive)</li>
 *   <li>are not a valid BBK identifier (must match {@code [a-zA-Z_][a-zA-Z0-9_]*})</li>
 * </ul>
 *
 * <p>Rejection shows inline in the Rename dialog and blocks OK.
 */
public class BbkRenameInputValidator implements RenameInputValidator {

    private static final Pattern VALID_IDENT = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public @NotNull ElementPattern<? extends PsiElement> getPattern() {
        // Apply to any PsiNamedElement (i.e., every named BBK declaration).
        return PlatformPatterns.psiElement(PsiNamedElement.class);
    }

    @Override
    public boolean isInputValid(@NotNull String newName,
                                @NotNull PsiElement element,
                                @NotNull ProcessingContext context) {
        if (newName.isEmpty()) return false;
        if (!VALID_IDENT.matcher(newName).matches()) return false;
        if (BbkReservedWords.isReserved(newName)) return false;
        return true;
    }
}
