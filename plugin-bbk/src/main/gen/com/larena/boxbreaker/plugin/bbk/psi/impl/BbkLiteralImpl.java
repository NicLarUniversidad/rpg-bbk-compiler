// This is a generated file. Not intended for manual editing.
package com.larena.boxbreaker.plugin.bbk.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.larena.boxbreaker.plugin.bbk.psi.BbkTypes.*;
import com.larena.boxbreaker.plugin.bbk.psi.BbkPsiElementBase;
import com.larena.boxbreaker.plugin.bbk.psi.*;

public class BbkLiteralImpl extends BbkPsiElementBase implements BbkLiteral {

  public BbkLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getDecLit() {
    return findChildByType(DEC_LIT);
  }

  @Override
  @Nullable
  public PsiElement getFloatLit() {
    return findChildByType(FLOAT_LIT);
  }

  @Override
  @Nullable
  public PsiElement getIntLit() {
    return findChildByType(INT_LIT);
  }

  @Override
  @Nullable
  public PsiElement getIntLitHex() {
    return findChildByType(INT_LIT_HEX);
  }

  @Override
  @Nullable
  public PsiElement getIntLitOct() {
    return findChildByType(INT_LIT_OCT);
  }

  @Override
  @Nullable
  public PsiElement getStrLit() {
    return findChildByType(STR_LIT);
  }

}
