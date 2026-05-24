package com.larena.boxbreaker.plugin.bbk;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.larena.boxbreaker.plugin.bbk.psi.BbkProcedureDeclaration;
import com.larena.boxbreaker.plugin.bbk.scope.BbkScopeWalker;

import java.util.List;

/**
 * Integration test: opens a synthetic BBK file with a procedure declared and called,
 * positions the caret on the call site, and verifies that {@code resolve()} returns
 * the declaration.
 *
 * <p>This bypasses the GUI sandbox entirely — uses IntelliJ's headless test fixture.
 */
public class BbkReferenceResolutionTest extends BasePlatformTestCase {

    public void testIntraFileProcedureCtrlBResolvesToDeclaration() {
        // Layout: caret is on the second `localHelper`, which is the call site.
        myFixture.configureByText("main.bbk",
            "DCL-PROC main {\n" +
            "  DCL-S x INT(10);\n" +
            "  x = local<caret>Helper(x);\n" +
            "}\n" +
            "\n" +
            "DCL-PROC localHelper(n INT(10) VALUE) -> INT(10) {\n" +
            "  return n - 1;\n" +
            "}\n");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull("No element at caret", element);
        System.out.println("CARET ELEMENT class=" + element.getClass().getSimpleName()
            + " text='" + element.getText() + "'"
            + " elementType=" + element.getNode().getElementType());
        System.out.println("CARET PARENT  class=" + element.getParent().getClass().getSimpleName()
            + " text='" + element.getParent().getText() + "'");

        PsiReference[] refs = element.getReferences();
        System.out.println("REFERENCES (via element.getReferences()) count=" + refs.length);
        for (PsiReference r : refs) {
            System.out.println("  ref=" + r.getClass().getSimpleName()
                + " value='" + r.getCanonicalText() + "'"
                + " resolved=" + (r.resolve() == null ? "null"
                    : r.resolve().getClass().getSimpleName()));
        }

        // Direct invocation via the registry to bypass any "getReferences() default"
        PsiReference[] viaRegistry = com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
            .getReferencesFromProviders(element);
        System.out.println("REFERENCES (via Registry) count=" + viaRegistry.length);
        for (PsiReference r : viaRegistry) {
            System.out.println("  registry-ref=" + r.getClass().getSimpleName()
                + " value='" + r.getCanonicalText() + "'");
        }

        // Test on the parent composite (BbkPrimary) — this is where the contributor matches now
        PsiElement parent = element.getParent();
        PsiReference[] parentRefs = parent.getReferences();
        System.out.println("REFERENCES (via parent.getReferences()) count=" + parentRefs.length);
        PsiReference[] parentRegistryRefs = com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
            .getReferencesFromProviders(parent);
        System.out.println("REFERENCES (Registry on parent) count=" + parentRegistryRefs.length);
        for (PsiReference r : parentRegistryRefs) {
            System.out.println("  parent-registry-ref=" + r.getClass().getSimpleName()
                + " value='" + r.getCanonicalText() + "'");
        }
        for (PsiReference r : parentRefs) {
            System.out.println("  parent-ref=" + r.getClass().getSimpleName()
                + " value='" + r.getCanonicalText() + "'"
                + " resolved=" + (r.resolve() == null ? "null"
                    : r.resolve().getClass().getSimpleName()));
        }

        // The Ctrl+B equivalent: file.findReferenceAt(offset)
        PsiReference foundAt = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        System.out.println("REFERENCES (via file.findReferenceAt) = "
            + (foundAt == null ? "null" : foundAt.getClass().getSimpleName()
                + " resolved=" + (foundAt.resolve() == null ? "null"
                    : foundAt.resolve().getClass().getSimpleName())));

        // Scope walker check
        List<PsiNamedElement> visible = BbkScopeWalker.allVisible(element);
        System.out.println("SCOPE visible=" + visible.size());
        for (PsiNamedElement d : visible) {
            System.out.println("  - " + d.getClass().getSimpleName() + " name=" + d.getName());
        }

        // Assertions — use findReferenceAt which is what Ctrl+B uses internally
        assertNotNull("findReferenceAt returned null — Ctrl+B will fail", foundAt);
        PsiElement resolved = foundAt.resolve();
        assertNotNull("Reference resolved to null", resolved);
        assertTrue("Resolved to wrong type: " + resolved.getClass().getName(),
            resolved instanceof BbkProcedureDeclaration);
        assertEquals("localHelper", ((BbkProcedureDeclaration) resolved).getName());
    }

    public void testNoDuplicateDeclarationsInScopeChain() {
        // Reproduces the "Choose Declaration" popup bug: scope should list each local
        // exactly once even though ProcedureScope and BlockScope cover the same body.
        myFixture.configureByText("main.bbk",
            "DCL-PROC runDemo {\n" +
            "  DCL-S currentCustomer INT(10);\n" +
            "  currentCustomer = <caret>currentCustomer;\n" +
            "}\n");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(element);
        List<PsiNamedElement> visible = BbkScopeWalker.allVisible(element);
        long count = visible.stream()
            .filter(d -> "currentCustomer".equalsIgnoreCase(d.getName()))
            .count();
        System.out.println("=== Duplicate check ===");
        for (PsiNamedElement d : visible) {
            System.out.println("  - " + d.getClass().getSimpleName() + " name=" + d.getName());
        }
        assertEquals("currentCustomer should appear exactly once in scope chain", 1, count);
    }

    public void testScopeWalkerSeesTopLevelDeclarations() {
        myFixture.configureByText("main.bbk",
            "DCL-C MAX_RETRIES 5;\n" +
            "DCL-S counter INT(10);\n" +
            "DCL-DS person QUALIFIED {\n" +
            "  id INT(10);\n" +
            "  name CHAR(50);\n" +
            "}\n" +
            "DCL-PROC main {\n" +
            "  <caret>\n" +
            "}\n");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(element);

        List<PsiNamedElement> visible = BbkScopeWalker.allVisible(element);
        System.out.println("=== Top-level visible from inside DCL-PROC main ===");
        for (PsiNamedElement d : visible) {
            System.out.println("  - " + d.getClass().getSimpleName() + " name=" + d.getName());
        }
        assertTrue("Scope should include the 3 module-level + 1 proc declarations, got " + visible.size(),
            visible.size() >= 4);
    }
}
