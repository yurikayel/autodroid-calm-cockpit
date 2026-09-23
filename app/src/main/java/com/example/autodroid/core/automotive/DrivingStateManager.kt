package com.example.autodroid.core.automotive

import com.example.autodroid.di.ApplicationScope
import com.example.autodroid.domain.repository.TelemetryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stand-in for CarUxRestrictionsManager: on a real head unit the platform decides when
 * the driving state is "moving" and which restrictions apply. Here it is derived from speed.
 */
@Singleton
class DrivingStateManager @Inject constructor(
    telemetryRepository: TelemetryRepository,
    @ApplicationScope scope: CoroutineScope
) {
    val isDriving: StateFlow<Boolean> = telemetryRepository.vehicleState
        .map { it.speedKmh > DRIVING_SPEED_THRESHOLD_KMH }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, false)

    companion object {
        const val DRIVING_SPEED_THRESHOLD_KMH = 5f
    }
}
