package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.BbkVariableDeclaration;
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkVariableDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkVariableDeclarationStubElementType
        extends IStubElementType<BbkVariableDeclarationStub, BbkVariableDeclaration> {

    public BbkVariableDeclarationStubElementType() {
        super("BBK_VARIABLE_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkVariableDeclaration createPsi(@NotNull BbkVariableDeclarationStub stub) {
        return new BbkVariableDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkVariableDeclarationStub createStub(@NotNull BbkVariableDeclaration psi,
                                                           StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        String name = psi.getName();
        String typeText = Optional.ofNullable(psi.getNode().findChildByType(
            com.larena.boxbreaker.plugin.bbk.psi.BbkTypes.TYPE_SPECIFICATION))
            .map(n -> n.getText())
            .orElse(null);
        return new BbkVariableDeclarationStub(parentStub, this, name, typeText);
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.variableDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkVariableDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getTypeText());
    }

    @Override
    public @NotNull BbkVariableDeclarationStub deserialize(@NotNull StubInputStream in,
                                                            StubElement parentStub) throws IOException {
        String name = readNullable(in);
        String typeText = readNullable(in);
        return new BbkVariableDeclarationStub(parentStub, this, name, typeText);
    }

    @Override
    public void indexStub(@NotNull BbkVariableDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.VARIABLE, name.toLowerCase());
        }
    }
}
