package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkFileDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkFileDeclarationStub extends StubBase<BbkFileDeclaration>
        implements NamedStub<BbkFileDeclaration> {

    private final @Nullable String name;
    private final @Nullable String usageText;
    private final @Nullable String externalName;

    public BbkFileDeclarationStub(@Nullable StubElement<?> parent,
                                   @NotNull IStubElementType<?, ?> type,
                                   @Nullable String name,
                                   @Nullable String usageText,
                                   @Nullable String externalName) {
        super(parent, type);
        this.name = name;
        this.usageText = usageText;
        this.externalName = externalName;
    }

    @Override public @Nullable String getName() { return name; }
    public @Nullable String getUsageText() { return usageText; }
    public @Nullable String getExternalName() { return externalName; }
}
