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

public class BbkVarModifierImpl extends BbkPsiElementBase implements BbkVarModifier {

  public BbkVarModifierImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BbkVisitor visitor) {
    visitor.visitVarModifier(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BbkVisitor) accept((BbkVisitor)visitor);
    else super.accept(visitor);
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
  public BbkExportModifier getExportModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkExportModifier.class);
  }

  @Override
  @Nullable
  public BbkInzModifier getInzModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkInzModifier.class);
  }

  @Override
  @Nullable
  public BbkOverlayModifier getOverlayModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkOverlayModifier.class);
  }

  @Override
  @Nullable
  public BbkPosModifier getPosModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkPosModifier.class);
  }

  @Override
  @Nullable
  public BbkQualifiedModifier getQualifiedModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkQualifiedModifier.class);
  }

  @Override
  @Nullable
  public BbkStaticModifier getStaticModifier() {
    return PsiTreeUtil.getChildOfType(this, BbkStaticModifier.class);
  }

}
