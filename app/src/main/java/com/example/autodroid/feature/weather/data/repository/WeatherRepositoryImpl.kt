package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.data.mapper.toDomain
import com.example.autodroid.feature.weather.domain.model.AppResult
import com.example.autodroid.feature.weather.domain.model.CountryPrefetchResult
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.model.RouteWeatherForecast
import com.example.autodroid.feature.weather.domain.model.RouteWeatherPoint
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import com.example.autodroid.feature.weather.domain.usecase.HubCities
import com.example.autodroid.feature.weather.domain.usecase.NearbyCities
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import com.example.autodroid.core.navigation.NavCoordinate
import com.example.autodroid.core.navigation.Route
import java.time.Clock
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val geocodingCollaborator: WeatherGeocodingCollaborator,
    private val forecastCollaborator: WeatherForecastCollaborator,
    private val historyCollaborator: WeatherHistoryCollaborator,
    private val weatherDao: WeatherDao,
    private val countryCityCatalog: CountryCityCatalog,
    private val prefetchCollaborator: WeatherPrefetchCollaborator,
    private val routeCollaborator: RouteWeatherCollaborator,
    private val clock: Clock
) : WeatherRepository {

    override suspend fun searchCity(query: String): AppResult<List<Location>> {
        return geocodingCollaborator.searchCity(query)
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): AppResult<Location> {
        return geocodingCollaborator.reverseGeocode(latitude, longitude)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getForecastFlow(location: Location): Flow<WeatherForecast?> {
        return canonicalLocationFlow(location).flatMapLatest { canonical ->
            forecastFlowForId(canonical)
        }
    }

    private fun canonicalLocationFlow(location: Location): Flow<Location> = flow {
        var current = historyCollaborator.resolveCanonicalLocation(location)
        while (true) {
            emit(current)
            weatherDao.getLocationFlow(current.id).first { it == null }
            val next = historyCollaborator.resolveCanonicalLocation(location)
            if (next.id == current.id) {
                awaitCancellation()
            }
            current = next
        }
    }

    private fun forecastFlowForId(location: Location): Flow<WeatherForecast?> {
        return combine(
            weatherDao.getLocationFlow(location.id),
            weatherDao.getDailyForecastsFlow(location.id)
        ) { locationEntity, forecastEntities ->
            if (forecastEntities.isEmpty()) {
                null
            } else {
                val resolvedLocation = locationEntity?.toDomain() ?: location
                WeatherForecast(resolvedLocation, forecastEntities.map { it.toDomain() })
            }
        }
    }

    override suspend fun hasFreshForecast(location: Location): Boolean {
        val canonical = historyCollaborator.resolveCanonicalLocation(location)
        val existing = weatherDao.getLocation(canonical.id)
        val existingDays = weatherDao.getDailyForecasts(canonical.id)
        return com.example.autodroid.feature.weather.domain.model.CachePolicy.isWeatherFresh(existing?.lastUpdated ?: 0L, existingDays.isNotEmpty(), clock.millis())
    }

    override suspend fun refreshForecast(location: Location, force: Boolean): AppResult<Unit> {
        return forecastCollaborator.refreshForecast(location, force)
    }

    override suspend fun prefetchNearbyCities(origin: Location) {
        prefetchCollaborator.prefetchNearbyCities(origin)
    }

    override suspend fun prefetchCountryCities(countryCode: String, limit: Int): CountryPrefetchResult {
        return prefetchCollaborator.prefetchCountryCities(countryCode, limit)
    }

    override suspend fun getForecastRemote(location: Location): AppResult<WeatherForecast> {
        return forecastCollaborator.getForecastRemote(location)
    }

    override fun observeRecentLocations(limit: Int): Flow<List<Location>> {
        return historyCollaborator.observeRecentLocations(limit)
    }

    override suspend fun markLocationViewed(location: Location) {
        historyCollaborator.markLocationViewed(location)
    }

    override fun getRouteWeather(route: Route): Flow<RouteWeatherForecast?> {
        return routeCollaborator.getRouteWeather(route)
    }
}





