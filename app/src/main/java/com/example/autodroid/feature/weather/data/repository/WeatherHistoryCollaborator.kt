package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.data.local.entity.LocationEntity
import com.example.autodroid.feature.weather.data.mapper.toDomain
import com.example.autodroid.feature.weather.data.mapper.toEntity
import com.example.autodroid.feature.weather.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class WeatherHistoryCollaborator @Inject constructor(
    private val weatherDao: WeatherDao,
    private val clock: Clock
) {

    suspend fun resolveCanonicalLocation(location: Location): Location {
        val duplicate = if (weatherDao.getLocation(location.id) != null) {
            null
        } else {
            findDuplicateLocation(location)
        }
        val preferIncoming = duplicate != null && location.id > 0L && duplicate.id < 0L
        return when {
            duplicate == null || preferIncoming -> location
            else -> location.copy(id = duplicate.id)
        }
    }

    suspend fun markLocationViewed(location: Location) {
        val now = clock.millis()
        val existingById = weatherDao.getLocation(location.id)
        if (existingById != null) {
            weatherDao.updateLastViewedAt(location.id, now)
            backfillCountryCode(location.id, location.countryCode, existingById.countryCode)
            return
        }

        val duplicate = findDuplicateLocation(location)
        if (duplicate != null) {
            mergeViewedLocation(incoming = location, existing = duplicate, now = now)
            return
        }

        weatherDao.insertLocation(
            location.toEntity(lastUpdated = now, lastViewedAt = now)
        )
    }

    suspend fun findDuplicateLocation(location: Location): LocationEntity? {
        val near = weatherDao.findLocationsNear(
            latitude = location.latitude,
            longitude = location.longitude,
            delta = LocationHistoryDeduper.PROXIMITY_DEGREES
        )
        val byProximity = near.maxByOrNull { it.lastViewedAt }
        if (byProximity != null) return byProximity

        val country = location.country?.trim().orEmpty()
        val name = location.name.trim()
        return if (name.isEmpty()) {
            null
        } else {
            weatherDao.findLocationsByNameAndCountry(name = name, country = country)
                .maxByOrNull { it.lastViewedAt }
        }
    }

    private suspend fun mergeViewedLocation(incoming: Location, existing: LocationEntity, now: Long) {
        val preferIncomingId = incoming.id > 0L && existing.id < 0L
        if (preferIncomingId) {
            val merged = incoming.copy(
                imageUrl = incoming.imageUrl ?: existing.imageUrl,
                countryCode = incoming.countryCode ?: existing.countryCode
            )
            weatherDao.rekeyLocation(
                oldId = existing.id,
                newLocation = merged.toEntity(
                    lastUpdated = existing.lastUpdated,
                    lastViewedAt = now,
                    placeMetadataUpdatedAt = existing.placeMetadataUpdatedAt
                )
            )
        } else {
            weatherDao.updateLastViewedAt(existing.id, now)
            backfillCountryCode(existing.id, incoming.countryCode, existing.countryCode)
        }
    }

    suspend fun backfillCountryCode(locationId: Long, incoming: String?, existing: String?) {
        if (!existing.isNullOrBlank()) return
        val code = incoming?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return
        weatherDao.updateCountryCode(locationId, code)
    }

    fun observeRecentLocations(limit: Int): Flow<List<Location>> {
        val fetchLimit = (limit * DEDUPE_FETCH_MULTIPLIER).coerceAtLeast(limit)
        return weatherDao.getRecentLocationsFlow(fetchLimit).map { entities ->
            LocationHistoryDeduper.collapse(entities)
                .take(limit)
                .map { it.toDomain() }
        }
    }

    suspend fun evictStaleLocationsIfNeeded() {
        val overflow = weatherDao.getLocationCount() - MAX_CACHED_LOCATIONS
        if (overflow <= 0) return

        val unviewed = weatherDao.getUnviewedOldestIds(overflow)
        unviewed.forEach { locationId ->
            weatherDao.deleteLocationWithForecasts(locationId)
        }
        val remaining = weatherDao.getLocationCount() - MAX_CACHED_LOCATIONS
        if (remaining <= 0) return
        weatherDao.getLeastRecentlyViewedIds(remaining).forEach { locationId ->
            weatherDao.deleteLocationWithForecasts(locationId)
        }
    }

    companion object {
        const val MAX_CACHED_LOCATIONS = 100
        const val DEDUPE_FETCH_MULTIPLIER = 3
    }
}



