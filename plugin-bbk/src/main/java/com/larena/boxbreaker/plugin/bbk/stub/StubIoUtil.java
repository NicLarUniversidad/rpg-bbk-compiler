package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

final class StubIoUtil {

    private StubIoUtil() {}

    static void writeNullable(@NotNull StubOutputStream out, @Nullable String value) throws IOException {
        out.writeName(value);
    }

    static @Nullable String readNullable(@NotNull StubInputStream in) throws IOException {
        StringRef ref = in.readName();
        return ref == null ? null : ref.getString();
    }
}
