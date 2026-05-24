package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkProcedureDeclaration;
import org.jetbrains.annotations.NotNull;

public class BbkProcedureIndex extends StringStubIndexExtension<BbkProcedureDeclaration> {
    @Override
    public @NotNull StubIndexKey<String, BbkProcedureDeclaration> getKey() {
        return BbkIndexKeys.PROCEDURE;
    }
}
