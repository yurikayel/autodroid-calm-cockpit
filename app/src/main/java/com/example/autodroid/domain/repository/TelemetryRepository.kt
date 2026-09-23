package com.example.autodroid.domain.repository

import com.example.autodroid.domain.model.VehicleState
import kotlinx.coroutines.flow.StateFlow

interface TelemetryRepository {
    /** Hot stream shared by every consumer, like CarPropertyManager callbacks on a real head unit. */
    val vehicleState: StateFlow<VehicleState>
}
