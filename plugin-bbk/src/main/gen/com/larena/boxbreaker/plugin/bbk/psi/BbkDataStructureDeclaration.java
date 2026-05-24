// This is a generated file. Not intended for manual editing.
package com.larena.boxbreaker.plugin.bbk.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.larena.boxbreaker.plugin.bbk.stub.BbkDataStructureDeclarationStub;

public interface BbkDataStructureDeclaration extends PsiNamedElement, StubBasedPsiElement<BbkDataStructureDeclarationStub> {

  @Nullable
  BbkDsBody getDsBody();

  @NotNull
  List<BbkDsModifier> getDsModifierList();

  @Nullable
  PsiElement getIdent();

}
