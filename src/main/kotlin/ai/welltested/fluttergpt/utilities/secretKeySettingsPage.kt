package ai.welltested.fluttergpt.utilities

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class SecretKeySettingsPage : Configurable {

    private var settingsPanel: SecretKeySettingsPanel? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "My Extension"
    }

    override fun createComponent(): JComponent? {
        settingsPanel = SecretKeySettingsPanel()
        return settingsPanel
    }

    override fun isModified(): Boolean {
        val config = SecretKeyConfig.getInstance()
        return config.secretKey != settingsPanel?.secretKey
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val config = SecretKeyConfig.getInstance()
        val newSecretKey = settingsPanel?.secretKey ?: ""
        config.secretKey = newSecretKey

        // Notify listeners
        val app = ApplicationManager.getApplication()
        app.messageBus.syncPublisher(SecretKeyListener.SECRET_KEY_TOPIC).secretKeyChanged(newSecretKey)
    }

    override fun reset() {
        val config = SecretKeyConfig.getInstance()
        settingsPanel?.secretKey = config.secretKey
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}