package com.example.autodroid.core.navigation.domain.repository

import com.example.autodroid.core.navigation.Route

interface NavigationRepository {
    suspend fun getRoute(
        originName: String, originLon: Double, originLat: Double, 
        destName: String, destLon: Double, destLat: Double
    ): Route?
}
