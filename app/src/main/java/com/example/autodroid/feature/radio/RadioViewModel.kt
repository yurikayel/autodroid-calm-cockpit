package com.example.autodroid.feature.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.demo.DemoScenario
import com.example.autodroid.core.demo.DemoScenarioStore
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.usecase.GetRadioStationsUseCase
import com.example.autodroid.domain.usecase.LimitBrowsingWhileDrivingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RadioUiState {
    data object Loading : RadioUiState
    data object Empty : RadioUiState
    data class Error(val message: String) : RadioUiState
    data class Success(val stations: List<RadioStation>, val hiddenCount: Int = 0) : RadioUiState
}

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val getRadioStations: GetRadioStationsUseCase,
    private val limitBrowsingWhileDriving: LimitBrowsingWhileDrivingUseCase,
    private val demoScenarioStore: DemoScenarioStore,
    drivingStateManager: DrivingStateManager
) : ViewModel() {

    private val loadResult = MutableStateFlow<Result<List<RadioStation>>?>(null)
    private var loadJob: Job? = null

    val uiState: StateFlow<RadioUiState> =
        combine(loadResult, drivingStateManager.isDriving) { result, isDriving ->
            when {
                result == null -> RadioUiState.Loading
                result.isFailure -> RadioUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error"
                )
                else -> {
                    val stations = result.getOrThrow()
                    if (stations.isEmpty()) {
                        RadioUiState.Empty
                    } else {
                        val browsing = limitBrowsingWhileDriving(stations, isDriving)
                        RadioUiState.Success(browsing.stations, browsing.hiddenCount)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RadioUiState.Loading)

    val demoScenario: StateFlow<DemoScenario> = demoScenarioStore.scenario

    init {
        loadStations()
    }

    fun loadStations() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadResult.value = null
            loadResult.value = getRadioStations()
        }
    }

    fun selectDemoScenario(scenario: DemoScenario) {
        demoScenarioStore.select(scenario)
        loadStations()
    }
}
