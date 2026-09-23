package com.example.autodroid.data.repository

import com.example.autodroid.di.ApplicationScope
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.repository.TelemetryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simulates a drive cycle (park, accelerate, cruise, brake) so UX restrictions
 * visibly switch on and off during a demo on a standard emulator.
 */
@Singleton
class FakeTelemetryRepository @Inject constructor(
    @ApplicationScope scope: CoroutineScope
) : TelemetryRepository {

    override val vehicleState: StateFlow<VehicleState> = flow {
        var fuel = 85f
        while (true) {
            for (tick in 0 until TICKS_PER_CYCLE) {
                val speed = speedAt(tick)
                fuel = (fuel - if (speed > 0f) 0.01f else 0f).coerceAtLeast(5f)
                emit(
                    VehicleState(
                        speedKmh = speed,
                        rpm = if (speed == 0f) IDLE_RPM else IDLE_RPM + (speed % 30f) * 60f,
                        fuelLevelPercent = fuel,
                        gear = if (speed == 0f) 0 else (speed / 30f).toInt() + 1
                    )
                )
                delay(TICK_MS)
            }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), VehicleState())

    private fun speedAt(tick: Int): Float = when {
        tick < PARK_TICKS -> 0f
        tick < PARK_TICKS + RAMP_TICKS -> (tick - PARK_TICKS) * STEP
        tick < PARK_TICKS + RAMP_TICKS + CRUISE_TICKS -> CRUISE_KMH
        else -> (CRUISE_KMH - (tick - PARK_TICKS - RAMP_TICKS - CRUISE_TICKS) * STEP).coerceAtLeast(0f)
    }

    private companion object {
        const val TICK_MS = 100L
        const val PARK_TICKS = 80
        const val RAMP_TICKS = 50
        const val CRUISE_TICKS = 60
        const val TICKS_PER_CYCLE = PARK_TICKS + RAMP_TICKS * 2 + CRUISE_TICKS
        const val CRUISE_KMH = 80f
        const val STEP = CRUISE_KMH / RAMP_TICKS
        const val IDLE_RPM = 800f
    }
}
