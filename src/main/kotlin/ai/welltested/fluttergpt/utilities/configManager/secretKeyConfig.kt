package ai.welltested.fluttergpt.utilities.configManager

import com.intellij.openapi.components.*

@State(name = "SecretKeyConfig", storages = [Storage("secretKeyConfig.xml")])
@Service
class SecretKeyConfig : PersistentStateComponent<SecretKeyConfig.State> {

    data class State(var secretKey: String = "")

    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    var secretKey: String
        get() = myState.secretKey
        set(value) {
            myState.secretKey = value
        }

    companion object {
        fun getInstance(): SecretKeyConfig {
            return service()
        }
    }
}