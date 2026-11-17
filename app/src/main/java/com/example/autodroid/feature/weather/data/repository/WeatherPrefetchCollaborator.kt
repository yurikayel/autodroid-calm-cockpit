package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.domain.model.CountryPrefetchResult
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.model.CachePolicy
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import com.example.autodroid.feature.weather.domain.usecase.HubCities
import com.example.autodroid.feature.weather.domain.usecase.NearbyCities
import com.example.autodroid.feature.weather.domain.util.toIsoCountryCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.time.Clock
import javax.inject.Inject

class WeatherPrefetchCollaborator @Inject constructor(
    private val forecastCollaborator: WeatherForecastCollaborator,
    private val historyCollaborator: WeatherHistoryCollaborator,
    private val weatherDao: WeatherDao,
    private val countryCityCatalog: CountryCityCatalog,
    private val clock: Clock
) {
    suspend fun prefetchNearbyCities(origin: Location) {
        val neighbors = NearbyCities.select(origin, HubCities.all)
        neighbors.forEachIndexed { index, nearby ->
            if (index > 0) delay(PREFETCH_STAGGER_MS)
            try {
                forecastCollaborator.refreshForecast(nearby, force = false)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    suspend fun prefetchCountryCities(countryCode: String, limit: Int): CountryPrefetchResult {
        val code = countryCode.toIsoCountryCode()
        val pending = if (code.isNullOrEmpty() || limit <= 0) {
            emptyList()
        } else {
            countryCityCatalog.citiesFor(code).filter { city -> !isCountryCityWarmed(city) }
        }
        if (pending.isEmpty()) return CountryPrefetchResult(0, 0)
        var warmed = 0
        pending.take(limit).forEachIndexed { index, city ->
            if (index > 0) delay(COUNTRY_WARM_STAGGER_MS)
            if (warmCountryCity(city)) warmed += 1
        }
        return CountryPrefetchResult(warmed, (pending.size - warmed).coerceAtLeast(0))
    }

    private suspend fun warmCountryCity(city: Location): Boolean {
        return try {
            forecastCollaborator.refreshForecast(city, force = false) is Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun isCountryCityWarmed(city: Location): Boolean {
        val existing = weatherDao.getLocation(city.id) ?: historyCollaborator.findDuplicateLocation(city)
        if (existing == null) return false
        val days = weatherDao.getDailyForecasts(existing.id)
        return CachePolicy.isWeatherFresh(existing.lastUpdated, days.isNotEmpty(), clock.millis())
    }

    companion object {
        const val PREFETCH_STAGGER_MS = 150L
        const val COUNTRY_WARM_STAGGER_MS = 2_000L
    }
}




