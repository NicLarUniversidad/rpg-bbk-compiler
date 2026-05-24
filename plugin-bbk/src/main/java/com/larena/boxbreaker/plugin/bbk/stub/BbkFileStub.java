package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.larena.boxbreaker.plugin.bbk.psi.BbkFile;
import org.jetbrains.annotations.NotNull;

/**
 * Root stub for a whole BBK file. The children of this stub are the top-level
 * declarations that opted in via {@code stubClass=...} in {@code BBK.bnf}.
 */
public class BbkFileStub extends PsiFileStubImpl<BbkFile> {

    public BbkFileStub(@NotNull BbkFile file) {
        super(file);
    }
}
