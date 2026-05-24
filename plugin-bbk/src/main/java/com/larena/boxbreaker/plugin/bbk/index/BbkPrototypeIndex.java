package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkPrototypeDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkPrototypeIndex extends StringStubIndexExtension<BbkPrototypeDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkPrototypeDeclaration> getKey() {
        return BbkIndexKeys.PROTOTYPE;
    }
}
