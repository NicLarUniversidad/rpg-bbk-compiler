package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkVariableDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkVariableIndex extends StringStubIndexExtension<BbkVariableDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkVariableDeclaration> getKey() {
        return BbkIndexKeys.VARIABLE;
    }
}
