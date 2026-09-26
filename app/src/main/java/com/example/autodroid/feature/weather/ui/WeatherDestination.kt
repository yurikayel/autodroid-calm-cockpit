package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.Location

/** Explicit navigation target for the weather screen (home vs city detail). */
sealed interface WeatherDestination {
    data object Home : WeatherDestination
    data class Detail(val location: Location) : WeatherDestination
}

