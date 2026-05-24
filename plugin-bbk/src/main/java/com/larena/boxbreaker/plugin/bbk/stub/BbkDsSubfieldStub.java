package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDsSubfield;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkDsSubfieldStub extends StubBase<BbkDsSubfield>
        implements NamedStub<BbkDsSubfield> {

    private final @Nullable String name;
    private final @Nullable String typeText;

    public BbkDsSubfieldStub(@Nullable StubElement<?> parent,
                              @NotNull IStubElementType<?, ?> type,
                              @Nullable String name,
                              @Nullable String typeText) {
        super(parent, type);
        this.name = name;
        this.typeText = typeText;
    }

    @Override public @Nullable String getName() { return name; }
    public @Nullable String getTypeText() { return typeText; }
}
