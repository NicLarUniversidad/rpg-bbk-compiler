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
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkPrototypeDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkPrototypeDeclarationStubElementType
        extends IStubElementType<BbkPrototypeDeclarationStub, BbkPrototypeDeclaration> {

    public BbkPrototypeDeclarationStubElementType() {
        super("BBK_PROTOTYPE_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkPrototypeDeclaration createPsi(@NotNull BbkPrototypeDeclarationStub stub) {
        return new BbkPrototypeDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkPrototypeDeclarationStub createStub(@NotNull BbkPrototypeDeclaration psi,
                                                            StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        BbkInlineParamList params = PsiTreeUtil.findChildOfType(psi, BbkInlineParamList.class);
        BbkReturnType ret = PsiTreeUtil.findChildOfType(psi, BbkReturnType.class);
        BbkExtpgmModifier extpgm = PsiTreeUtil.findChildOfType(psi, BbkExtpgmModifier.class);
        BbkExtprocModifier extproc = PsiTreeUtil.findChildOfType(psi, BbkExtprocModifier.class);
        return new BbkPrototypeDeclarationStub(parentStub, this,
            psi.getName(),
            params != null ? params.getText() : null,
            ret != null ? ret.getText() : null,
            extractStringLit(extpgm != null ? extpgm.getText() : (extproc != null ? extproc.getText() : null)));
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.prototypeDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkPrototypeDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getParamsText());
        writeNullable(out, stub.getReturnTypeText());
        writeNullable(out, stub.getExternalName());
    }

    @Override
    public @NotNull BbkPrototypeDeclarationStub deserialize(@NotNull StubInputStream in,
                                                             StubElement parentStub) throws IOException {
        return new BbkPrototypeDeclarationStub(parentStub, this,
            readNullable(in), readNullable(in), readNullable(in), readNullable(in));
    }

    @Override
    public void indexStub(@NotNull BbkPrototypeDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.PROTOTYPE, name.toLowerCase());
        }
        String ext = stub.getExternalName();
        if (ext != null && !ext.isEmpty() && !ext.equalsIgnoreCase(name)) {
            sink.occurrence(BbkIndexKeys.PROTOTYPE, ext.toLowerCase());
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
