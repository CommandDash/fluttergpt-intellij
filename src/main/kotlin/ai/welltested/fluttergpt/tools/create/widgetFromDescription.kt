package ai.welltested.fluttergpt.tools.create

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

class CreateWidgetFromDescription : AnAction("Widget from Description") {
    private val openAIRepo: OpenAIRepository = OpenAIRepository()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformCoreDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val description = Messages.showInputDialog(
            project,
            "Enter widget description",
            "Widget Description",
            null
        ) ?: return

        object : Task.Backgroundable(project, "Creating Widget", false) {
            override fun run(indicator: ProgressIndicator) {
                val prompt = buildString {
                    append("You're an expert Flutter/Dart coding assistant. Follow the user instructions carefully and to the letter.\n\n")
                    append("Create a Flutter Widget from the following description: $description.\n")
                    append("Output code in a single block")
                }

                try {
                    val result = openAIRepo.getCompletion(listOf(Message(role = "user", content = prompt)))
                    val dartCode = extractDartCode(result)
                    ApplicationManager.getApplication().invokeLater {
                        try {
                            PsiDocumentManager.getInstance(project).commitAllDocuments()
                            WriteCommandAction.runWriteCommandAction(project) {
                                val position = editor.caretModel.offset
                                editor.document.insertString(position, dartCode)
                            }
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("FlutterGPT Success Notification")
                                .createNotification("Widget created successfully!", NotificationType.INFORMATION)
                                .notify(project)
                        } catch (error: Exception) {
                            Messages.showErrorDialog(project, "Failed to write code: ${error.message}", "Error")
                        }
                    }
                } catch (error: Exception) {
                    Messages.showErrorDialog(project, "Failed to create widget: ${error.message}", "Error")
                }
            }
        }.queue()
    }
}