package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkVariableDeclarationStub extends StubBase<BbkVariableDeclaration>
        implements NamedStub<BbkVariableDeclaration> {

    private final @Nullable String name;
    private final @Nullable String typeText;

    public BbkVariableDeclarationStub(@Nullable StubElement<?> parent,
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
