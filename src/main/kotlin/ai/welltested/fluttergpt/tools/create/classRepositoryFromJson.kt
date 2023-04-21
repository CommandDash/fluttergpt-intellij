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
import org.json.JSONObject

class CreateRepoClassFromPostman : AnAction() {
    private val openAIRepo: OpenAIRepository = OpenAIRepository()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformCoreDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val document = editor.document
        val description: String = try {
            JSONObject(document.text).toString()
        } catch (error: Exception) {
            Messages.showErrorDialog(project, "File content doesn't seem to be a JSON.", "Error")
            return
        }

        object : Task.Backgroundable(project, "Generating API repository", false) {
            override fun run(indicator: ProgressIndicator) {
                val prompt = buildString {
                    append("You're an expert Flutter/Dart coding assistant. Follow the user instructions carefully and to the letter.\n\n")
                    append("Create a Flutter API repository class from the following postman export:\n$description\n")
                    append("Give class an appropriate name based on the name and info of the export\nBegin!")
                }

                try {
                    val result = openAIRepo.getCompletion(listOf(Message(role = "user", content = prompt)))
                    val dartCode = extractDartCode(result)
                    ApplicationManager.getApplication().invokeLater {
                        try {
                            PsiDocumentManager.getInstance(project).commitAllDocuments()
                            WriteCommandAction.runWriteCommandAction(project) {
                                val range = document.createRangeMarker(0, document.textLength)
                                editor.document.replaceString(range.startOffset, range.endOffset, dartCode)
                            }
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("FlutterGPT Success Notification")
                                .createNotification("API repository created successfully!", NotificationType.INFORMATION)
                                .notify(project)
                        } catch (error: Exception) {
                            Messages.showErrorDialog(project, "Failed to write code: ${error.message}", "Error")
                        }
                    }
                } catch (error: Exception) {
                    Messages.showErrorDialog(project, "Failed to create API repository: ${error.message}", "Error")
                }
            }
        }.queue()
    }
}