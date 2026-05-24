package com.larena.boxbreaker.plugin.bbk.stub;

import com.intellij.psi.stubs.IStubElementType;

/**
 * Factory used by Grammar-Kit when a BNF rule declares
 * {@code elementTypeFactory="com.larena.boxbreaker.plugin.bbk.stub.BbkStubElementTypeFactory.factory"}.
 */
public final class BbkStubElementTypeFactory {

    private BbkStubElementTypeFactory() {}

    public static IStubElementType<?, ?> factory(String name) {
        return switch (name) {
            case "VARIABLE_DECLARATION"        -> BbkStubElementTypes.VARIABLE_DECLARATION;
            case "CONSTANT_DECLARATION"        -> BbkStubElementTypes.CONSTANT_DECLARATION;
            case "DATA_STRUCTURE_DECLARATION"  -> BbkStubElementTypes.DATA_STRUCTURE_DECLARATION;
            case "DS_SUBFIELD"                 -> BbkStubElementTypes.DS_SUBFIELD;
            case "PROTOTYPE_DECLARATION"       -> BbkStubElementTypes.PROTOTYPE_DECLARATION;
            case "PROCEDURE_DECLARATION"       -> BbkStubElementTypes.PROCEDURE_DECLARATION;
            case "FILE_DECLARATION"            -> BbkStubElementTypes.FILE_DECLARATION;
            default -> throw new IllegalArgumentException("Unknown BBK stub element type: " + name);
        };
    }
}
