package com.example.autodroid.feature.dashboard

sealed interface DashboardNavEvent {
    data object Weather : DashboardNavEvent
    data object WeatherRoute : DashboardNavEvent
}
