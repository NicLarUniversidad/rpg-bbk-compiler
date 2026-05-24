// This is a generated file. Not intended for manual editing.
package com.larena.boxbreaker.plugin.bbk.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.larena.boxbreaker.plugin.bbk.psi.BbkTypes.*;
import com.larena.boxbreaker.plugin.bbk.psi.BbkStubBasedNamedElementMixin;
import com.larena.boxbreaker.plugin.bbk.stub.BbkPrototypeDeclarationStub;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class BbkPrototypeDeclarationImpl extends BbkStubBasedNamedElementMixin<BbkPrototypeDeclarationStub> implements BbkPrototypeDeclaration {

  public BbkPrototypeDeclarationImpl(ASTNode node) {
    super(node);
  }

  public BbkPrototypeDeclarationImpl(BbkPrototypeDeclarationStub stub, IStubElementType stubType) {
    super(stub, stubType);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitPrototypeDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkInlineParamList getInlineParamList() {
    return PsiTreeUtil.getChildOfType(this, BbkInlineParamList.class);
  }

  @Override
  @NotNull
  public List<BbkPrModifier> getPrModifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BbkPrModifier.class);
  }

  @Override
  @Nullable
  public BbkReturnType getReturnType() {
    return PsiTreeUtil.getChildOfType(this, BbkReturnType.class);
  }

  @Override
  @Nullable
  public PsiElement getIdent() {
    return findChildByType(IDENT);
  }

}
