package ai.welltested.fluttergpt.tools.refactor

import ai.welltested.fluttergpt.repository.GeminiRepository
import ai.welltested.fluttergpt.utilities.extractDartCode
import ai.welltested.fluttergpt.utilities.getMethodElement
import ai.welltested.fluttergpt.utilities.isCursorAtMethodDeclaration
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

class OptimizeFunction : PsiElementBaseIntentionAction(), IntentionAction {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) return false
        return isCursorAtMethodDeclaration(element, editor)
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {

                val methodElement = getMethodElement(p2)
                if (methodElement == null) {
                    return
                }
                val fullMethodCode = methodElement.text

                var prompt =
                    "You're an expert Flutter/Dart coding assistant. Follow the instructions carefully and output response in the modified format..\\n\\n"
                prompt += "Develop and optimize the following Flutter code by troubleshooting errors, fixing errors, and identifying root causes of issues. Reflect and critique your solution to ensure it meets the requirements and specifications of speed, flexibility and user friendliness.\n\n Subject Code:\n${fullMethodCode}\n\n";
                prompt += "Here is the full code for context:\n";
                if (p1 != null) {
                    prompt += "```" + p1.document.text + "```";
                }
                prompt += "\n\n";
                prompt += "Output the optimized code in a single code block to be replaced over selected code.";
        object : Task.Backgroundable(p0, "Optimizing function", false) {
            override fun run(indicator: ProgressIndicator) {

                val result = GeminiRepository().getCompletion(listOf(prompt))
                val dartCode = extractDartCode(result)
                if (p1 != null) {
                    p1.document.replaceString(
                        methodElement.textRange.startOffset,
                        methodElement.textRange.endOffset,
                        dartCode
                    )
                }
            }
        }.queue()

    }


    override fun getFamilyName(): String {
        return "FlutterGPT"
    }

    override fun getText(): String {
        return "Optimize code"
    }


}