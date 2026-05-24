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
import com.larena.boxbreaker.plugin.bbk.psi.impl.BbkFileDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.readNullable;
import static com.larena.boxbreaker.plugin.bbk.stub.StubIoUtil.writeNullable;

public class BbkFileDeclarationStubElementType
        extends IStubElementType<BbkFileDeclarationStub, BbkFileDeclaration> {

    public BbkFileDeclarationStubElementType() {
        super("BBK_FILE_DECLARATION", BbkLanguage.INSTANCE);
    }

    @Override
    public BbkFileDeclaration createPsi(@NotNull BbkFileDeclarationStub stub) {
        return new BbkFileDeclarationImpl(stub, this);
    }

    @Override
    public @NotNull BbkFileDeclarationStub createStub(@NotNull BbkFileDeclaration psi,
                                                       StubElement<? extends com.intellij.psi.PsiElement> parentStub) {
        BbkUsageFKeyword usage = PsiTreeUtil.findChildOfType(psi, BbkUsageFKeyword.class);
        BbkExtfileFKeyword extfile = PsiTreeUtil.findChildOfType(psi, BbkExtfileFKeyword.class);
        BbkExtnameFKeyword extname = PsiTreeUtil.findChildOfType(psi, BbkExtnameFKeyword.class);
        String ext = extractStringLit(extfile != null ? extfile.getText() :
                                       (extname != null ? extname.getText() : null));
        return new BbkFileDeclarationStub(parentStub, this,
            psi.getName(),
            usage != null ? usage.getText() : null,
            ext);
    }

    @Override
    public @NotNull String getExternalId() {
        return "bbk.fileDeclaration";
    }

    @Override
    public void serialize(@NotNull BbkFileDeclarationStub stub, @NotNull StubOutputStream out) throws IOException {
        writeNullable(out, stub.getName());
        writeNullable(out, stub.getUsageText());
        writeNullable(out, stub.getExternalName());
    }

    @Override
    public @NotNull BbkFileDeclarationStub deserialize(@NotNull StubInputStream in,
                                                        StubElement parentStub) throws IOException {
        return new BbkFileDeclarationStub(parentStub, this,
            readNullable(in), readNullable(in), readNullable(in));
    }

    @Override
    public void indexStub(@NotNull BbkFileDeclarationStub stub, @NotNull IndexSink sink) {
        String name = stub.getName();
        if (name != null && !name.isEmpty()) {
            sink.occurrence(BbkIndexKeys.FILE_DECLARATION, name.toLowerCase());
        }
        String ext = stub.getExternalName();
        if (ext != null && !ext.isEmpty() && !ext.equalsIgnoreCase(name)) {
            sink.occurrence(BbkIndexKeys.FILE_DECLARATION, ext.toLowerCase());
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
