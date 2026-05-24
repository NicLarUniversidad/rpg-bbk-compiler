package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.PsiTreeUtil;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkProcedureDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkProcedureDeclarationStubElementType
        extends IStubElementType<BbkProcedureDeclarationStub, BbkProcedureDeclaration> {

    public BbkProcedureDeclarationStubElementType() {
        super("BBK_PROCEDURE_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkProcedureDeclaration createPsi(@NotNull BbkProcedureDeclarationStub stub) {
        return new BbkProcedureDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkProcedureDeclarationStub createStub(@NotNull BbkProcedureDeclaration psi,
                                                            StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        BbkInlineParamList params = PsiTreeUtil.findChildOfType(psi, BbkInlineParamList.class);
        BbkReturnType ret = PsiTreeUtil.findChildOfType(psi, BbkReturnType.class);
        boolean exported = PsiTreeUtil.findChildOfType(psi, BbkExportModifier.class) != null;
        BbkExtprocModifier extproc = PsiTreeUtil.findChildOfType(psi, BbkExtprocModifier.class);
        return new BbkProcedureDeclarationStub(parentStub, this,
            psi.getName(),
            params != null ? params.getText() : null,
            ret != null ? ret.getText() : null,
            exported,
            extproc != null ? extractStringLit(extproc.getText()) : null);
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.procedureDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkProcedureDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getParamsText());
        writeNullable(out, stub.getReturnTypeText());
        out.writeBoolean(stub.isExported());
        writeNullable(out, stub.getExternalName());
    }

    @Override
    public @NotNull BbkProcedureDeclarationStub deserialize(@NotNull StubInputStream in,
                                                             StubElement parentStub) throws IOException {
        String name = readNullable(in);
        String params = readNullable(in);
        String ret = readNullable(in);
        boolean exported = in.readBoolean();
        String ext = readNullable(in);
        return new BbkProcedureDeclarationStub(parentStub, this, name, params, ret, exported, ext);
    }

    @Override
    public void indexStub(@NotNull BbkProcedureDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.PROCEDURE, name.toLowerCase());
        }
        String ext = stub.getExternalName();
        if (ext != null && !ext.isEmpty() && !ext.equalsIgnoreCase(name)) {
            sink.occurrence(BbkIndexKeys.PROCEDURE, ext.toLowerCase());
        }
    }

    private static @Nullable String extractStringLit(@Nullable String text) {
        if (text == null) return null;
        int open = text.indexOf('"');
        int close = text.lastIndexOf('"');
        if (open >= 0 && close > open) return text.substring(open + 1, close);
        return null;
    }
}
