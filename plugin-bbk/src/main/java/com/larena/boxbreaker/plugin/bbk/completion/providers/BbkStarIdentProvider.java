package com.larena.boxbreaker.plugin.bbk.completion.providers;

import com.larena.boxbreaker.plugin.bbk.BbkBundle;
import com.larena.boxbreaker.plugin.bbk.completion.BbkCompletionPatterns;
import com.larena.boxbreaker.plugin.bbk.completion.Suggestion;
import com.larena.boxbreaker.plugin.bbk.icons.BbkIcons;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggests context-specific star identifiers (figurative constants):
 *
 * <ul>
 *   <li>Inside {@code USAGE(...)}: {@code *INPUT}, {@code *OUTPUT}, {@code *UPDATE}, {@code *DELETE}.</li>
 *   <li>Inside {@code OPTIONS(...)}: {@code *NOPASS}, {@code *OMIT}, {@code *VARSIZE},
 *       {@code *STRING}, {@code *NULLIND}.</li>
 *   <li>Inside CTL-OPT keyword args: {@code *NO}, {@code *YES}, {@code *NEW}, {@code *CALLER}.</li>
 * </ul>
 */
public class BbkStarIdentProvider extends BbkKeywordProviderBase {

    @Override
    protected boolean applies(@NotNull PsiElement position) {
        return BbkCompletionPatterns.insideUsageArgs(position)
            || BbkCompletionPatterns.insideOptionsArgs(position)
            || BbkCompletionPatterns.insideCtlOptArgs(position);
    }

    @Override
    protected @NotNull List<Suggestion> suggestions(@NotNull PsiElement position) {
        String type = BbkBundle.message("completion.type.starIdent");
        List<Suggestion> out = new ArrayList<>();

        if (BbkCompletionPatterns.insideUsageArgs(position)) {
            out.add(Suggestion.plain("*INPUT",  type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*OUTPUT", type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*UPDATE", type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*DELETE", type, BbkIcons.Category.STAR_IDENT));
        }
        if (BbkCompletionPatterns.insideOptionsArgs(position)) {
            out.add(Suggestion.plain("*NOPASS",  type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*OMIT",    type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*VARSIZE", type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*STRING",  type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*NULLIND", type, BbkIcons.Category.STAR_IDENT));
        }
        if (BbkCompletionPatterns.insideCtlOptArgs(position)) {
            out.add(Suggestion.plain("*NO",     type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*YES",    type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*NEW",    type, BbkIcons.Category.STAR_IDENT));
            out.add(Suggestion.plain("*CALLER", type, BbkIcons.Category.STAR_IDENT));
        }
        return out;
    }
}
