package com.larena.boxbreaker.plugin.bbk.completion.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.ProcessingContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.BbkBundle;
import com.larena.boxbreaker.plugin.bbk.completion.matcher.BbkHyphenAwarePrefixMatcher;
import com.larena.boxbreaker.plugin.bbk.icons.BbkIcons;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import com.larena.boxbreaker.plugin.bbk.scope.BbkScopeWalker;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Emits every visible user-declared identifier as a completion suggestion:
 * variables, constants, procedures, prototypes, files, data structures and
 * (non-qualified) subfields. Subroutines are excluded — they appear only
 * after {@code exsr} (see {@link BbkSubroutineReference}'s variants).
 *
 * <p>The category label (variable / constant / procedure / ...) is shown as the
 * lookup's type text, so the user can tell at a glance what kind of entity each
 * suggestion is. Tail text reproduces the declaration's signature for procedures
 * and prototypes.
 */
public class BbkScopeCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        String prefix = BbkKeywordProviderBase.computeBbkPrefix(parameters);
        CompletionResultSet wrapped = result.withPrefixMatcher(new BbkHyphenAwarePrefixMatcher(prefix));

        Set<String> emittedKeys = new HashSet<>();

        // 1) Local scope (Block B).
        for (PsiNamedElement decl : BbkScopeWalker.allVisible(position)) {
            String name = decl.getName();
            if (name == null || name.isEmpty()) continue;
            String key = decl.getClass().getName() + "::" + name.toLowerCase();
            if (emittedKeys.add(key)) wrapped.addElement(buildLookup(decl, name));
        }

        // 2) Cross-file: walk each index and emit anything not already in local scope.
        Project project = position.getProject();
        emitFromIndex(project, BbkIndexKeys.PROCEDURE,      BbkProcedureDeclaration.class,     emittedKeys, wrapped);
        emitFromIndex(project, BbkIndexKeys.PROTOTYPE,      BbkPrototypeDeclaration.class,     emittedKeys, wrapped);
        emitFromIndex(project, BbkIndexKeys.CONSTANT,       BbkConstantDeclaration.class,      emittedKeys, wrapped);
        emitFromIndex(project, BbkIndexKeys.VARIABLE,       BbkVariableDeclaration.class,      emittedKeys, wrapped);
        emitFromIndex(project, BbkIndexKeys.DATA_STRUCTURE, BbkDataStructureDeclaration.class, emittedKeys, wrapped);
        emitFromIndex(project, BbkIndexKeys.FILE_DECLARATION, BbkFileDeclaration.class,        emittedKeys, wrapped);
    }

    private <T extends PsiNamedElement> void emitFromIndex(
            @NotNull Project project,
            @NotNull StubIndexKey<String, T> key,
            @NotNull Class<T> clazz,
            @NotNull Set<String> emittedKeys,
            @NotNull CompletionResultSet wrapped) {
        for (String name : StubIndex.getInstance().getAllKeys(key, project)) {
            for (T decl : StubIndex.getElements(key, name, project,
                    GlobalSearchScope.allScope(project), clazz)) {
                String emittedKey = clazz.getName() + "::" + name.toLowerCase();
                if (emittedKeys.add(emittedKey)) {
                    String declName = decl.getName();
                    if (declName != null && !declName.isEmpty()) {
                        wrapped.addElement(buildLookup(decl, declName));
                    }
                }
            }
        }
    }

    private static LookupElementBuilder buildLookup(@NotNull PsiNamedElement decl, @NotNull String name) {
        LookupElementBuilder b = LookupElementBuilder.create(decl, name)
            .withCaseSensitivity(false)
            .withIcon(BbkIcons.forCategory(categoryFor(decl)))
            .withTypeText(typeTextFor(decl));
        String tail = tailTextFor(decl);
        if (tail != null) b = b.withTailText(tail, true);
        return b;
    }

    private static BbkIcons.Category categoryFor(@NotNull PsiNamedElement decl) {
        if (decl instanceof BbkProcedureDeclaration || decl instanceof BbkPrototypeDeclaration) {
            return BbkIcons.Category.KEYWORD;
        }
        if (decl instanceof BbkFileDeclaration) return BbkIcons.Category.FILE_KEYWORD;
        if (decl instanceof BbkDataStructureDeclaration) return BbkIcons.Category.TYPE;
        if (decl instanceof BbkConstantDeclaration) return BbkIcons.Category.MODIFIER;
        return BbkIcons.Category.STATEMENT;
    }

    private static @NotNull String typeTextFor(@NotNull PsiNamedElement decl) {
        if (decl instanceof BbkVariableDeclaration)       return BbkBundle.message("completion.type.userVariable");
        if (decl instanceof BbkConstantDeclaration)       return BbkBundle.message("completion.type.userConstant");
        if (decl instanceof BbkProcedureDeclaration)      return BbkBundle.message("completion.type.userProcedure");
        if (decl instanceof BbkPrototypeDeclaration)      return BbkBundle.message("completion.type.userPrototype");
        if (decl instanceof BbkDataStructureDeclaration)  return BbkBundle.message("completion.type.userDataStruct");
        if (decl instanceof BbkDsSubfield)                return BbkBundle.message("completion.type.userSubfield");
        if (decl instanceof BbkFileDeclaration)           return BbkBundle.message("completion.type.userFile");
        if (decl instanceof BbkInlineParam)               return BbkBundle.message("completion.type.userVariable");
        return "";
    }

    private static String tailTextFor(@NotNull PsiNamedElement decl) {
        if (decl instanceof BbkProcedureDeclaration p) {
            BbkInlineParamList params = com.intellij.psi.util.PsiTreeUtil.findChildOfType(p, BbkInlineParamList.class);
            return params != null ? params.getText() : "()";
        }
        if (decl instanceof BbkPrototypeDeclaration p) {
            BbkInlineParamList params = com.intellij.psi.util.PsiTreeUtil.findChildOfType(p, BbkInlineParamList.class);
            return params != null ? params.getText() : "()";
        }
        return null;
    }
}
