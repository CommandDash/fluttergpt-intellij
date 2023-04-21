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
import com.intellij.openapi.ui.InputValidator
import org.json.JSONObject

class CreateModelClass : AnAction() {
    private val openAIRepo: OpenAIRepository = OpenAIRepository()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformCoreDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val jsonStructure = Messages.showInputDialog(
            project,
            "Enter JSON structure",
            "JSON Structure",
            null,
            "",
            object : InputValidator {
                override fun checkInput(inputString: String?): Boolean {
                    return try {
                        inputString?.let { JSONObject(it) }
                        true
                    } catch (e: Exception) {
                        false
                    }
                }

                override fun canClose(inputString: String?): Boolean {
                    return checkInput(inputString)
                }
            }
        ) ?: return

        val library = Messages.showDialog(
            project,
            "Select a library",
            "Library",
            arrayOf("None", "Freezed", "JsonSerializable"),
            0,
            null
        ) ?: return

        val includeHelpers = Messages.showDialog(
            project,
            "Include toJson, fromJson, and copyWith methods?",
            "Include Helpers",
            arrayOf("Yes", "No"),
            0,
            null
        ) ?: return

        object : Task.Backgroundable(project, "Creating Model Class", false) {
            override fun run(indicator: ProgressIndicator) {
                val prompt = buildString {
                    append("You're an expert Flutter/Dart coding assistant. Follow the user instructions carefully and to the letter.\n\n")
                    append("Create a Flutter model class, keeping null safety in mind for from the following JSON structure: $jsonStructure.")
                    if(library != 0) {
                        append("Use $library")
                    }
                    if(includeHelpers == 1){
                        append("Make sure toJson, fromJson, and copyWith methods are included.")
                    }
                    append("Output the model class code in a single block.")
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
                                .createNotification("Model class created successfully!", NotificationType.INFORMATION)
                                .notify(project);
                        } catch (error: Exception){
                            Messages.showErrorDialog(project, "Failed to write code: ${error.message}", "Error")
                        }
                    }
                } catch (error: Exception) {
                    Messages.showErrorDialog(project, "Failed to create model class: ${error.message}", "Error")
                }
            }
        }.queue()
    }
}