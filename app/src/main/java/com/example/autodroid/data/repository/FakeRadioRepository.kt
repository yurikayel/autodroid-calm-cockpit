package com.example.autodroid.data.repository

import com.example.autodroid.core.demo.DemoScenario
import com.example.autodroid.core.demo.DemoScenarioStore
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.repository.RadioRepository
import kotlinx.coroutines.delay
import java.io.IOException
import javax.inject.Inject

class FakeRadioRepository @Inject constructor(
    private val demoScenarioStore: DemoScenarioStore
) : RadioRepository {

    override suspend fun getStations(): Result<List<RadioStation>> {
        delay(NETWORK_DELAY_MS)
        return when (demoScenarioStore.scenario.value) {
            DemoScenario.NORMAL -> Result.success(STATIONS)
            DemoScenario.EMPTY -> Result.success(emptyList())
            DemoScenario.ERROR -> Result.failure(IOException("Could not reach the station directory"))
        }
    }

    override suspend fun getStation(id: String): Result<RadioStation?> {
        delay(NETWORK_DELAY_MS / 2)
        return when (demoScenarioStore.scenario.value) {
            DemoScenario.ERROR -> Result.failure(IOException("Could not reach the station directory"))
            else -> Result.success(STATIONS.firstOrNull { it.id == id })
        }
    }

    companion object {
        const val NETWORK_DELAY_MS = 800L

        val STATIONS = listOf(
            RadioStation("1", "Classic Rock FM", "99.5 MHz", "Rock"),
            RadioStation("2", "Jazz Grooves", "101.1 MHz", "Jazz"),
            RadioStation("3", "News Radio", "88.0 MHz", "News"),
            RadioStation("4", "Pop Hits", "104.3 MHz", "Pop"),
            RadioStation("5", "Nordic Classical", "92.4 MHz", "Classical"),
            RadioStation("6", "Traffic Info", "87.6 MHz", "Traffic"),
            RadioStation("7", "Electro Drive", "106.7 MHz", "Electronic"),
            RadioStation("8", "Country Roads", "95.9 MHz", "Country")
        )
    }
}
