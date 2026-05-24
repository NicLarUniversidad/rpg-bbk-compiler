package com.larena.boxbreaker.plugin.bbk.index;

import com.intellij.psi.stubs.StubIndexKey;
import com.larena.boxbreaker.plugin.bbk.psi.*;

public final class BbkIndexKeys {

    public static final StubIndexKey<String, BbkVariableDeclaration> VARIABLE =
        StubIndexKey.createIndexKey("bbk.variable.shortName");

    public static final StubIndexKey<String, BbkConstantDeclaration> CONSTANT =
        StubIndexKey.createIndexKey("bbk.constant.shortName");

    public static final StubIndexKey<String, BbkDataStructureDeclaration> DATA_STRUCTURE =
        StubIndexKey.createIndexKey("bbk.dataStructure.shortName");

    public static final StubIndexKey<String, BbkDsSubfield> DS_SUBFIELD =
        StubIndexKey.createIndexKey("bbk.dsSubfield.shortName");

    public static final StubIndexKey<String, BbkPrototypeDeclaration> PROTOTYPE =
        StubIndexKey.createIndexKey("bbk.prototype.shortName");

    public static final StubIndexKey<String, BbkProcedureDeclaration> PROCEDURE =
        StubIndexKey.createIndexKey("bbk.procedure.shortName");

    public static final StubIndexKey<String, BbkFileDeclaration> FILE_DECLARATION =
        StubIndexKey.createIndexKey("bbk.fileDeclaration.shortName");

    private BbkIndexKeys() {}
}
