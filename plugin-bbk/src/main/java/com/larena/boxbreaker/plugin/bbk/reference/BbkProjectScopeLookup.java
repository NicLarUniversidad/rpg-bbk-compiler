package com.larena.boxbreaker.plugin.bbk.reference;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BbkProjectScopeLookup {

    private BbkProjectScopeLookup() {}

    public static <T extends PsiElement> @NotNull List<T> findInProject(
            @NotNull Project project,
            @NotNull String name,
            @NotNull StubIndexKey<String, T> indexKey,
            @NotNull Class<T> clazz) {
        Collection<T> hits = StubIndex.getElements(
            indexKey, name.toLowerCase(), project,
            GlobalSearchScope.allScope(project), clazz);
        return new ArrayList<>(hits);
    }

    public static <T extends PsiElement> T findFirst(
            @NotNull Project project,
            @NotNull String name,
            @NotNull StubIndexKey<String, T> indexKey,
            @NotNull Class<T> clazz) {
        List<T> all = findInProject(project, name, indexKey, clazz);
        return all.isEmpty() ? null : all.get(0);
    }
}
