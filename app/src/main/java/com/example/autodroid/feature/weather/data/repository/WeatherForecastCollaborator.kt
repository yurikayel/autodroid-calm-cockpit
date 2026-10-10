package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.image.PlaceImagePrefetcher
import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.data.local.entity.DailyForecastEntity
import com.example.autodroid.feature.weather.data.local.entity.LocationEntity
import com.example.autodroid.feature.weather.data.mapper.toEntity
import com.example.autodroid.feature.weather.data.remote.ForecastApi
import com.example.autodroid.feature.weather.data.remote.MarineApi
import com.example.autodroid.feature.weather.data.remote.WikipediaPlaceImageResolver
import com.example.autodroid.feature.weather.data.remote.dto.DailyForecastDto
import com.example.autodroid.feature.weather.domain.model.AppResult
import com.example.autodroid.feature.weather.domain.model.CachePolicy
import com.example.autodroid.feature.weather.domain.model.DailyForecast
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.model.WeatherForecast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Clock
import javax.inject.Inject

class WeatherForecastCollaborator @Inject constructor(
    private val forecastApi: ForecastApi,
    private val marineApi: MarineApi,
    private val placeImageResolver: WikipediaPlaceImageResolver,
    private val placeImagePrefetcher: PlaceImagePrefetcher,
    private val weatherDao: WeatherDao,
    private val historyCollaborator: WeatherHistoryCollaborator,
    private val clock: java.time.Clock
) {

    suspend fun refreshForecast(location: Location, force: Boolean): AppResult<Unit> {
        return try {
            val canonical = historyCollaborator.resolveCanonicalLocation(location)
            var existing = weatherDao.getLocation(canonical.id)
            val existingDays = weatherDao.getDailyForecasts(canonical.id)
            val now = clock.millis()
            if (force || !CachePolicy.isWeatherFresh(existing?.lastUpdated ?: 0L, existingDays.isNotEmpty(), now)) {
                val forecast = fetchRemoteForecast(canonical, existing)
                persistRefreshedForecast(forecast, existing, now)
                historyCollaborator.evictStaleLocationsIfNeeded()
                existing = weatherDao.getLocation(canonical.id)
            } else {
                historyCollaborator.backfillCountryCode(canonical.id, canonical.countryCode, existing?.countryCode)
            }
            refreshPlaceImageIfNeeded(canonical, existing, clock.millis())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    private suspend fun persistRefreshedForecast(
        forecast: WeatherForecast,
        existing: LocationEntity?,
        now: Long
    ) {
        weatherDao.insertLocationWithForecast(
            location = forecast.location.copy(
                countryCode = forecast.location.countryCode ?: existing?.countryCode,
                imageUrl = forecast.location.imageUrl ?: existing?.imageUrl
            ).toEntity(
                lastUpdated = now,
                lastViewedAt = existing?.lastViewedAt ?: 0L,
                placeMetadataUpdatedAt = existing?.placeMetadataUpdatedAt ?: 0L
            ),
            forecasts = forecast.dailyForecasts.map { it.toEntity(forecast.location.id) }
        )
    }

    private suspend fun refreshPlaceImageIfNeeded(
        location: Location,
        existing: LocationEntity?,
        now: Long
    ) {
        val cachedUrl = location.imageUrl ?: existing?.imageUrl
        val metadataAt = existing?.placeMetadataUpdatedAt ?: 0L
        if (!CachePolicy.shouldFetchPlaceImage(cachedUrl, metadataAt, now)) {
            placeImagePrefetcher.prefetch(cachedUrl)
            return
        }
        val resolved = placeImageResolver.resolve(location.name, location.country)
        val finalUrl = resolved ?: cachedUrl
        weatherDao.updatePlaceImage(
            locationId = location.id,
            imageUrl = finalUrl,
            timestamp = now
        )
        placeImagePrefetcher.prefetch(finalUrl)
    }

    private suspend fun fetchRemoteForecast(
        location: Location,
        existing: LocationEntity?
    ): WeatherForecast = coroutineScope {
        val forecastDeferred = async {
            forecastApi.getForecast(
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
        val waveDeferred = async { fetchWaveHeightsByDate(location) }
        val forecastResponse = forecastDeferred.await()
        val waveByDate = waveDeferred.await()
        val hasSeaAccess = waveByDate?.values?.any { it != null } ?: location.hasSeaAccess
        WeatherForecast(
            location = location.copy(
                hasSeaAccess = hasSeaAccess,
                imageUrl = location.imageUrl ?: existing?.imageUrl
            ),
            dailyForecasts = mapDailyForecasts(forecastResponse.daily, waveByDate)
        )
    }

    suspend fun getForecastRemote(location: Location): AppResult<WeatherForecast> {
        return try {
            coroutineScope {
                val weatherDeferred = async {
                    fetchRemoteForecast(location, existing = null)
                }
                val imageDeferred = async {
                    placeImageResolver.resolve(location.name, location.country)
                }
                val weather = weatherDeferred.await()
                val imageUrl = imageDeferred.await()
                Result.Success(
                    weather.copy(location = weather.location.copy(imageUrl = imageUrl))
                )
            }
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    private fun mapDailyForecasts(
        daily: DailyForecastDto,
        waveByDate: Map<String, Double?>?
    ): List<DailyForecast> {
        return daily.time.indices.map { i ->
            val date = daily.time.getOrNull(i) ?: ""
            DailyForecast(
                date = date,
                weatherCode = daily.weatherCode.getOrNull(i) ?: 0,
                maxTemp = daily.temperature2mMax.getOrNull(i) ?: 0.0,
                minTemp = daily.temperature2mMin.getOrNull(i) ?: 0.0,
                precipitationSum = daily.precipitationSum.getOrNull(i) ?: 0.0,
                snowfallSum = daily.snowfallSum.getOrNull(i) ?: 0.0,
                maxWindSpeed = daily.windSpeed10mMax.getOrNull(i) ?: 0.0,
                waveHeightMax = waveByDate?.get(date)
            )
        }
    }

    private suspend fun fetchWaveHeightsByDate(location: Location): Map<String, Double?>? {
        return try {
            val marine = marineApi.getMarine(
                latitude = location.latitude,
                longitude = location.longitude
            )
            val marineDaily = marine.daily ?: return null
            marineDaily.time.indices.associateBy({ marineDaily.time[it] }) { i ->
                marineDaily.waveHeightMax.getOrNull(i)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }



    private fun DailyForecast.toEntity(locationId: Long) = DailyForecastEntity(
        locationId = locationId,
        date = date,
        maxTemp = maxTemp,
        minTemp = minTemp,
        weatherCode = weatherCode,
        precipitationSum = precipitationSum,
        maxWindSpeed = maxWindSpeed,
        snowfallSum = snowfallSum,
        waveHeightMax = waveHeightMax
    )
}







