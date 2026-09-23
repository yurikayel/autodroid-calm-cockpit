package com.example.autodroid.domain.usecase

import com.example.autodroid.domain.model.RadioStation
import javax.inject.Inject

data class RadioBrowsing(
    val stations: List<RadioStation>,
    val hiddenCount: Int
) {
    val isRestricted: Boolean get() = hiddenCount > 0
}

/**
 * Mirrors UX_RESTRICTIONS_LIMIT_CONTENT: while moving, long lists are capped
 * (CarUxRestrictions#getMaxCumulativeContentItems on a real head unit).
 */
class LimitBrowsingWhileDrivingUseCase @Inject constructor() {

    operator fun invoke(stations: List<RadioStation>, isDriving: Boolean): RadioBrowsing {
        if (!isDriving || stations.size <= MAX_ITEMS_WHILE_DRIVING) {
            return RadioBrowsing(stations, hiddenCount = 0)
        }
        return RadioBrowsing(
            stations = stations.take(MAX_ITEMS_WHILE_DRIVING),
            hiddenCount = stations.size - MAX_ITEMS_WHILE_DRIVING
        )
    }

    companion object {
        const val MAX_ITEMS_WHILE_DRIVING = 4
    }
}
