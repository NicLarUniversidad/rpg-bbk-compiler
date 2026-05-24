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
import com.larena.boxbreaker.plugin.bbk.stub.BbkConstantDeclarationStub;
import com.larena.boxbreaker.plugin.bbk.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class BbkConstantDeclarationImpl extends BbkStubBasedNamedElementMixin<BbkConstantDeclarationStub> implements BbkConstantDeclaration {

  public BbkConstantDeclarationImpl(ASTNode node) {
    super(node);
  }

  public BbkConstantDeclarationImpl(BbkConstantDeclarationStub stub, IStubElementType stubType) {
    super(stub, stubType);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitConstantDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkConstantValue getConstantValue() {
    return PsiTreeUtil.getChildOfType(this, BbkConstantValue.class);
  }

  @Override
  @Nullable
  public PsiElement getIdent() {
    return findChildByType(IDENT);
  }

}
