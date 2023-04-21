package ai.welltested.fluttergpt.utilities.configManager

import com.intellij.util.messages.Topic

interface SecretKeyListener {
    fun secretKeyChanged(newSecretKey: String)

    companion object {
        val SECRET_KEY_TOPIC: Topic<SecretKeyListener> = Topic.create("Secret Key Changed", SecretKeyListener::class.java)
    }
}