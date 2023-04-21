package ai.welltested.fluttergpt.tools.refactor

import ai.welltested.fluttergpt.repository.Message
import ai.welltested.fluttergpt.repository.OpenAIRepository
import ai.welltested.fluttergpt.utilities.extractDartCode
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

class RefactorFromInstructions : AnAction() {
    private val openAIRepo: OpenAIRepository = OpenAIRepository()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformCoreDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val selectedCode = editor.caretModel.currentCaret.selectedText
        if (selectedCode.isNullOrBlank()) {
            Messages.showErrorDialog(project, "No code selected", "Error")
            return
        }

        val instructions = Messages.showInputDialog(
            project,
            "Enter refactor instructions",
            "Refactor Instructions",
            null
        ) ?: return

        object : Task.Backgroundable(project, "Refactoring Code", false) {
            override fun run(indicator: ProgressIndicator) {
                val prompt = buildString {
                    append("You're an expert Flutter/Dart coding assistant. Follow the instructions carefully and to the letter.\n\n")
                    append("Refactor the following Flutter code based on the instructions: $instructions\n\nCode:\n$selectedCode\n\n")
                    append("Output code in a single block")
                }

                try {
                    val result = openAIRepo.getCompletion(listOf(Message(role = "user", content = prompt)))
                    val refactoredCode = extractDartCode(result)
                    ApplicationManager.getApplication().invokeLater {
                        try {
                            PsiDocumentManager.getInstance(project).commitAllDocuments()
                            WriteCommandAction.runWriteCommandAction(project) {
                                editor.document.replaceString(editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd, refactoredCode)
                            }
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("FlutterGPT Success Notification")
                                .createNotification("Code refactored successfully!", NotificationType.INFORMATION)
                                .notify(project);
                        } catch (error: Exception){
                            Messages.showErrorDialog(project, "Failed to write code: ${error.message}", "Error")
                        }
                    }
                } catch (error: Exception) {
                    Messages.showErrorDialog(project, "Failed to refactor code: ${error.message}", "Error")
                }
            }
        }.queue()
    }
}