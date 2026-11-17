package com.example.autodroid.core.navigation.data.repository

import com.example.autodroid.core.navigation.NavCoordinate
import com.example.autodroid.core.navigation.Route
import com.example.autodroid.core.navigation.data.remote.OsrmApi
import com.example.autodroid.core.navigation.domain.repository.NavigationRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class NavigationRepositoryImpl @Inject constructor(
    private val api: OsrmApi
) : NavigationRepository {

    override suspend fun getRoute(
        originName: String, originLon: Double, originLat: Double,
        destName: String, destLon: Double, destLat: Double
    ): Route? {
        return try {
            val coords = "$originLon,$originLat;$destLon,$destLat"
            val response = api.getRoute(coords)
            if (response.code != "Ok") return null
            
            val firstRoute = response.routes.firstOrNull() ?: return null
            val geometry = firstRoute.geometry.coordinates

            val downsampled = downsamplePolyline(geometry)
            Route(
                originName = originName,
                destinationName = destName,
                polyline = downsampled,
                estimatedDuration = firstRoute.duration.seconds
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private fun downsamplePolyline(geometry: List<List<Double>>): List<NavCoordinate> {
        val downsampled = mutableListOf<NavCoordinate>()
        val step = if (geometry.size > 100) geometry.size / 100 else 1

        for (i in geometry.indices step step) {
            val point = geometry[i]
            if (point.size >= 2) {
                downsampled.add(NavCoordinate(longitude = point[0], latitude = point[1]))
            }
        }
        if (geometry.isNotEmpty() && downsampled.lastOrNull()?.longitude != geometry.last()[0]) {
            val last = geometry.last()
            downsampled.add(NavCoordinate(longitude = last[0], latitude = last[1]))
        }
        return downsampled
    }
}
