package com.example.autodroid.feature.weather.ui

import com.example.autodroid.core.navigation.Route
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.RankedActivity
import com.example.autodroid.feature.weather.domain.model.RouteWeatherForecast
import com.example.autodroid.feature.weather.domain.model.TopPick
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import com.example.autodroid.feature.weather.ui.map.MapCameraPosition
import com.example.autodroid.feature.weather.ui.map.MapHopProfile
import com.example.autodroid.feature.weather.ui.util.UiText

/**
 * Immutable state representing the entire Weather UI.
 *
 * The screen has two modes derived from [destination]:
 *  - **Home** ([WeatherDestination.Home]): a search bar plus a feed of [topPicks].
 *  - **Detail** ([WeatherDestination.Detail]): the forecast with a per-day selector; [rankedActivities]
 *    always reflects [selectedDayIndex].
 *
 * The map is driven by [mapCamera] / [mapPin]; [WeatherScreenContent] keeps a single map
 * instance whose layout height tracks the sheet while the sheet body Crossfades home↔detail.
 */
data class WeatherUiState(
    val query: String = "",
    val search: SearchUiState = SearchUiState.Idle,
    val destination: WeatherDestination = WeatherDestination.Home,
    val forecast: WeatherForecast? = null,
    val forecastFetch: FetchStatus = FetchStatus.Idle,
    val selectedDayIndex: Int = 0,
    val rankedActivities: List<RankedActivity> = emptyList(),
    val topPicks: List<TopPick> = emptyList(),
    val topPicksFetch: FetchStatus = FetchStatus.Idle,
    val recentHistory: List<Location> = emptyList(),
    val mapCamera: MapCameraPosition = MapCameraPosition.DEFAULT,
    val mapPin: Location? = null,
    val mapTapFetch: FetchStatus = FetchStatus.Idle,
    val deviceLocation: Location? = null,
    val isDriving: Boolean = false,
    val activeRoute: Route? = null,
    val routeForecast: RouteWeatherForecast? = null
) {
    /** Convenience accessor for detail mode; null on [WeatherDestination.Home]. */
    val selectedLocation: Location?
        get() = (destination as? WeatherDestination.Detail)?.location

    val isSearching: Boolean get() = search is SearchUiState.Loading

    val searchResults: List<Location>
        get() = when (val current = search) {
            SearchUiState.Idle -> emptyList()
            is SearchUiState.Loading -> current.previousResults
            is SearchUiState.Results -> current.locations
            is SearchUiState.Failed -> current.previousResults
        }

    val searchError: UiText?
        get() = (search as? SearchUiState.Failed)?.error

    val isLoadingForecast: Boolean get() = forecastFetch.isLoading()

    val isLoadingTopPicks: Boolean get() = topPicksFetch.isLoading()

    val isRefreshingTopPicks: Boolean get() = topPicksFetch.isRefreshing()

    val isResolvingMapTap: Boolean get() = mapTapFetch.isLoading()

    /** Home-visible failures from search, map-tap, and Top Picks — never the forecast lane. */
    fun homeLaneErrors(): List<UiText> = listOfNotNull(
        searchError,
        mapTapFetch.errorOrNull(),
        topPicksFetch.errorOrNull()
    )
}

/** Clears detail fields and recenters the camera on the device city or the London default. */
fun WeatherUiState.toHome(): WeatherUiState = copy(
    destination = WeatherDestination.Home,
    forecast = null,
    rankedActivities = emptyList(),
    selectedDayIndex = 0,
    query = "",
    search = SearchUiState.Idle,
    forecastFetch = FetchStatus.Idle,
    mapCamera = deviceLocation?.toMapCamera(
        MapCameraPosition.HOME_DEFAULT_ZOOM,
        MapHopProfile.CACHED
    ) ?: MapCameraPosition.DEFAULT,
    mapPin = null,
    mapTapFetch = FetchStatus.Idle
)

/** Attaches [error] to the search lane while keeping any previous hits visible. */
fun WeatherUiState.withSearchSettled(error: UiText): WeatherUiState {
    return copy(search = SearchUiState.Failed(error, searchResults))
}

/** Forecast lane status at sheet reveal: failed, first-load, or already painted. */
fun revealedForecastFetch(ready: WeatherForecast?, pendingError: UiText?): FetchStatus {
    if (pendingError != null) return FetchStatus.Failed(pendingError)
    return if (ready == null) FetchStatus.Loading else FetchStatus.Idle
}

/** Builds a camera target at this city's coordinates with the given zoom and hop profile. */
fun Location.toMapCamera(
    zoom: Double,
    hop: MapHopProfile = MapHopProfile.CACHE_MISS
) = MapCameraPosition(
    latitude = latitude,
    longitude = longitude,
    zoom = zoom,
    hop = hop
)
