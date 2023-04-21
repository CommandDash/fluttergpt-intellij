package ai.welltested.fluttergpt.tools.refactor

import ai.welltested.fluttergpt.repository.Message
import ai.welltested.fluttergpt.repository.OpenAIRepository
import ai.welltested.fluttergpt.utilities.extractDartCode
import ai.welltested.fluttergpt.utilities.extractExplanation
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction

class FixErrors() : AnAction() {
    private val openAIRepo: OpenAIRepository = OpenAIRepository()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformCoreDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val selectedCode = editor.caretModel.currentCaret.selectedText
        if (selectedCode.isNullOrBlank()) {
            Messages.showErrorDialog(project, "No code selected", "Error")
            return
        }

        val errorsDescription = Messages.showInputDialog(
            project,
            "Enter the errors you're facing",
            "Error Description",
            null
        ) ?: return

        object : Task.Backgroundable(project, "Debugging Errors", false) {
            override fun run(indicator: ProgressIndicator) {
                val promptContent = buildString {
                    append("Follow the instructions carefully and to the letter. You're a Flutter/Dart debugging expert.\n\n")
                    append("Here's a piece of Flutter code with errors:\n\n$selectedCode\n\n")
                    append("The errors are: $errorsDescription\n\n")
                    append("Output the fixed code in a single code block.")
                }

                try {
                    val result = openAIRepo.getCompletion(listOf(Message(role = "user", content = promptContent)))
                    val fixedCode = extractDartCode(result)
                    val explanation = extractExplanation(result)
                    ApplicationManager.getApplication().invokeLater {
                        try {
                            PsiDocumentManager.getInstance(project).commitAllDocuments()
                            WriteCommandAction.runWriteCommandAction(project) {
                                editor.document.replaceString(editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd, fixedCode)
                            }
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("FlutterGPT Success Notification")
                                .createNotification("Errors resolved: \n $explanation", NotificationType.INFORMATION)
                                .notify(project);
                        } catch (error: Exception){
                            Messages.showErrorDialog(project, "Failed to write code: ${error.message}", "Error")
                        }
                    }
                } catch (error: Exception) {
                    Messages.showErrorDialog(project, "Failed to fix code: ${error.message}", "Error")
                }
            }
        }.queue()
    }
}