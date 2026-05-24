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

public class BbkBlockItemImpl extends BbkPsiElementBase implements BbkBlockItem {

  public BbkBlockItemImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitBlockItem(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkConstantDeclaration getConstantDeclaration() {
    return PsiTreeUtil.getChildOfType(this, BbkConstantDeclaration.class);
  }

  @Override
  @Nullable
  public BbkDataStructureDeclaration getDataStructureDeclaration() {
    return PsiTreeUtil.getChildOfType(this, BbkDataStructureDeclaration.class);
  }

  @Override
  @Nullable
  public BbkDirective getDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkDirective.class);
  }

  @Override
  @Nullable
  public BbkStatement getStatement() {
    return PsiTreeUtil.getChildOfType(this, BbkStatement.class);
  }

  @Override
  @Nullable
  public BbkSubroutineDefinition getSubroutineDefinition() {
    return PsiTreeUtil.getChildOfType(this, BbkSubroutineDefinition.class);
  }

  @Override
  @Nullable
  public BbkVariableDeclaration getVariableDeclaration() {
    return PsiTreeUtil.getChildOfType(this, BbkVariableDeclaration.class);
  }

}
