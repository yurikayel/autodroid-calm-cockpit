package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.data.preferences.FirstRunThemeSettler
import com.example.autodroid.feature.weather.domain.location.DeviceLocationProvider
import com.example.autodroid.feature.weather.domain.model.AppError
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.util.ConnectivityStatus
import com.example.autodroid.feature.weather.ui.util.UiText
import com.example.autodroid.feature.weather.ui.util.asUiText
import javax.inject.Inject

sealed class MapTapOutcome {
    data class Success(val location: Location) : MapTapOutcome()
    data class Error(val error: UiText) : MapTapOutcome()
}



class WeatherMapGpsCoordinator @Inject constructor(
    private val repository: WeatherRepository,
    private val deviceLocationProvider: DeviceLocationProvider,
    private val firstRunThemeSettler: FirstRunThemeSettler
) {
    suspend fun resolveMapTap(
        latitude: Double,
        longitude: Double,
        connectivity: ConnectivityStatus
    ): MapTapOutcome {
        if (connectivity != ConnectivityStatus.Available) {
            return MapTapOutcome.Error(AppError.NetworkError.NoConnectivity.asUiText())
        }
        return when (val result = repository.reverseGeocode(latitude, longitude)) {
            is Result.Success -> MapTapOutcome.Success(result.data)
            is Result.Error -> MapTapOutcome.Error(result.error.asUiText())
        }
    }

    suspend fun resolveDeviceLocation(
        connectivity: ConnectivityStatus
    ): Location? {
        val coords = deviceLocationProvider.getLastKnownLocation()
        firstRunThemeSettler.settle()
        if (coords == null || connectivity != ConnectivityStatus.Available) return null

        return when (val result = repository.reverseGeocode(coords.latitude, coords.longitude)) {
            is Result.Success -> result.data
            is Result.Error -> null
        }
    }

    suspend fun settleTheme() {
        firstRunThemeSettler.settle()
    }
}


