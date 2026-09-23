package com.example.autodroid.feature.radio

import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.demo.DemoScenario
import com.example.autodroid.core.demo.DemoScenarioStore
import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.usecase.GetRadioStationsUseCase
import com.example.autodroid.domain.usecase.LimitBrowsingWhileDrivingUseCase
import com.example.autodroid.testing.MainDispatcherRule
import com.example.autodroid.testing.TestTelemetryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RadioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scenarioStore = DemoScenarioStore()
    private val telemetry = TestTelemetryRepository()

    private fun TestScope.createViewModel(): RadioViewModel {
        val viewModel = RadioViewModel(
            getRadioStations = GetRadioStationsUseCase(FakeRadioRepository(scenarioStore)),
            limitBrowsingWhileDriving = LimitBrowsingWhileDrivingUseCase(),
            demoScenarioStore = scenarioStore,
            drivingStateManager = DrivingStateManager(telemetry, backgroundScope)
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `starts in loading`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        assertEquals(RadioUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `parked shows every station sorted by name`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as RadioUiState.Success
        assertEquals(FakeRadioRepository.STATIONS.size, state.stations.size)
        assertEquals(FakeRadioRepository.STATIONS.map { it.name }.sorted(), state.stations.map { it.name })
        assertEquals(0, state.hiddenCount)
    }

    @Test
    fun `driving limits the list and recovers when parked`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        telemetry.emit(VehicleState(speedKmh = 50f))
        val driving = viewModel.uiState.value as RadioUiState.Success
        assertEquals(LimitBrowsingWhileDrivingUseCase.MAX_ITEMS_WHILE_DRIVING, driving.stations.size)
        assertTrue(driving.hiddenCount > 0)

        telemetry.emit(VehicleState(speedKmh = 0f))
        val parked = viewModel.uiState.value as RadioUiState.Success
        assertEquals(0, parked.hiddenCount)
    }

    @Test
    fun `empty directory shows empty state`() = runTest(mainDispatcherRule.dispatcher) {
        scenarioStore.select(DemoScenario.EMPTY)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RadioUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `failure shows error and retry recovers`() = runTest(mainDispatcherRule.dispatcher) {
        scenarioStore.select(DemoScenario.ERROR)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is RadioUiState.Error)

        viewModel.selectDemoScenario(DemoScenario.NORMAL)
        assertEquals(RadioUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RadioUiState.Success)
    }
}
