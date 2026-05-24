package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDataStructureDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkDataStructureDeclarationStub extends StubBase<BbkDataStructureDeclaration>
        implements NamedStub<BbkDataStructureDeclaration> {

    private final @Nullable String name;
    private final boolean qualified;
    private final @Nullable String extName;

    public BbkDataStructureDeclarationStub(@Nullable StubElement<?> parent,
                                            @NotNull IStubElementType<?, ?> type,
                                            @Nullable String name,
                                            boolean qualified,
                                            @Nullable String extName) {
        super(parent, type);
        this.name = name;
        this.qualified = qualified;
        this.extName = extName;
    }

    @Override public @Nullable String getName() { return name; }
    public boolean isQualified() { return qualified; }
    public @Nullable String getExtName() { return extName; }
}
