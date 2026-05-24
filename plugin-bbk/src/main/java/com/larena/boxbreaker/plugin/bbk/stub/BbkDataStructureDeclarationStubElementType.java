package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.PsiTreeUtil;
import com.larena.boxbreaker.plugin.bbk.BbkLanguage;
import com.larena.boxbreaker.plugin.bbk.index.BbkIndexKeys;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDataStructureDeclaration;
import com.larena.boxbreaker.plugin.bbk.psi.BbkDsModifier;
import com.larena.boxbreaker.plugin.bbk.psi.BbkExtnameDsModifier;
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkDataStructureDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkDataStructureDeclarationStubElementType
        extends IStubElementType<BbkDataStructureDeclarationStub, BbkDataStructureDeclaration> {

    public BbkDataStructureDeclarationStubElementType() {
        super("BBK_DATA_STRUCTURE_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkDataStructureDeclaration createPsi(@NotNull BbkDataStructureDeclarationStub stub) {
        return new BbkDataStructureDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkDataStructureDeclarationStub createStub(@NotNull BbkDataStructureDeclaration psi,
                                                                StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        return new BbkDataStructureDeclarationStub(parentStub, this,
            psi.getName(),
            isQualified(psi),
            extractExtName(psi));
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.dataStructureDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkDataStructureDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        out.writeBoolean(stub.isQualified());
        writeNullable(out, stub.getExtName());
    }

    @Override
    public @NotNull BbkDataStructureDeclarationStub deserialize(@NotNull StubInputStream in,
                                                                 StubElement parentStub) throws IOException {
        String name = readNullable(in);
        boolean qualified = in.readBoolean();
        String extName = readNullable(in);
        return new BbkDataStructureDeclarationStub(parentStub, this, name, qualified, extName);
    }

    @Override
    public void indexStub(@NotNull BbkDataStructureDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.DATA_STRUCTURE, name.toLowerCase());
        }
    }

    private static boolean isQualified(@NotNull BbkDataStructureDeclaration ds) {
        for (BbkDsModifier mod : PsiTreeUtil.findChildrenOfType(ds, BbkDsModifier.class)) {
            String text = mod.getText();
            if (text != null && text.toUpperCase().startsWith("QUALIFIED")) return true;
        }
        return false;
    }

    private static @Nullable String extractExtName(@NotNull BbkDataStructureDeclaration ds) {
        BbkExtnameDsModifier ext = PsiTreeUtil.findChildOfType(ds, BbkExtnameDsModifier.class);
        if (ext == null) return null;
        String text = ext.getText();
        int open = text.indexOf('"');
        int close = text.lastIndexOf('"');
        if (open >= 0 && close > open) return text.substring(open + 1, close);
        return null;
    }
}
