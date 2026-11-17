package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.core.navigation.NavCoordinate
import com.example.autodroid.core.navigation.Route
import com.example.autodroid.feature.weather.domain.model.AppResult
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.model.RouteWeatherForecast
import com.example.autodroid.feature.weather.domain.model.RouteWeatherPoint
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RouteWeatherCollaborator @Inject constructor(
    private val forecastCollaborator: WeatherForecastCollaborator
) {
    fun getRouteWeather(route: Route): Flow<RouteWeatherForecast?> = flow {
        emit(null)
        val origin = route.polyline.firstOrNull() ?: return@flow
        val dest = route.polyline.lastOrNull() ?: return@flow

        val originLoc = buildRouteLocation(ROUTE_ORIGIN_ID, route.originName, origin)
        val destLoc = buildRouteLocation(ROUTE_DEST_ID, route.destinationName, dest)

        val (originResult, destResult) = coroutineScope {
            val o = async { forecastCollaborator.getForecastRemote(originLoc) }
            val d = async { forecastCollaborator.getForecastRemote(destLoc) }
            o.await() to d.await()
        }

        val forecast = buildRouteForecast(route, originLoc, destLoc, originResult, destResult)
        emit(forecast)
    }

    private fun buildRouteLocation(
        id: Long, name: String, coord: NavCoordinate
    ): Location = Location(
        id = id, name = name,
        latitude = coord.latitude, longitude = coord.longitude,
        country = null, admin1 = null
    )

    private fun buildRouteForecast(
        route: Route,
        originLoc: Location, destLoc: Location,
        originResult: AppResult<WeatherForecast>, destResult: AppResult<WeatherForecast>
    ): RouteWeatherForecast? {
        if (originResult !is Result.Success || destResult !is Result.Success) return null
        val originDay = originResult.data.dailyForecasts.firstOrNull() ?: return null
        val destDay = destResult.data.dailyForecasts.firstOrNull() ?: return null

        return RouteWeatherForecast(
            origin = originLoc,
            destination = destLoc,
            points = listOf(
                RouteWeatherPoint(route.polyline.first(), 0, originDay),
                RouteWeatherPoint(route.polyline.last(), route.estimatedDuration.inWholeMinutes.toInt(), destDay)
            )
        )
    }

    companion object {
        const val ROUTE_ORIGIN_ID = -9001L
        const val ROUTE_DEST_ID = -9002L
    }
}

