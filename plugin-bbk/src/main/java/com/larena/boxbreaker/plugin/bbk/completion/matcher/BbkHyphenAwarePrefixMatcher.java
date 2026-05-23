package com.larena.boxbreaker.plugin.bbk.completion.matcher;

import com.intellij.codeInsight.completion.PrefixMatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Prefix matcher that accepts three input styles for hyphenated BBK keywords:
 *
 * <ul>
 *   <li><b>Literal prefix.</b> {@code dcl-s} matches {@code DCL-S}.</li>
 *   <li><b>No-hyphen prefix.</b> {@code dcls} matches {@code DCL-S} (the {@code -}
 *       in the candidate is ignored for the comparison).</li>
 *   <li><b>CamelHumps with hyphen.</b> {@code d-s} matches {@code DCL-S}; {@code d-c}
 *       matches {@code DCL-C}; {@code p-i} matches {@code PRE-IF}, etc.</li>
 * </ul>
 *
 * <p>All comparisons are case-insensitive (BBK is case-insensitive).
 */
public class BbkHyphenAwarePrefixMatcher extends PrefixMatcher {

    public BbkHyphenAwarePrefixMatcher(@NotNull String prefix) {
        super(prefix);
    }

    @Override
    public boolean prefixMatches(@NotNull String name) {
        String p = getPrefix();
        if (p.isEmpty()) return true;

        String pLower = p.toLowerCase();
        String nLower = name.toLowerCase();

        // Literal prefix (case-insensitive)
        if (nLower.startsWith(pLower)) return true;

        // No-hyphen prefix: compare both with hyphens stripped
        String pStripped = pLower.replace("-", "");
        String nStripped = nLower.replace("-", "");
        if (!pStripped.isEmpty() && nStripped.startsWith(pStripped)) return true;

        // CamelHumps with hyphen: each char of pLower must match the first char of the
        // corresponding hyphen-separated segment of nLower.
        if (pLower.contains("-")) {
            String[] pSegs = pLower.split("-", -1);
            String[] nSegs = nLower.split("-", -1);
            if (pSegs.length <= nSegs.length) {
                boolean ok = true;
                for (int i = 0; i < pSegs.length; i++) {
                    if (pSegs[i].isEmpty()) continue;
                    if (!nSegs[i].startsWith(pSegs[i])) { ok = false; break; }
                }
                if (ok) return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
        return new BbkHyphenAwarePrefixMatcher(prefix);
    }
}
