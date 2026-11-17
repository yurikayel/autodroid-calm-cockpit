package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import com.example.autodroid.feature.weather.ui.util.UiText

class DetailHopBuffer {
    var pendingDetailLocation: Location? = null
        private set
    var bufferedForecast: WeatherForecast? = null
        private set
    var bufferedError: UiText? = null
        private set

    fun beginHop(location: Location) {
        pendingDetailLocation = location
        bufferedForecast = null
        bufferedError = null
    }

    fun applyRefreshError(location: Location, mappedError: UiText) {
        val pending = pendingDetailLocation ?: return
        if (pending.id == location.id || namesMatch(pending, location)) {
            bufferedError = mappedError
        }
    }

    fun bufferForecast(forecast: WeatherForecast) {
        val pending = pendingDetailLocation ?: return
        if (pending.id == forecast.location.id || namesMatch(pending, forecast.location)) {
            bufferedForecast = forecast
        }
    }

    fun clear() {
        pendingDetailLocation = null
        bufferedForecast = null
        bufferedError = null
    }

    fun takeReadyForecast(location: Location): WeatherForecast? {
        val pending = pendingDetailLocation ?: return null
        if (pending.id != location.id && !namesMatch(pending, location)) return null
        val ready = bufferedForecast?.takeIf {
            it.location.id == location.id || namesMatch(it.location, location)
        }
        if (ready != null) {
            bufferedForecast = null
        }
        return ready
    }

    fun takeError(location: Location): UiText? {
        val pending = pendingDetailLocation ?: return null
        if (pending.id != location.id && !namesMatch(pending, location)) return null
        val error = bufferedError
        bufferedError = null
        return error
    }

    private fun namesMatch(a: Location, b: Location): Boolean =
        a.name.equals(b.name, ignoreCase = true) &&
            a.country.equals(b.country, ignoreCase = true)
}
