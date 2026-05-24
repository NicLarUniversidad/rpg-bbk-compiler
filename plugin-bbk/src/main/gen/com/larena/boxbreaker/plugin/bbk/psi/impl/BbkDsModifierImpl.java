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

public class BbkDsModifierImpl extends BbkPsiElementBase implements BbkDsModifier {

  public BbkDsModifierImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitDsModifier(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BbkAlignModifier getAlignModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkAlignModifier.class);
  }

  @Override
  @Nullable
  public BbkBasedModifier getBasedModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkBasedModifier.class);
  }

  @Override
  @Nullable
  public BbkDimModifier getDimModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkDimModifier.class);
  }

  @Override
  @Nullable
  public BbkExtnameDsModifier getExtnameDsModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkExtnameDsModifier.class);
  }

  @Override
  @Nullable
  public BbkInfdsDsModifier getInfdsDsModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkInfdsDsModifier.class);
  }

  @Override
  @Nullable
  public BbkInzModifier getInzModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkInzModifier.class);
  }

  @Override
  @Nullable
  public BbkLikedsDsModifier getLikedsDsModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkLikedsDsModifier.class);
  }

  @Override
  @Nullable
  public BbkLikerecDsModifier getLikerecDsModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkLikerecDsModifier.class);
  }

  @Override
  @Nullable
  public BbkQualifiedModifier getQualifiedModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkQualifiedModifier.class);
  }

  @Override
  @Nullable
  public BbkTemplateModifier getTemplateModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkTemplateModifier.class);
  }

}
