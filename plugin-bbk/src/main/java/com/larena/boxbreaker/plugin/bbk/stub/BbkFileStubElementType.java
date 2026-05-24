package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.tree.IStubFileElementType;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.psi.BbkFile;
import org.jetbrains.annotations.NotNull;

/**
 * File-level element type for BBK that opts the language into IntelliJ's stub
 * infrastructure.
 *
 * <p>Bump {@link #VERSION} on every BBK stub schema change so IntelliJ invalidates
 * the persistent index.
 */
public class BbkFileStubElementType extends IStubFileElementType<BbkFileStub> {

    public static final int VERSION = 1;

    public BbkFileStubElementType() {
        super("BBK_FILE", BbkLanguage.INSTANCE);
    }

    @Override
    public int getStubVersion() {
        return VERSION;
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.file";
    }

    @Override
    public @NotNull StubBuilder getBuilder() {
        return new DefaultStubBuilder() {
            @Override
            protected @NotNull com.intellij.psi.stubs.StubElement createStubForFile(@NotNull PsiFile file) {
                if (file instanceof BbkFile bbk) {
                    return new BbkFileStub(bbk);
                }
                return super.createStubForFile(file);
            }
        };
    }
}
