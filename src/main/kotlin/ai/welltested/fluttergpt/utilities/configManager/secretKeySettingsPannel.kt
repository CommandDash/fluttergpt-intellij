package ai.welltested.fluttergpt.utilities.configManager

import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Desktop
import java.net.URI

class SecretKeySettingsPanel : JPanel() {
    private val secretKeyTextField: JTextField

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val titleLabel = JLabel("Fluttergpt: Api Key").apply {
            font = Font(font.name, Font.BOLD, font.size + 2)
            alignmentX = LEFT_ALIGNMENT
        }
        add(titleLabel)



        val openAiLabel = JLabel("<html>Key from <a href=\"\">OpenAI</a></html>").apply {
            cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    Desktop.getDesktop().browse(URI.create("https://platform.openai.com/account/api-keys"))
                }
            })
            alignmentX = LEFT_ALIGNMENT
            maximumSize = Dimension(200, preferredSize.height)
        }
        add(openAiLabel)

        add(Box.createRigidArea(Dimension(0, 10))) // Add vertical space

        secretKeyTextField = JTextField(25).apply {
            alignmentX = LEFT_ALIGNMENT
            margin = Insets(2, 0, 2, 0)
            maximumSize = Dimension(400, preferredSize.height)
        }
        add(secretKeyTextField)

        add(Box.createVerticalGlue())
    }

    var secretKey: String
        get() = secretKeyTextField.text
        set(value) {
            secretKeyTextField.text = value
        }
}