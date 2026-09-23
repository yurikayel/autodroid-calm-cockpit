package com.example.autodroid.feature.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.usecase.GetStationByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RadioDetailUiState {
    data object Loading : RadioDetailUiState
    data object NotFound : RadioDetailUiState
    data class Error(val message: String) : RadioDetailUiState
    data class Success(val station: RadioStation) : RadioDetailUiState
}

@HiltViewModel(assistedFactory = RadioDetailViewModel.Factory::class)
class RadioDetailViewModel @AssistedInject constructor(
    @Assisted private val stationId: String,
    private val getStationById: GetStationByIdUseCase,
    private val nowPlayingStore: NowPlayingStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<RadioDetailUiState>(RadioDetailUiState.Loading)
    val uiState: StateFlow<RadioDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RadioDetailUiState.Loading
            _uiState.value = getStationById(stationId).fold(
                onSuccess = { station ->
                    if (station != null) {
                        nowPlayingStore.select(station)
                        RadioDetailUiState.Success(station)
                    } else {
                        RadioDetailUiState.NotFound
                    }
                },
                onFailure = { RadioDetailUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): RadioDetailViewModel
    }
}
