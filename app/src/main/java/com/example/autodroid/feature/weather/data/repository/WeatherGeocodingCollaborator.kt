package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.remote.GeocodingApi
import com.example.autodroid.feature.weather.data.remote.NominatimApi
import com.example.autodroid.feature.weather.data.remote.dto.NominatimResponse
import com.example.autodroid.feature.weather.domain.model.AppResult
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import javax.inject.Inject
import kotlin.math.abs

class WeatherGeocodingCollaborator @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val nominatimApi: NominatimApi,
    private val historyCollaborator: WeatherHistoryCollaborator
) {

    suspend fun searchCity(query: String): AppResult<List<Location>> {
        return try {
            val response = geocodingApi.searchCity(query)
            val locations = response.results?.map { dto ->
                Location(
                    id = dto.id,
                    name = dto.name,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    country = dto.country,
                    admin1 = dto.admin1,
                    elevation = dto.elevation,
                    population = dto.population,
                    featureCode = dto.featureCode,
                    countryCode = dto.countryCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                )
            } ?: emptyList()
            Result.Success(locations)
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): AppResult<Location> {
        return try {
            val nominatim = nominatimApi.reverseGeocode(latitude, longitude)
                .toLocation(fallbackLat = latitude, fallbackLng = longitude)
            Result.Success(stabilizeReverseGeocode(nominatim))
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    private suspend fun stabilizeReverseGeocode(nominatim: Location): Location {
        val cached = historyCollaborator.resolveCanonicalLocation(nominatim)
        if (cached.id != nominatim.id) {
            return cached.copy(countryCode = cached.countryCode ?: nominatim.countryCode)
        }
        val matched = matchForwardGeocode(nominatim)
        return matched?.copy(countryCode = matched.countryCode ?: nominatim.countryCode) ?: nominatim
    }

    private suspend fun matchForwardGeocode(nominatim: Location): Location? {
        val hits = when (val result = searchCity(nominatim.name)) {
            is Result.Success -> result.data
            is Result.Error -> return null
        }
        return LocationHistoryDeduper.nearestWithinProximity(
            latitude = nominatim.latitude,
            longitude = nominatim.longitude,
            candidates = hits,
            latOf = { it.latitude },
            lngOf = { it.longitude }
        )
    }

    private fun NominatimResponse.toLocation(fallbackLat: Double, fallbackLng: Double): Location {
        val address = address
        val placeName = listOfNotNull(
            address?.city,
            address?.town,
            address?.village,
            address?.municipality,
            name?.takeIf { it.isNotBlank() },
            address?.county
        ).firstOrNull()
            ?: displayName?.substringBefore(',')?.trim()
            ?: "Dropped pin"

        val lat = lat.toDoubleOrNull() ?: fallbackLat
        val lng = lon.toDoubleOrNull() ?: fallbackLng
        val syntheticId = -(1_000_000L + abs(placeId))

        return Location(
            id = syntheticId,
            name = placeName,
            latitude = lat,
            longitude = lng,
            country = address?.country,
            admin1 = address?.state,
            countryCode = address?.countryCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        )
    }
}


