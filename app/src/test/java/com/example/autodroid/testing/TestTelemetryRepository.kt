package com.example.autodroid.testing

import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow

class TestTelemetryRepository(initial: VehicleState = VehicleState()) : TelemetryRepository {
    override val vehicleState = MutableStateFlow(initial)

    fun emit(state: VehicleState) {
        vehicleState.value = state
    }
}
