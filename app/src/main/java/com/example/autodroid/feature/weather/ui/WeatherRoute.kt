package com.example.autodroid.feature.weather.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WeatherRoute() {
    val viewModel = hiltViewModel<WeatherViewModel>()
    WeatherScreen(
        viewModel = viewModel,
        isDarkTheme = isSystemInDarkTheme()
    )
}
