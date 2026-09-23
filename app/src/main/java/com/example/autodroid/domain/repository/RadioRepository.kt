package com.example.autodroid.domain.repository

import com.example.autodroid.domain.model.RadioStation

interface RadioRepository {
    suspend fun getStations(): Result<List<RadioStation>>

    /** Success(null) means the station does not exist. */
    suspend fun getStation(id: String): Result<RadioStation?>
}
