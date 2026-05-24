package com.larena.boxbreaker.plugin.bbk;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.larena.boxbreaker.plugin.bbk.psi.factory.BbkElementFactory;
import com.larena.boxbreaker.plugin.bbk.psi.BbkTypes;
import com.larena.boxbreaker.plugin.bbk.psi.BbkProcedureDeclaration;

/**
 * Verifies that Rename actually rewrites the document — both the declaration
 * (via {@code setName}) and the use-sites (via {@code handleElementRename}).
 */
public class BbkRenameTest extends BasePlatformTestCase {

    public void testFactoryCreatesIdent() {
        PsiElement id = BbkElementFactory.createIdentifier(getProject(), "foo");
        assertNotNull(id);
        assertEquals("foo", id.getText());
        assertEquals(BbkTypes.IDENT, id.getNode().getElementType());
    }

    public void testSetNameOnProcedureRewritesIdent() {
        myFixture.configureByText("main.bbk",
            "DCL-PROC <caret>oldName {\n" +
            "  return;\n" +
            "}\n");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(element);
        BbkProcedureDeclaration proc = com.intellij.psi.util.PsiTreeUtil.getParentOfType(
            element, BbkProcedureDeclaration.class);
        assertNotNull("Expected to find a BbkProcedureDeclaration at caret", proc);

        WriteCommandAction.runWriteCommandAction(getProject(), (Runnable) () -> proc.setName("newName"));

        assertEquals("newName", proc.getName());
        assertTrue("Document should now contain newName",
            myFixture.getFile().getText().contains("DCL-PROC newName"));
        assertFalse("Document should NOT contain oldName anymore",
            myFixture.getFile().getText().contains("oldName"));
    }

    public void testRenameProcedureRewritesAllUses() {
        myFixture.configureByText("main.bbk",
            "DCL-PROC main {\n" +
            "  DCL-S x INT(10);\n" +
            "  x = <caret>helper(x);\n" +
            "  x = helper(x);\n" +
            "}\n" +
            "\n" +
            "DCL-PROC helper(n INT(10) VALUE) -> INT(10) {\n" +
            "  return n - 1;\n" +
            "}\n");

        myFixture.renameElementAtCaret("renamedHelper");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone everywhere", text.contains("helper"));
        // 2 call sites + 1 declaration = 3 occurrences
        int count = text.split("renamedHelper", -1).length - 1;
        assertEquals("Expected 3 occurrences of renamedHelper (1 decl + 2 calls), got " + count
            + "\n--- file ---\n" + text, 3, count);
    }

    public void testRenameVariableRewritesAllUses() {
        // Caret on a USE site, not the declaration.
        myFixture.configureByText("main.bbk",
            "DCL-PROC main {\n" +
            "  DCL-S counter INT(10);\n" +
            "  <caret>counter = 0;\n" +
            "  counter = counter + 1;\n" +
            "}\n");

        myFixture.renameElementAtCaret("total");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone\n--- file ---\n" + text, text.contains("counter"));
        int count = text.split("total", -1).length - 1;
        assertEquals("Expected 4 occurrences of total (1 decl + 3 uses), got " + count
            + "\n--- file ---\n" + text, 4, count);
    }

