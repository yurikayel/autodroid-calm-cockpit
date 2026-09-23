package com.example.autodroid.domain.usecase

import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetTelemetryUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    operator fun invoke(): StateFlow<VehicleState> = repository.vehicleState
}
