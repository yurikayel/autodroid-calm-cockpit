package com.example.autodroid.core.navigation.data.remote

import com.example.autodroid.core.navigation.data.remote.dto.OsrmResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OsrmApi {
    @GET("route/v1/driving/{coordinates}?overview=full&geometries=geojson")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String
    ): OsrmResponse
}
