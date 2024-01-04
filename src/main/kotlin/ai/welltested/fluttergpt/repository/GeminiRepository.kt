package ai.welltested.fluttergpt.repository

import ai.welltested.fluttergpt.utilities.configManager.SecretKeyConfig
import ai.welltested.fluttergpt.utilities.configManager.SecretKeyListener
import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import java.net.HttpURLConnection
import java.net.URL


data class Candidate(
    val content: Content,
    val finishReason: String,
    val index: Int
)

data class Content(
    val parts: List<Part>,
    val role: String
)

data class Part(
    val text: String
)

data class GeminiCompletionResponse(
    val candidates: List<Candidate>
)

class GeminiRepository {
    private var apiKey: String? = null

    init {
        apiKey = SecretKeyConfig.getInstance().secretKey;
        val connection: MessageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(SecretKeyListener.SECRET_KEY_TOPIC, object : SecretKeyListener {
            override fun secretKeyChanged(newSecretKey: String) {
                apiKey = newSecretKey
            }
        })
    }

    fun getCompletion(prompt: List<String>): String {
        if (apiKey == null) {
            throw Exception("API token not set, please go to extension settings to set it (read README.md for more info)")
        }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val gson = Gson()
        val requestBody = gson.toJson(mapOf("contents" to prompt.map { mapOf("parts" to listOf(mapOf("text" to it))) }))
        connection.outputStream.write(requestBody.toByteArray())
        val inputStream = connection.inputStream
        val response = gson.fromJson(inputStream.reader(), GeminiCompletionResponse::class.java)
        inputStream.close()


        if(response.candidates.isNotEmpty()) {
            return response.candidates[0].content.parts[0].text
        }
        return "Failed to generate content"

    }
}