package ai.welltested.fluttergpt.utilities


import ai.welltested.fluttergpt.tools.create.CodeFromBlueprint
import ai.welltested.fluttergpt.tools.create.CreateModelClass
import ai.welltested.fluttergpt.tools.create.CreateRepoClassFromPostman
import ai.welltested.fluttergpt.tools.create.CreateWidgetFromDescription
import ai.welltested.fluttergpt.tools.refactor.FixErrors
import ai.welltested.fluttergpt.tools.refactor.RefactorFromInstructions
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ActionManager



object ActionRegistry  {
    fun refreshActions() {

        val actionManager = ActionManager.getInstance()
        val existingActionGroup = actionManager.getAction("FlutterGPTActionGroup")
        if (existingActionGroup is DefaultActionGroup) {
            val group = existingActionGroup

            group.removeAll()
            group.add(CreateWidgetFromDescription())
            group.add(CreateModelClass())
            group.add(CodeFromBlueprint())
            group.add(CreateRepoClassFromPostman())
            group.addSeparator()

        }
        val refactActionGroup = actionManager.getAction("FlutterGPTRefactorActionGroup")
        if (refactActionGroup is DefaultActionGroup) {
            val group = refactActionGroup
            group.removeAll()
            group.add(FixErrors())
            group.add(RefactorFromInstructions())
        }
    }
}

