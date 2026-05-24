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

public class BbkFileOpStatementImpl extends BbkPsiElementBase implements BbkFileOpStatement {

  public BbkFileOpStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitFileOpStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkChainOp getChainOp() {
    return PsiTreeUtil.getChildOfType(this, BbkChainOp.class);
  }

  @Override
  @Nullable
  public BbkCloseOp getCloseOp() {
    return PsiTreeUtil.getChildOfType(this, BbkCloseOp.class);
  }

  @Override
  @Nullable
  public BbkDeleteOp getDeleteOp() {
    return PsiTreeUtil.getChildOfType(this, BbkDeleteOp.class);
  }

  @Override
  @Nullable
  public BbkExfmtOp getExfmtOp() {
    return PsiTreeUtil.getChildOfType(this, BbkExfmtOp.class);
  }

  @Override
  @Nullable
  public BbkOpenOp getOpenOp() {
    return PsiTreeUtil.getChildOfType(this, BbkOpenOp.class);
  }

  @Override
  @Nullable
  public BbkReadOp getReadOp() {
    return PsiTreeUtil.getChildOfType(this, BbkReadOp.class);
  }

  @Override
  @Nullable
  public BbkReadeOp getReadeOp() {
    return PsiTreeUtil.getChildOfType(this, BbkReadeOp.class);
  }

  @Override
  @Nullable
  public BbkReadpOp getReadpOp() {
    return PsiTreeUtil.getChildOfType(this, BbkReadpOp.class);
  }

  @Override
  @Nullable
  public BbkReadpeOp getReadpeOp() {
    return PsiTreeUtil.getChildOfType(this, BbkReadpeOp.class);
  }

  @Override
  @Nullable
  public BbkSetgtOp getSetgtOp() {
    return PsiTreeUtil.getChildOfType(this, BbkSetgtOp.class);
  }

  @Override
  @Nullable
  public BbkSetllOp getSetllOp() {
    return PsiTreeUtil.getChildOfType(this, BbkSetllOp.class);
  }

  @Override
  @Nullable
  public BbkUnlockOp getUnlockOp() {
    return PsiTreeUtil.getChildOfType(this, BbkUnlockOp.class);
  }

  @Override
  @Nullable
  public BbkUpdateOp getUpdateOp() {
    return PsiTreeUtil.getChildOfType(this, BbkUpdateOp.class);
  }

  @Override
  @Nullable
  public BbkWriteOp getWriteOp() {
    return PsiTreeUtil.getChildOfType(this, BbkWriteOp.class);
  }

}
