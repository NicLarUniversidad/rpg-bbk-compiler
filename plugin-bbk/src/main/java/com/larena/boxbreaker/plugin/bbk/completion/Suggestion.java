package com.larena.boxbreaker.plugin.bbk.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.larena.boxbreaker.plugin.bbk.icons.BbkIcons;
import org.jetbrains.annotations.Nullable;

/**
 * Plain description of one completion suggestion. Used by every keyword-style provider.
 *
 * <p>The {@code lookup} is the keyword/identifier inserted into the editor;
 * {@code tail} is the schematic placeholder shown after the lookup (e.g. {@code (precision)});
 * {@code typeText} is the right-aligned label (e.g. {@code primitive type}).
 *
 * <p>{@code insertHandler} may be {@code null} when no post-insertion logic is needed.
 */
public record Suggestion(
        String lookup,
        @Nullable String tail,
        String typeText,
        BbkIcons.Category category,
        @Nullable InsertHandler<LookupElement> insertHandler
) {
    public static Suggestion plain(String lookup, String typeText, BbkIcons.Category category) {
        return new Suggestion(lookup, null, typeText, category, null);
    }

    public static Suggestion withTail(String lookup, String tail, String typeText,
                                      BbkIcons.Category category) {
        return new Suggestion(lookup, tail, typeText, category, null);
    }

    public static Suggestion withHandler(String lookup, String typeText,
                                         BbkIcons.Category category,
                                         InsertHandler<LookupElement> handler) {
        return new Suggestion(lookup, null, typeText, category, handler);
    }

    public static Suggestion full(String lookup, String tail, String typeText,
                                  BbkIcons.Category category,
                                  InsertHandler<LookupElement> handler) {
        return new Suggestion(lookup, tail, typeText, category, handler);
    }
}
