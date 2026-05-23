package com.larena.boxbreaker.plugin.bbk.completion.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.larena.boxbreaker.plugin.bbk.completion.Suggestion;
import com.larena.boxbreaker.plugin.bbk.completion.matcher.BbkHyphenAwarePrefixMatcher;
import com.larena.boxbreaker.plugin.bbk.icons.BbkIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Boilerplate-free base for keyword-style completion providers.
 *
 * <p>Each concrete provider only has to (a) decide whether the cursor position belongs
 * to its context via {@link #applies(PsiElement)}, and (b) return the list of suggestions
 * to contribute via {@link #suggestions(PsiElement)}.
 *
 * <p>The base wraps the {@link CompletionResultSet} with {@link BbkHyphenAwarePrefixMatcher}
 * so all providers consistently honour the BBK matching rules (case-insensitive,
 * hyphen-aware, CamelHumps with hyphen).
 */
public abstract class BbkKeywordProviderBase extends CompletionProvider<CompletionParameters> {

    protected abstract boolean applies(@NotNull PsiElement position);

    protected abstract @NotNull List<Suggestion> suggestions(@NotNull PsiElement position);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (!applies(position)) return;

        String prefix = result.getPrefixMatcher().getPrefix();
        CompletionResultSet wrapped = result.withPrefixMatcher(new BbkHyphenAwarePrefixMatcher(prefix));

        for (Suggestion s : suggestions(position)) {
            LookupElementBuilder b = LookupElementBuilder.create(s.lookup())
                .withCaseSensitivity(false)
                .withIcon(BbkIcons.forCategory(s.category()))
                .withTypeText(s.typeText());
            if (s.tail() != null && !s.tail().isEmpty()) {
                b = b.withTailText(s.tail(), true);
            }
            if (s.insertHandler() != null) {
                b = b.withInsertHandler(s.insertHandler());
            }
            wrapped.addElement(b);
        }
    }
}
