package com.larena.boxbreaker.plugin.bbk.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * Cached icon registry for the BBK plugin.
 *
 * <p>Block A reuses the single {@code /icons/bbk.svg} for every completion category.
 * If differentiated icons (keyword / type / modifier / directive / file-op / star-ident)
 * are added later, expose them here as separate fields and update the providers'
 * {@link #forCategory(Category)} lookup.
 */
public final class BbkIcons {

    public enum Category {
        KEYWORD, TYPE, MODIFIER, FILE_KEYWORD, DIRECTIVE, FILE_OP, STAR_IDENT, BIF, STATEMENT
    }

    public static final Icon FILE = load("/icons/bbk.svg");

    private BbkIcons() {}

    public static Icon forCategory(Category category) {
        // Single shared icon for now; see TODO in classes.md §7.1.
        return FILE;
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, BbkIcons.class);
    }
}
