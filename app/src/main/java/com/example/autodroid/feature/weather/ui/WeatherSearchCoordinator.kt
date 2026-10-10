package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.AppError
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.util.ConnectivityStatus
import com.example.autodroid.feature.weather.ui.map.MapCameraPosition
import com.example.autodroid.feature.weather.ui.map.MapHopProfile
import com.example.autodroid.feature.weather.ui.util.UiText
import com.example.autodroid.feature.weather.ui.util.asUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class SearchOutcome(
    val searchState: SearchUiState,
    val mapCamera: MapCameraPosition?,
    val mapPin: Location?
)

class WeatherSearchCoordinator @Inject constructor(
    private val repository: WeatherRepository
) {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeSearch(
        queries: Flow<String>,
        connectivity: StateFlow<ConnectivityStatus>,
        previousResults: StateFlow<List<Location>>
    ): Flow<SearchOutcome> {
        return queries
            .debounce(500.milliseconds)
            .filter { it.length > 2 }
            .distinctUntilChanged()
            .transformLatest { query ->
                emit(SearchOutcome(SearchUiState.Loading(previousResults.value), null, null))
                emit(executeSearch(query, connectivity.value, previousResults.value))
            }
    }

    private suspend fun executeSearch(
        query: String,
        connectivity: ConnectivityStatus,
        previousResults: List<Location>
    ): SearchOutcome {
        if (connectivity != ConnectivityStatus.Available) {
            val error = AppError.NetworkError.NoConnectivity.asUiText()
            return SearchOutcome(
                searchState = SearchUiState.Failed(error, previousResults),
                mapCamera = null,
                mapPin = null
            )
        }
        return when (val result = repository.searchCity(query)) {
            is Result.Success -> applySearchResults(result.data)
            is Result.Error -> SearchOutcome(
                searchState = SearchUiState.Failed(result.error.asUiText(), previousResults),
                mapCamera = null,
                mapPin = null
            )
        }
    }

    private fun applySearchResults(locations: List<Location>): SearchOutcome {
        val preview = locations.firstOrNull()
        val camera = preview?.let {
            MapCameraPosition(
                latitude = it.latitude,
                longitude = it.longitude,
                zoom = MapCameraPosition.CITY_ZOOM,
                hop = MapHopProfile.CACHED
            )
        }
        return SearchOutcome(
            searchState = SearchUiState.Results(locations),
            mapCamera = camera,
            mapPin = preview
        )
    }
}


