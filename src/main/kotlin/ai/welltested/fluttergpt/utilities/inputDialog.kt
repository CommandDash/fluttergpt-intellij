package ai.welltested.fluttergpt.utilities

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

class InputDialog(
    private val dialogTitle: String,
    private val labelText: String,
    private val validationErrorMessage: String
) : DialogWrapper(true) {
    private val inputField = JBTextField()

    init {
        init()
        title = dialogTitle
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(GridLayout(2, 1))
        panel.add(JBLabel("$labelText:"))
        panel.add(inputField)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (inputField.text.trim().isEmpty()) {
            return ValidationInfo(validationErrorMessage, inputField)
        }
        return null
    }

    fun getInputText(): String {
        return inputField.text.trim()
    }
}