package com.example.autodroid.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

data class NavCoordinate(val latitude: Double, val longitude: Double)

data class Route(
    val originName: String,
    val destinationName: String,
    val polyline: List<NavCoordinate>,
    val estimatedDuration: Duration
)

enum class NavigationState {
    IDLE,
    NAVIGATING
}

@Singleton
class NavigationManager @Inject constructor() {
    private val _navState = MutableStateFlow(NavigationState.IDLE)
    val navState: StateFlow<NavigationState> = _navState.asStateFlow()

    private val _activeRoute = MutableStateFlow<Route?>(null)
    val activeRoute: StateFlow<Route?> = _activeRoute.asStateFlow()

    fun startNavigation(route: Route) {
        _activeRoute.value = route
        _navState.value = NavigationState.NAVIGATING
    }

    fun stopNavigation() {
        _activeRoute.value = null
        _navState.value = NavigationState.IDLE
    }
}
