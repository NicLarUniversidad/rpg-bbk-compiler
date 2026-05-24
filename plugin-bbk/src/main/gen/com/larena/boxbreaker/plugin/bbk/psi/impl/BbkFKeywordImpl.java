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

public class BbkFKeywordImpl extends BbkPsiElementBase implements BbkFKeyword {

  public BbkFKeywordImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitFKeyword(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkExtfileFKeyword getExtfileFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkExtfileFKeyword.class);
  }

  @Override
  @Nullable
  public BbkExtnameFKeyword getExtnameFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkExtnameFKeyword.class);
  }

  @Override
  @Nullable
  public BbkInddsFKeyword getInddsFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkInddsFKeyword.class);
  }

  @Override
  @Nullable
  public BbkInfdsFKeyword getInfdsFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkInfdsFKeyword.class);
  }

  @Override
  @Nullable
  public BbkPrefixFKeyword getPrefixFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkPrefixFKeyword.class);
  }

  @Override
  @Nullable
  public BbkRenameFKeyword getRenameFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkRenameFKeyword.class);
  }

  @Override
  @Nullable
  public BbkSimpleFKeyword getSimpleFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkSimpleFKeyword.class);
  }

  @Override
  @Nullable
  public BbkUsageFKeyword getUsageFKeyword() {
    return PsiTreeUtil.getChildOfType(this, BbkUsageFKeyword.class);
  }

}
