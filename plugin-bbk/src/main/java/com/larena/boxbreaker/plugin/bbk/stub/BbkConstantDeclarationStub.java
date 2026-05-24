package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkConstantDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkConstantDeclarationStub extends StubBase<BbkConstantDeclaration>
        implements NamedStub<BbkConstantDeclaration> {

    private final @Nullable String name;
    private final @Nullable String valueText;

    public BbkConstantDeclarationStub(@Nullable StubElement<?> parent,
                                       @NotNull IStubElementType<?, ?> type,
                                       @Nullable String name,
                                       @Nullable String valueText) {
        super(parent, type);
        this.name = name;
        this.valueText = valueText;
    }

    @Override public @Nullable String getName() { return name; }
    public @Nullable String getValueText() { return valueText; }
}
