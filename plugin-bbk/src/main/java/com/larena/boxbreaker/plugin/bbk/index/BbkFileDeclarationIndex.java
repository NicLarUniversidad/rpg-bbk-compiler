package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkFileDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkFileDeclarationIndex extends StringStubIndexExtension<BbkFileDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkFileDeclaration> getKey() {
        return BbkIndexKeys.FILE_DECLARATION;
    }
}