    /** Repro: caret on DCL-S declaration IDENT must rename declaration + every use. */
    public void testRenameVariableFromDeclarationRewritesAllUses() {
        myFixture.configureByText("main.bbk",
            "DCL-S <caret>currentOrder INT(10);\n" +
            "\n" +
            "DCL-PROC main {\n" +
            "  currentOrder = 0;\n" +
            "  currentOrder = currentOrder + 1;\n" +
            "}\n");

        myFixture.renameElementAtCaret("total");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone everywhere\n--- file ---\n" + text,
            text.contains("currentOrder"));
        int count = text.split("total", -1).length - 1;
        assertEquals("Expected 4 occurrences of total (1 decl + 3 uses), got " + count
            + "\n--- file ---\n" + text, 4, count);
    }

    /** Repro: caret on DCL-PROC declaration IDENT must rename declaration + every call-site. */
    public void testRenameProcedureFromDeclarationRewritesAllUses() {
        myFixture.configureByText("main.bbk",
            "DCL-PROC main {\n" +
            "  DCL-S x INT(10);\n" +
            "  x = helper(x);\n" +
            "  x = helper(x);\n" +
            "}\n" +
            "\n" +
            "DCL-PROC <caret>helper(n INT(10) VALUE) -> INT(10) {\n" +
            "  return n - 1;\n" +
            "}\n");

        myFixture.renameElementAtCaret("renamedHelper");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone everywhere\n--- file ---\n" + text,
            text.contains("helper"));
        int count = text.split("renamedHelper", -1).length - 1;
        assertEquals("Expected 3 occurrences of renamedHelper (1 decl + 2 calls), got " + count
            + "\n--- file ---\n" + text, 3, count);
    }

    /** Repro: caret on DCL-C declaration IDENT must rename declaration + every use. */
    public void testRenameConstantFromDeclarationRewritesAllUses() {
        myFixture.configureByText("main.bbk",
            "DCL-C <caret>MAX_RETRIES 5;\n" +
            "\n" +
            "DCL-PROC main {\n" +
            "  DCL-S i INT(10);\n" +
            "  i = MAX_RETRIES;\n" +
            "  i = i + MAX_RETRIES;\n" +
            "}\n");

        myFixture.renameElementAtCaret("MAX_TRIES");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone\n--- file ---\n" + text,
            text.contains("MAX_RETRIES"));
        int count = text.split("MAX_TRIES", -1).length - 1;
        assertEquals("Expected 3 occurrences of MAX_TRIES (1 decl + 2 uses), got " + count
            + "\n--- file ---\n" + text, 3, count);
    }

    /** Repro: caret on DCL-S declaration IDENT with LIKEDS type spec. */
    public void testRenameVariableFromDeclarationWithLikeDs() {
        myFixture.configureByText("main.bbk",
            "DCL-DS customer QUALIFIED;\n" +
            "  DCL-SUBF id INT(10);\n" +
            "END-DS;\n" +
            "\n" +
            "DCL-S <caret>currentOrder LIKEDS(customer);\n" +
            "\n" +
            "DCL-PROC main {\n" +
            "  currentOrder.id = 0;\n" +
            "}\n");

        myFixture.renameElementAtCaret("total");

        String text = myFixture.getFile().getText();
        assertFalse("Old name should be gone\n--- file ---\n" + text,
            text.contains("currentOrder"));
        assertTrue("Declaration should be renamed", text.contains("DCL-S total LIKEDS"));
        assertTrue("Use site should be renamed", text.contains("total.id"));
    }

    public void testReservedWordsValidator() {
        com.larena.boxbreaker.plugin.bbk.refactoring.BbkRenameInputValidator v =
            new com.larena.boxbreaker.plugin.bbk.refactoring.BbkRenameInputValidator();
        myFixture.configureByText("main.bbk", "DCL-S x INT(10);\n");
        PsiElement decl = com.intellij.psi.util.PsiTreeUtil.findChildOfType(
            myFixture.getFile(),
            com.larena.boxbreaker.plugin.bbk.psi.BbkVariableDeclaration.class);
        assertNotNull(decl);

        com.intellij.util.ProcessingContext ctx = new com.intellij.util.ProcessingContext();
        assertTrue("Valid identifier 'foo' should be accepted",
            v.isInputValid("foo", decl, ctx));
        assertFalse("Reserved word 'value' should be rejected",
            v.isInputValid("value", decl, ctx));
        assertFalse("Reserved word 'IF' should be rejected (case-insensitive)",
            v.isInputValid("IF", decl, ctx));
        assertFalse("Reserved word 'DCL-S' should be rejected",
            v.isInputValid("DCL-S", decl, ctx));
        assertFalse("Reserved word 'PRE-IF' should be rejected",
            v.isInputValid("PRE-IF", decl, ctx));
        assertFalse("Empty should be rejected", v.isInputValid("", decl, ctx));
        assertFalse("Starts with digit should be rejected", v.isInputValid("1foo", decl, ctx));
        assertFalse("Contains space should be rejected", v.isInputValid("foo bar", decl, ctx));
        assertFalse("Contains hyphen (looks like keyword) should be rejected",
            v.isInputValid("my-var", decl, ctx));
    }

    public void testFindTargetElementOnDeclIdent() {
        // Caret in the MIDDLE of the identifier ('cur|rentOrder') to match real-user behaviour.
        myFixture.configureByText("main.bbk",
            "DCL-S cur<caret>rentOrder INT(10);\n");

        com.intellij.openapi.editor.Editor editor = myFixture.getEditor();
        int offset = myFixture.getCaretOffset();
        System.out.println("caret offset=" + offset);
        PsiElement leaf = myFixture.getFile().findElementAt(offset);
        System.out.println("leaf=" + (leaf == null ? "null" : leaf.getClass().getName() + " '" + leaf.getText() + "' range=" + leaf.getTextRange()));

        com.intellij.codeInsight.TargetElementUtil util = com.intellij.codeInsight.TargetElementUtil.getInstance();
        int flags = com.intellij.codeInsight.TargetElementUtil.ELEMENT_NAME_ACCEPTED
            | com.intellij.codeInsight.TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED;
        PsiElement target = util.findTargetElement(editor, flags, offset);
        System.out.println("findTargetElement(flags=NAME|REF)=" + target);

        target = util.findTargetElement(editor,
            com.intellij.codeInsight.TargetElementUtil.ELEMENT_NAME_ACCEPTED, offset);
        System.out.println("findTargetElement(flags=NAME)=" + target);

        com.intellij.psi.PsiReference ref = util.findReference(editor, offset);
        System.out.println("findReference=" + ref);
        if (ref != null) {
            System.out.println("  ref.getElement=" + ref.getElement().getClass().getSimpleName());
            System.out.println("  ref.resolve=" + ref.resolve());
        }

        // walk up checking what's PsiNamedElement and what nameIdentifier returns
        if (leaf != null) {
            PsiElement p = leaf.getParent();
            while (p != null) {
                if (p instanceof com.intellij.psi.PsiNamedElement) {
                    String s = "  PsiNamedElement " + p.getClass().getSimpleName();
                    if (p instanceof com.intellij.psi.PsiNameIdentifierOwner owner) {
                        PsiElement ni = owner.getNameIdentifier();
                        s += " nameIdentifier=" + (ni == null ? "null" : ni.getTextRange()
                            + " sameAsLeaf=" + (ni == leaf));
                    }
                    System.out.println(s);
                }
                p = p.getParent();
            }
        }
    }

    public void testDumpPsiOfTopLevelDclS() {
        myFixture.configureByText("main.bbk",
            "DCL-S <caret>currentOrder INT(10);\n");

        PsiElement leaf = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(leaf);
        assertEquals("currentOrder", leaf.getText());

        System.out.println("=== parent chain of IDENT 'currentOrder' (top-level decl) ===");
        PsiElement p = leaf;
        while (p != null) {
            String cls = p.getClass().getName();
            String iface = "";
            if (p instanceof com.intellij.psi.PsiNameIdentifierOwner owner) {
                PsiElement nameId = owner.getNameIdentifier();
                iface = " [PsiNameIdentifierOwner, nameIdentifier="
                    + (nameId == null ? "null" : "'" + nameId.getText() + "'@" + nameId.getTextOffset())
                    + "]";
            }
            System.out.println("  " + cls + iface);
            p = p.getParent();
        }
    }

    public void testDumpPsiOfDclSInsideProcBody() {
        // Diagnostic: print the PSI tree so we can see if DCL-S inside a procedure body
        // is actually wrapped in BbkVariableDeclarationImpl.
        myFixture.configureByText("main.bbk",
            "DCL-PROC main {\n" +
            "  DCL-S counter INT(10);\n" +
            "}\n");

        System.out.println("=== PSI tree ===");
        dump(myFixture.getFile(), 0);

        // Find the IDENT 'counter' and check its parent chain
        PsiElement counter = findIdentByText(myFixture.getFile(), "counter");
        assertNotNull(counter);
        System.out.println("\nIDENT 'counter' parent chain:");
        PsiElement p = counter;
        while (p != null) {
            System.out.println("  " + p.getClass().getSimpleName() + " text='"
                + (p.getText().length() > 30 ? p.getText().substring(0, 30) + "..." : p.getText()) + "'");
            p = p.getParent();
        }
    }

    private static void dump(PsiElement el, int depth) {
        String indent = "  ".repeat(depth);
        String text = el.getText();
        if (text.length() > 40) text = text.substring(0, 40) + "...";
        text = text.replace("\n", "\\n");
        System.out.println(indent + el.getClass().getSimpleName()
            + "(" + (el.getNode() == null ? "?" : el.getNode().getElementType()) + ")"
            + " '" + text + "'");
        for (PsiElement c : el.getChildren()) {
            dump(c, depth + 1);
        }
    }

    private static PsiElement findIdentByText(PsiElement root, String text) {
        for (PsiElement child : com.intellij.psi.util.PsiTreeUtil.findChildrenOfAnyType(root, com.intellij.psi.impl.source.tree.LeafPsiElement.class)) {
            if (child.getNode().getElementType() == BbkTypes.IDENT && text.equals(child.getText())) {
                return child;
            }
        }
        return null;
    }
}
