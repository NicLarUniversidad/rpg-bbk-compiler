package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDsSubfield;
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkDsSubfieldImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkDsSubfieldStubElementType
        extends IStubElementType<BbkDsSubfieldStub, BbkDsSubfield> {

    public BbkDsSubfieldStubElementType() {
        super("BBK_DS_SUBFIELD", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkDsSubfield createPsi(@NotNull BbkDsSubfieldStub stub) {
        return new BbkDsSubfieldImpl(stub, this);
    }

    @Override
    public @NotNull BbkDsSubfieldStub createStub(@NotNull BbkDsSubfield psi,
                                                  StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        String name = psi.getName();
        String typeText = Optional.ofNullable(psi.getNode().findChildByType(
            com.larena.boxbreaker.plugin.bbk.psi.BbkTypes.TYPE_SPECIFICATION))
            .map(n -> n.getText())
            .orElse(null);
        return new BbkDsSubfieldStub(parentStub, this, name, typeText);
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.dsSubfield";
    }

    @Override
    public void serialize(@NotNull BbkDsSubfieldStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getTypeText());
    }

    @Override
    public @NotNull BbkDsSubfieldStub deserialize(@NotNull StubInputStream in,
                                                   StubElement parentStub) throws IOException {
        String name = readNullable(in);
        String typeText = readNullable(in);
        return new BbkDsSubfieldStub(parentStub, this, name, typeText);
    }

    @Override
    public void indexStub(@NotNull BbkDsSubfieldStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.DS_SUBFIELD, name.toLowerCase());
        }
    }
}
