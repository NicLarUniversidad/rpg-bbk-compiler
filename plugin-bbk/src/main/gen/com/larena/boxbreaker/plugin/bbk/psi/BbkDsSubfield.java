// This is a generated file. Not intended for manual editing.
package com.larena.boxbreaker.plugin.bbk.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.larena.boxbreaker.plugin.bbk.stub.BbkDsSubfieldStub;

public interface BbkDsSubfield extends PsiNamedElement, StubBasedPsiElement<BbkDsSubfieldStub> {

  @Nullable
  BbkTypeSpecification getTypeSpecification();

  @NotNull
  List<BbkVarModifier> getVarModifierList();

  @NotNull
  PsiElement getIdent();

}
