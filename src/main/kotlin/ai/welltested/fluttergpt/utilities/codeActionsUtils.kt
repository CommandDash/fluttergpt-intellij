package ai.welltested.fluttergpt.utilities

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

fun isCursorAtMethodDeclaration(element: PsiElement, editor: Editor): Boolean {
    val methodElement = PsiTreeUtil.findFirstParent(element) { it.elementType.toString() == "METHOD_DECLARATION" }
    if (methodElement == null) {
        return false
    }
    val document = editor.document
    val lineNumber = document.getLineNumber(editor.caretModel.offset)
    val methodLine = methodElement.textRange?.startOffset?.let { document.getLineNumber(it) }
    return lineNumber == methodLine
}

fun getMethodElement(element: PsiElement): PsiElement? {
    val methodElement = PsiTreeUtil.findFirstParent(element) { it.elementType.toString() == "METHOD_DECLARATION" }
    return methodElement
}