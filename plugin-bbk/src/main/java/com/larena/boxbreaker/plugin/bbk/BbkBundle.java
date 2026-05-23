package com.larena.boxbreaker.plugin.bbk;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Central bundle for every user-visible string in the BBK plugin.
 *
 * <p>The bundle keeps all completion labels, type texts, template descriptions, etc.
 * in {@code resources/messages/BbkBundle.properties}, so translations or rewording
 * never require recompilation of the providers themselves.
 */
public final class BbkBundle extends DynamicBundle {

    @NonNls public static final String BUNDLE = "messages.BbkBundle";

    private static final BbkBundle INSTANCE = new BbkBundle();

    private BbkBundle() {
        super(BUNDLE);
    }

    public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                          Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}
