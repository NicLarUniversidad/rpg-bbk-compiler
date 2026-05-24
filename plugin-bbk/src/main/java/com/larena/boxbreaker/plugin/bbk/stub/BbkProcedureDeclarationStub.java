package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.larena.boxbreaker.plugin.bbk.psi.BbkProcedureDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BbkProcedureDeclarationStub extends StubBase<BbkProcedureDeclaration>
        implements NamedStub<BbkProcedureDeclaration> {

    private final @Nullable String name;
    private final @Nullable String paramsText;
    private final @Nullable String returnTypeText;
    private final boolean exported;
    private final @Nullable String externalName;

    public BbkProcedureDeclarationStub(@Nullable StubElement<?> parent,
                                        @NotNull IStubElementType<?, ?> type,
                                        @Nullable String name,
                                        @Nullable String paramsText,
                                        @Nullable String returnTypeText,
                                        boolean exported,
                                        @Nullable String externalName) {
        super(parent, type);
        this.name = name;
        this.paramsText = paramsText;
        this.returnTypeText = returnTypeText;
        this.exported = exported;
        this.externalName = externalName;
    }

    @Override public @Nullable String getName() { return name; }
    public @Nullable String getParamsText() { return paramsText; }
    public @Nullable String getReturnTypeText() { return returnTypeText; }
    public boolean isExported() { return exported; }
    public @Nullable String getExternalName() { return externalName; }
}
