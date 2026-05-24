package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDataStructureDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkDataStructureIndex extends StringStubIndexExtension<BbkDataStructureDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkDataStructureDeclaration> getKey() {
        return BbkIndexKeys.DATA_STRUCTURE;
    }
}
