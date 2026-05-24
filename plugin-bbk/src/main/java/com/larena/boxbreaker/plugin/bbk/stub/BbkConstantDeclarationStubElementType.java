package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.BbkConstantDeclaration;
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkConstantDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkConstantDeclarationStubElementType
        extends IStubElementType<BbkConstantDeclarationStub, BbkConstantDeclaration> {

    public BbkConstantDeclarationStubElementType() {
        super("BBK_CONSTANT_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkConstantDeclaration createPsi(@NotNull BbkConstantDeclarationStub stub) {
        return new BbkConstantDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkConstantDeclarationStub createStub(@NotNull BbkConstantDeclaration psi,
                                                           StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        String name = psi.getName();
        String valueText = Optional.ofNullable(psi.getNode().findChildByType(
            com.larena.boxbreaker.plugin.bbk.psi.BbkTypes.CONSTANT_VALUE))
            .map(n -> n.getText())
            .orElse(null);
        return new BbkConstantDeclarationStub(parentStub, this, name, valueText);
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.constantDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkConstantDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getValueText());
    }

    @Override
    public @NotNull BbkConstantDeclarationStub deserialize(@NotNull StubInputStream in,
                                                            StubElement parentStub) throws IOException {
        String name = readNullable(in);
        String valueText = readNullable(in);
        return new BbkConstantDeclarationStub(parentStub, this, name, valueText);
    }

    @Override
    public void indexStub(@NotNull BbkConstantDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.CONSTANT, name.toLowerCase());
        }
    }
}
