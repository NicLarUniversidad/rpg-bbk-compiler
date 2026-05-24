package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkConstantDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkConstantIndex extends StringStubIndexExtension<BbkConstantDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkConstantDeclaration> getKey() {
        return BbkIndexKeys.CONSTANT;
    }
}
