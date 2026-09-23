package com.example.autodroid.domain.usecase

import com.example.autodroid.domain.model.RadioStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LimitBrowsingWhileDrivingUseCaseTest {

    private val limit = LimitBrowsingWhileDrivingUseCase()
    private val max = LimitBrowsingWhileDrivingUseCase.MAX_ITEMS_WHILE_DRIVING

    private fun stations(count: Int) = List(count) { RadioStation("$it", "Station $it", "9$it.0 MHz", "Pop") }

    @Test
    fun `parked keeps the full list`() {
        val result = limit(stations(max + 3), isDriving = false)

        assertEquals(max + 3, result.stations.size)
        assertFalse(result.isRestricted)
    }

    @Test
    fun `driving caps a long list and reports what was hidden`() {
        val result = limit(stations(max + 3), isDriving = true)

        assertEquals(max, result.stations.size)
        assertEquals(3, result.hiddenCount)
        assertTrue(result.isRestricted)
    }

    @Test
    fun `driving with a short list is not restricted`() {
        val result = limit(stations(max), isDriving = true)

        assertEquals(max, result.stations.size)
        assertFalse(result.isRestricted)
    }
}
