package ai.welltested.fluttergpt.repository

import ai.welltested.fluttergpt.utilities.SecretKeyConfig
import ai.welltested.fluttergpt.utilities.SecretKeyListener
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection

data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double
)

data class Message(
    val role: String,
    val content: String
)

data class Choice(
    val index: Int,
    val message: Message,
    val finishReason: String
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

class OpenAIRepository() {
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

    fun getCompletion(prompt: List<Message>): String {
        if (apiKey == null) {
            throw Exception("API token not set, please go to extension settings to set it (read README.md for more info)")
        }

        val request = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = prompt,
            temperature = 0.7
        )

        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true

        val gson = Gson()
        val requestBody = gson.toJson(request).toByteArray()
        connection.outputStream.write(requestBody)

        val inputStream = connection.inputStream
        val response = gson.fromJson(inputStream.reader(), ChatCompletionResponse::class.java)
        inputStream.close()

        if (response.choices.isEmpty()) {
            throw Exception("API response was empty or missing \"choices\" field")
        }

        return response.choices[0].message.content.trim()
    }
}