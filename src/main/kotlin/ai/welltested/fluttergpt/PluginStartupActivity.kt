package ai.welltested.fluttergpt

import ai.welltested.fluttergpt.utilities.ActionRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.annotations.NotNull


class PluginStartupActivity : StartupActivity {

    override fun runActivity(@NotNull project: Project) {
        ActionRegistry.refreshActions( )

    }
}

