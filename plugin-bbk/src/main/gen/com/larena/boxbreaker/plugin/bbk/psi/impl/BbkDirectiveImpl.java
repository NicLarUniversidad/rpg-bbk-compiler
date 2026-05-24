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

public class BbkDirectiveImpl extends BbkPsiElementBase implements BbkDirective {

  public BbkDirectiveImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitDirective(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkPreDefineDirective getPreDefineDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreDefineDirective.class);
  }

  @Override
  @Nullable
  public BbkPreElseDirective getPreElseDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreElseDirective.class);
  }

  @Override
  @Nullable
  public BbkPreElseifDirective getPreElseifDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreElseifDirective.class);
  }

  @Override
  @Nullable
  public BbkPreEndifDirective getPreEndifDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreEndifDirective.class);
  }

  @Override
  @Nullable
  public BbkPreEofDirective getPreEofDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreEofDirective.class);
  }

  @Override
  @Nullable
  public BbkPreIfDirective getPreIfDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreIfDirective.class);
  }

  @Override
  @Nullable
  public BbkPreIncludeDirective getPreIncludeDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreIncludeDirective.class);
  }

  @Override
  @Nullable
  public BbkPreUndefineDirective getPreUndefineDirective() {
    return PsiTreeUtil.getChildOfType(this, BbkPreUndefineDirective.class);
  }

}
