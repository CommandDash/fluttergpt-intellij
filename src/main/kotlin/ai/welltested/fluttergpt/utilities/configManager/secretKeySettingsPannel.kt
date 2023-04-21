package ai.welltested.fluttergpt.utilities.configManager

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class SecretKeySettingsPanel : JPanel() {
    private val secretKeyTextField: JTextField

    init {
        layout = GridBagLayout()

        val label = JLabel("Secret Key:")
        val constraints = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = Insets(4, 4, 4, 4)
        }
        add(label, constraints)

        secretKeyTextField = JTextField(25)
        constraints.gridx = 1
        add(secretKeyTextField, constraints)
    }

    var secretKey: String
        get() = secretKeyTextField.text
        set(value) {
            secretKeyTextField.text = value
        }
}