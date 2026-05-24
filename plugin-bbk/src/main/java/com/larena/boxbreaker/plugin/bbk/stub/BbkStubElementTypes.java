package com.larena.boxbreaker.plugin.bbk.stub;

/**
 * Central holder for every BBK stub element type. Referenced by the
 * {@code stubElementTypeHolder} extension in {@code plugin.xml}.
 */
public interface BbkStubElementTypes {

    BbkVariableDeclarationStubElementType     VARIABLE_DECLARATION       = new BbkVariableDeclarationStubElementType();
    BbkConstantDeclarationStubElementType     CONSTANT_DECLARATION       = new BbkConstantDeclarationStubElementType();
    BbkDataStructureDeclarationStubElementType DATA_STRUCTURE_DECLARATION = new BbkDataStructureDeclarationStubElementType();
    BbkDsSubfieldStubElementType              DS_SUBFIELD                = new BbkDsSubfieldStubElementType();
    BbkPrototypeDeclarationStubElementType    PROTOTYPE_DECLARATION      = new BbkPrototypeDeclarationStubElementType();
    BbkProcedureDeclarationStubElementType    PROCEDURE_DECLARATION      = new BbkProcedureDeclarationStubElementType();
    BbkFileDeclarationStubElementType         FILE_DECLARATION           = new BbkFileDeclarationStubElementType();
}
