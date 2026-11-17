package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.util.toIsoCountryCode
import com.example.autodroid.feature.weather.ui.util.asUiText

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.navigation.NavigationManager
import com.example.autodroid.core.navigation.Route
import com.example.autodroid.feature.weather.domain.model.AppError
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import com.example.autodroid.feature.weather.domain.usecase.GetRankedActivitiesUseCase
import com.example.autodroid.feature.weather.domain.usecase.GetTopPicksUseCase
import com.example.autodroid.feature.weather.domain.util.ConnectivityObserver
import com.example.autodroid.feature.weather.domain.util.ConnectivityStatus
import com.example.autodroid.feature.weather.ui.map.MapCameraPosition
import com.example.autodroid.feature.weather.ui.map.MapHopProfile
import com.example.autodroid.feature.weather.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Orchestrates the UI logic and state for the main Weather Screen.
 * Employs unidirectional data flow and reacts to network connectivity events.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val getRankedActivities: GetRankedActivitiesUseCase,
    private val getTopPicks: GetTopPicksUseCase,
    private val connectivityObserver: ConnectivityObserver,
    private val searchCoordinator: WeatherSearchCoordinator,
    private val mapGpsCoordinator: WeatherMapGpsCoordinator,
    private val countryCityCatalog: CountryCityCatalog,
    private val drivingStateManager: DrivingStateManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())

    /**
     * The single source of truth for the UI state.
     * Combines search results, home data, and detail forecast data into a reactive stream.
     */
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var forecastJob: Job? = null
    private var contentRevealJob: Job? = null
    private var mapTapJob: Job? = null
    private var deviceLocationJob: Job? = null
    private var routeForecastJob: Job? = null
    private var topPicksJob: Job? = null
    private val countryWarmer = WeatherCountryWarmer(repository, countryCityCatalog, viewModelScope)
    private val hopBuffer = DetailHopBuffer()

    private val connectivityFlow = MutableStateFlow(ConnectivityStatus.Available)

    init {
        observeConnectivity()
        observeDrivingState()
        observeNavigationState()
        observeSearchQueries()
        loadTopPicks()
        observeHistory()
    }

    private fun observeNavigationState() {
        viewModelScope.launch {
            navigationManager.activeRoute.collectLatest { route ->
                _uiState.update { it.copy(activeRoute = route) }
                if (route != null) {
                    loadRouteForecast(route)
                } else {
                    routeForecastJob?.cancel()
                    routeForecastJob = null
                    _uiState.update { it.copy(routeForecast = null) }
                }
            }
        }
    }

    private fun loadRouteForecast(route: Route) {
        routeForecastJob?.cancel()
        routeForecastJob = viewModelScope.launch {
            repository.getRouteWeather(route).collect { forecast ->
                _uiState.update { it.copy(routeForecast = forecast) }
            }
        }
    }

    private fun observeDrivingState() {
        viewModelScope.launch {
            drivingStateManager.isDriving.collect { driving ->
                _uiState.update { it.copy(isDriving = driving) }
            }
        }
    }

    /** Settles in-flight search to an error when connectivity drops. */
    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                connectivityFlow.value = status
                if (status != ConnectivityStatus.Available && _uiState.value.isSearching) {
                    _uiState.update {
                        it.withSearchSettled(AppError.NetworkError.NoConnectivity.asUiText())
                    }
                }
            }
        }
    }

    /** Debounces the search box and geocodes queries longer than two characters. */
    private fun observeSearchQueries() {
        val searchResultsFlow = _uiState
            .map { it.searchResults }
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

        viewModelScope.launch {
            searchCoordinator.observeSearch(
                queries = searchQueryFlow,
                connectivity = connectivityFlow,
                previousResults = searchResultsFlow
            ).collectLatest { outcome ->
                _uiState.update { state ->
                    state.copy(
                        search = outcome.searchState,
                        mapCamera = outcome.mapCamera ?: state.mapCamera,
                        mapPin = outcome.mapPin ?: state.mapPin
                    )
                }
            }
        }
    }


    /** Mirrors the 10 most recently viewed cities into [WeatherUiState.recentHistory]. */
    private fun observeHistory() {
        viewModelScope.launch {
            repository.observeRecentLocations(HISTORY_LIMIT).collect { history ->
                _uiState.update { it.copy(recentHistory = history) }
            }
        }
    }

    /**
     * Loads the population-weighted featured suggestions for the home screen.
     * Pass [forceRefresh] = true (e.g. pull-to-refresh) to bypass the in-memory TTL cache.
     * Cancels an in-flight load so init and PTR cannot finish out of order; a throw
     * settles [WeatherUiState.topPicksFetch] to [FetchStatus.Failed] instead of leaving Loading.
     */
    fun loadTopPicks(forceRefresh: Boolean = false) {
        _uiState.update { state ->
            state.copy(
                topPicksFetch = if (forceRefresh) FetchStatus.Refreshing else FetchStatus.Loading
            )
        }
        topPicksJob?.cancel()
        topPicksJob = viewModelScope.launch {
            try {
                if (!forceRefresh) {
                    delay(TOP_PICKS_LOAD_DEFER_MS.milliseconds)
                }
                val picks = getTopPicks(forceRefresh = forceRefresh)
                _uiState.update {
                    it.copy(
                        topPicks = picks,
                        topPicksFetch = FetchStatus.Idle
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(topPicksFetch = FetchStatus.Failed(AppError.Unknown(e).asUiText()))
                }
            }
        }
    }

    private companion object {
        const val TOP_PICKS_LOAD_DEFER_MS = 400
        const val HISTORY_LIMIT = 10
        const val GPS_COUNTRY_WARM_BUDGET = 8
        const val SELECTION_COUNTRY_WARM_BUDGET = 2
    }

    /**
     * Called when the user types in the search bar.
     * Debounces the query and triggers geocoding searches automatically.
     */
    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        searchQueryFlow.value = query

        if (query.length <= 2) {
            _uiState.update { it.copy(search = SearchUiState.Idle) }
        }
    }

    /**
     * Starts fetch immediately, plants the pin, then picks a [MapHopProfile] from Room freshness
     * and flies the map. Sheet content waits [MapHopProfile.contentRevealMs] (500 ms before land
     * on a cache miss, 200 ms before land when weather is already fresh).
     */
    fun onLocationSelected(location: Location) {
        beginLocationHop(location)
        startForecastJob(location)
        startContentReveal(location)
    }

    /** Cancels a competing map-tap, plants the pin, and clears hop buffers for [location]. */
    private fun beginLocationHop(location: Location) {
        mapTapJob?.cancel()
        mapTapJob = null
        hopBuffer.beginHop(location)
        _uiState.update {
            it.copy(
                search = SearchUiState.Idle,
                query = "",
                mapTapFetch = FetchStatus.Idle,
                mapPin = location
            )
        }
    }

    /** Starts Room collect and refresh as sibling jobs so a later city selection cancels both. */
    private fun startForecastJob(location: Location) {
        forecastJob?.cancel()
        contentRevealJob?.cancel()
        // Collect + refresh are siblings under one Job so selecting another city cancels both.
        forecastJob = viewModelScope.launch {
            launch { collectForecast(location) }
            launch { refreshAndPrefetch(location) }
        }
    }

    /** Applies Room forecast emissions to the UI or buffers them until the hop reveal. */
    private suspend fun collectForecast(location: Location) {
        repository.getForecastFlow(location).collect { forecast ->
            if (forecast != null) bufferOrApplyForecast(location, forecast)
        }
    }

    /** Holds [forecast] until reveal, or paints it immediately if detail is already showing this city. */
    private fun bufferOrApplyForecast(location: Location, forecast: WeatherForecast) {
        val pendingThisCity = hopBuffer.pendingDetailLocation?.id == location.id
        val showingThisCity = _uiState.value.selectedLocation?.id == location.id
        when {
            pendingThisCity && !showingThisCity -> hopBuffer.bufferForecast(forecast)
            showingThisCity -> applyForecastToUi(forecast)
        }
    }

    /** Marks history, refreshes Open-Meteo into Room, then warms nearby hubs. */
    private suspend fun refreshAndPrefetch(location: Location) {
        repository.markLocationViewed(location)
        repository.refreshForecast(location).fold(
            onSuccess = { /* SSOT flow emission. */ },
            onError = { err -> applyRefreshError(location, err.asUiText()) }
        )
        repository.prefetchNearbyCities(location)
        val code = _uiState.value.deviceLocation?.let { countryIsoFor(it) }
            ?: countryIsoFor(location)
        code?.let { countryWarmer.addCountryWarmBudget(it, SELECTION_COUNTRY_WARM_BUDGET) }
    }

    /** Routes a refresh failure onto the forecast lane or the hop buffer. */
    private fun applyRefreshError(location: Location, mappedError: UiText) {
        if (_uiState.value.selectedLocation?.id == location.id) {
            _uiState.update { it.copy(forecastFetch = FetchStatus.Failed(mappedError)) }
            return
        }
        hopBuffer.applyRefreshError(location, mappedError)
    }

    /** Flies the camera with the TTL hop profile, then swaps the sheet to detail. */
    private fun startContentReveal(location: Location) {
        contentRevealJob = viewModelScope.launch {
            val hop = if (repository.hasFreshForecast(location)) {
                MapHopProfile.CACHED
            } else {
                MapHopProfile.CACHE_MISS
            }
            _uiState.update {
                it.copy(mapCamera = location.toMapCamera(MapCameraPosition.DETAIL_ZOOM, hop))
            }
            delay(hop.contentRevealMs.milliseconds)
            revealDetailContent(location)
        }
    }

    /** Switches destination to detail and paints any forecast buffered during the hop. */
    private fun revealDetailContent(location: Location) {
        val ready = hopBuffer.takeReadyForecast(location)
        val pendingError = hopBuffer.takeError(location)
        if (hopBuffer.pendingDetailLocation?.id != location.id) return
        _uiState.update { state ->
            state.copy(
                destination = WeatherDestination.Detail(location),
                forecast = ready,
                rankedActivities = ready?.let { getRankedActivities(it, 0) } ?: emptyList(),
                selectedDayIndex = 0,
                forecastFetch = revealedForecastFetch(ready, pendingError)
            )
        }
        ready?.let { applyForecastToUi(it) }
    }

    /** Re-ranks activities for the current day index from a freshly arrived [forecast]. */
    private fun applyForecastToUi(forecast: WeatherForecast) {
        _uiState.update { state ->
            val maxIndex = maxOf(0, forecast.dailyForecasts.lastIndex)
            val dayIndex = state.selectedDayIndex.coerceIn(0, maxIndex)
            state.copy(
                destination = WeatherDestination.Detail(forecast.location),
                forecast = forecast,
                rankedActivities = getRankedActivities(forecast, dayIndex),
                selectedDayIndex = dayIndex,
                forecastFetch = state.forecastFetch.completeIfInFlight()
            )
        }
    }
    /**
     * Tap / long-press on the map: reverse-geocode then open the same detail flow as search.
     */
    fun onMapTapped(latitude: Double, longitude: Double) {
        if (connectivityFlow.value != ConnectivityStatus.Available) {
            _uiState.update { it.copy(mapTapFetch = FetchStatus.Failed(AppError.NetworkError.NoConnectivity.asUiText())) }
            return
        }
        startMapTap(latitude, longitude)
    }

    /** Centers the camera on the tap and starts reverse-geocoding that point. */
    private fun startMapTap(latitude: Double, longitude: Double) {
        mapTapJob?.cancel()
        _uiState.update {
            it.copy(
                mapTapFetch = FetchStatus.Loading,
                mapCamera = MapCameraPosition(
                    latitude = latitude,
                    longitude = longitude,
                    zoom = MapCameraPosition.CITY_ZOOM
                )
            )
        }
        mapTapJob = viewModelScope.launch {
            val outcome = mapGpsCoordinator.resolveMapTap(
                latitude,
                longitude,
                connectivityFlow.value
            )
            when (outcome) {
                is MapTapOutcome.Success -> onLocationSelected(outcome.location)
                is MapTapOutcome.Error -> {
                    _uiState.update {
                        it.copy(mapTapFetch = FetchStatus.Failed(outcome.error))
                    }
                }
            }
        }
    }

    /** Re-ranks activities for the selected day without any network work. */
    fun onDaySelected(dayIndex: Int) {
        val forecast = _uiState.value.forecast ?: return
        if (dayIndex !in forecast.dailyForecasts.indices) return
        _uiState.update {
            it.copy(
                selectedDayIndex = dayIndex,
                rankedActivities = getRankedActivities(forecast, dayIndex)
            )
        }
    }

    /**
     * Returns from the detail view to the home screen and re-centers on the device location
     * when available (static London default otherwise).
     * Keeps [deviceLocation] so the current-location chip stays available.
     */
    fun onBack() {
        cancelDetailJobs()
        _uiState.update { it.toHome() }
    }

    /** Cancels hop/forecast/map-tap jobs and clears buffers so home cannot flash stale detail. */
    private fun cancelDetailJobs() {
        forecastJob?.cancel()
        contentRevealJob?.cancel()
        mapTapJob?.cancel()
        mapTapJob = null
        hopBuffer.clear()
    }

    /**
     * Called from the UI after the runtime location permission dialog.
     * When granted, reverse-geocodes the last-known fix for the home chip and map center —
     * does **not** open detail; the user opts in via [onCurrentLocationClick].
     * When denied, home stays on the static default framing and the chip remains hidden.
     */
    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            viewModelScope.launch { mapGpsCoordinator.settleTheme() }
            return
        }
        resolveDeviceLocation()
    }

    /** Home header chip: quick-check weather for the reverse-geocoded device city. */
    fun onCurrentLocationClick() {
        val location = _uiState.value.deviceLocation ?: return
        onLocationSelected(location)
    }

    /** Reverse-geocodes last-known GPS for the home chip and, on home, recenters the map. */
    private fun resolveDeviceLocation() {
        deviceLocationJob?.cancel()
        deviceLocationJob = viewModelScope.launch {
            val location = mapGpsCoordinator.resolveDeviceLocation(connectivityFlow.value)
            if (location != null) {
                _uiState.update { state ->
                    val isHome = state.destination is WeatherDestination.Home
                    state.copy(
                        deviceLocation = location,
                        mapCamera = if (isHome) {
                            location.toMapCamera(
                                MapCameraPosition.HOME_DEFAULT_ZOOM,
                                MapHopProfile.CACHED
                            )
                        } else {
                            state.mapCamera
                        }
                    )
                }
                countryIsoFor(location)?.let { countryWarmer.addCountryWarmBudget(it, GPS_COUNTRY_WARM_BUDGET) }
            }
        }
    }
    private fun countryIsoFor(location: Location): String? {
        val stored = location.countryCode.toIsoCountryCode()
        return stored ?: countryCityCatalog.isoForCountryName(location.country)
    }

    /**
     * Refresh entry point used by home pull-to-refresh (bonus): force-refreshes top picks
     * (bypassing TTL). When a location is selected, refreshes that city's forecast into Room
     * (kept for callers/tests; the detail UI no longer exposes PTR so it won't fight sheet drag).
     */
    fun refresh() {
        val location = _uiState.value.selectedLocation
        if (location == null) {
            refreshHome()
            return
        }
        refreshSelectedForecast(location)
    }

    /** Pull-to-refresh on home: force-reloads featured cities when online. */
    private fun refreshHome() {
        if (connectivityFlow.value != ConnectivityStatus.Available) {
            _uiState.update {
                it.copy(topPicksFetch = FetchStatus.Failed(AppError.NetworkError.NoConnectivity.asUiText()))
            }
            return
        }
        loadTopPicks(forceRefresh = true)
    }

    /** Force-refreshes the selected city's forecast into Room for callers/tests. */
    private fun refreshSelectedForecast(location: Location) {
        if (connectivityFlow.value != ConnectivityStatus.Available) {
            _uiState.update {
                it.copy(forecastFetch = FetchStatus.Failed(AppError.NetworkError.NoConnectivity.asUiText()))
            }
            return
        }
        viewModelScope.launch {
            repository.refreshForecast(location, force = true).fold(
                onSuccess = { _uiState.update { it.copy(forecastFetch = FetchStatus.Idle) } },
                onError = { err -> _uiState.update { it.copy(forecastFetch = FetchStatus.Failed(err.asUiText())) } }
            )
        }
    }
}



















