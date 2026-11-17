package com.example.autodroid.core.navigation.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OsrmResponse(
    val code: String,
    val routes: List<OsrmRoute> = emptyList()
)

@Serializable
data class OsrmRoute(
    val distance: Double,
    val duration: Double,
    val geometry: OsrmGeometry
)

@Serializable
data class OsrmGeometry(
    val coordinates: List<List<Double>>
)
