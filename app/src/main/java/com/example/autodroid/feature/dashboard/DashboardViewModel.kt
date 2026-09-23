package com.example.autodroid.feature.dashboard

import androidx.lifecycle.ViewModel
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.automotive.SettingsManager
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.core.voice.VoiceAction
import com.example.autodroid.core.voice.VoiceAssistantManager
import com.example.autodroid.core.voice.VoiceCommandParser
import com.example.autodroid.core.voice.VoiceState
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.usecase.GetTelemetryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTelemetryUseCase: GetTelemetryUseCase,
    drivingStateManager: DrivingStateManager,
    private val settingsManager: SettingsManager,
    private val voiceAssistantManager: VoiceAssistantManager,
    private val nowPlayingStore: NowPlayingStore
) : ViewModel() {

    val vehicleState: StateFlow<VehicleState> = getTelemetryUseCase()
    val isDriving: StateFlow<Boolean> = drivingStateManager.isDriving
    val voiceState: StateFlow<VoiceState> = voiceAssistantManager.voiceState
    val voiceTranscript: StateFlow<String> = voiceAssistantManager.transcript
    val nowPlaying: StateFlow<RadioStation> = nowPlayingStore.station

    fun cycleBrand() {
        settingsManager.cycleBrand()
    }

    fun previousStation() = nowPlayingStore.previous()

    fun nextStation() = nowPlayingStore.next()

    fun triggerVoiceCommand(isLongPress: Boolean = false) {
        if (isLongPress) {
            voiceAssistantManager.simulateListen("Navigate home", ::handleCommand)
        } else {
            voiceAssistantManager.startListening(::handleCommand)
        }
    }

    fun handleCommand(command: String) {
        val reply = when (VoiceCommandParser.parse(command)) {
            VoiceAction.FUEL -> "Your fuel level is at ${vehicleState.value.fuelLevelPercent.toInt()} percent."
            VoiceAction.NAVIGATE_HOME -> "Navigating home."
            VoiceAction.UNKNOWN -> "I didn't understand."
        }
        voiceAssistantManager.speak(reply)
    }
}
