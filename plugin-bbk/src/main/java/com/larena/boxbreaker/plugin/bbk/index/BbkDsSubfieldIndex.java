package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDsSubfield;
import org.jetbrains.annotations.NotNull;

public class BbkDsSubfieldIndex extends StringStubIndexExtension<BbkDsSubfield> {
    @Override
    public @NotNull StubIndexKey<String, BbkDsSubfield> getKey() {
        return BbkIndexKeys.DS_SUBFIELD;
    }
}
