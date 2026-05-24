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

public class BbkStatementImpl extends BbkPsiElementBase implements BbkStatement {

  public BbkStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkBreakStatement getBreakStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkBreakStatement.class);
  }

  @Override
  @Nullable
  public BbkCallpStatement getCallpStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkCallpStatement.class);
  }

  @Override
  @Nullable
  public BbkContinueStatement getContinueStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkContinueStatement.class);
  }

  @Override
  @Nullable
  public BbkDoWhileStatement getDoWhileStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkDoWhileStatement.class);
  }

  @Override
  @Nullable
  public BbkExpressionStatement getExpressionStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkExpressionStatement.class);
  }

  @Override
  @Nullable
  public BbkExsrStatement getExsrStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkExsrStatement.class);
  }

  @Override
  @Nullable
  public BbkFileOpStatement getFileOpStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkFileOpStatement.class);
  }

  @Override
  @Nullable
  public BbkForStatement getForStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkForStatement.class);
  }

  @Override
  @Nullable
  public BbkIfStatement getIfStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkIfStatement.class);
  }

  @Override
  @Nullable
  public BbkLeavesrStatement getLeavesrStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkLeavesrStatement.class);
  }

  @Override
  @Nullable
  public BbkMonitorStatement getMonitorStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkMonitorStatement.class);
  }

  @Override
  @Nullable
  public BbkReturnStatement getReturnStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkReturnStatement.class);
  }

  @Override
  @Nullable
  public BbkSelectStatement getSelectStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkSelectStatement.class);
  }

  @Override
  @Nullable
  public BbkWhileStatement getWhileStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkWhileStatement.class);
  }

}
