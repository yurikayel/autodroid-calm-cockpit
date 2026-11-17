package com.example.autodroid.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.automotive.SettingsManager
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.core.demo.DemoNavigationDestination
import com.example.autodroid.core.navigation.NavigationManager
import com.example.autodroid.core.voice.VoiceAction
import com.example.autodroid.core.voice.VoiceAssistantManager
import com.example.autodroid.core.voice.VoiceCommandParser
import com.example.autodroid.core.voice.VoiceState
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.usecase.GetTelemetryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.autodroid.core.navigation.domain.repository.NavigationRepository
import com.example.autodroid.feature.weather.domain.location.DeviceLocationProvider

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTelemetryUseCase: GetTelemetryUseCase,
    drivingStateManager: DrivingStateManager,
    private val settingsManager: SettingsManager,
    private val voiceAssistantManager: VoiceAssistantManager,
    private val nowPlayingStore: NowPlayingStore,
    private val navigationManager: NavigationManager,
    private val navigationRepository: NavigationRepository,
    private val deviceLocationProvider: DeviceLocationProvider
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

    private val _navEvents = MutableSharedFlow<DashboardNavEvent>(extraBufferCapacity = 1)
    val navEvents = _navEvents.asSharedFlow()

    fun handleCommand(command: String) {
        val action = VoiceCommandParser.parse(command)
        val reply = when (action) {
            VoiceAction.FUEL -> "Your fuel level is at ${vehicleState.value.fuelLevelPercent.toInt()} percent."
            VoiceAction.NAVIGATE_HOME -> "Navigating home."
            VoiceAction.WEATHER_ALONG_ROUTE -> {
                startRouteWeather()
                null // TTS handled inside the coroutine to avoid double-speak
            }
            is VoiceAction.WEATHER -> {
                _navEvents.tryEmit(DashboardNavEvent.Weather)
                if (action.city.isNotEmpty()) "Checking the weather in ${action.city}." else "Opening weather."
            }
            VoiceAction.UNKNOWN -> "I didn't understand."
        }
        reply?.let { voiceAssistantManager.speak(it) }
    }

    private fun startRouteWeather() {
        viewModelScope.launch {
            try {
                voiceAssistantManager.speak("Calculating route...")
                
                val originLocation = deviceLocationProvider.getLastKnownLocation()
                val originName = "Current Location"
                val originLon = originLocation?.longitude ?: DemoNavigationDestination.FALLBACK_ORIGIN_LON
                val originLat = originLocation?.latitude ?: DemoNavigationDestination.FALLBACK_ORIGIN_LAT

                val route = navigationRepository.getRoute(
                    originName = originName,
                    originLon = originLon,
                    originLat = originLat,
                    destName = DemoNavigationDestination.NAME,
                    destLon = DemoNavigationDestination.LONGITUDE,
                    destLat = DemoNavigationDestination.LATITUDE
                )
                if (route != null) {
                    navigationManager.startNavigation(route)
                    _navEvents.tryEmit(DashboardNavEvent.WeatherRoute)
                    voiceAssistantManager.speak("Showing weather along your route.")
                } else {
                    voiceAssistantManager.speak("Could not calculate route.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                voiceAssistantManager.speak("Route calculation failed.")
            }
        }
    }
}
