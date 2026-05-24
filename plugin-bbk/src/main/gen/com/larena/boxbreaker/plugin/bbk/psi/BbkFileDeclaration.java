// This is a generated file. Not intended for manual editing.
package com.larena.boxbreaker.plugin.bbk.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.larena.boxbreaker.plugin.bbk.stub.BbkFileDeclarationStub;

public interface BbkFileDeclaration extends PsiNamedElement, StubBasedPsiElement<BbkFileDeclarationStub> {

  @NotNull
  List<BbkFKeyword> getFKeywordList();

  @Nullable
  PsiElement getIdent();

}
