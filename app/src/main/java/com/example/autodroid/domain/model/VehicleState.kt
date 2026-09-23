package com.example.autodroid.domain.model

data class VehicleState(
    val speedKmh: Float = 0f,
    val rpm: Float = 0f,
    val fuelLevelPercent: Float = 100f,
    val gear: Int = 0 // 0 = Neutral, 1+ = Drive, -1 = Reverse
)
