package com.example.autodroid.feature.radio

import com.example.autodroid.core.demo.DemoScenario
import com.example.autodroid.core.demo.DemoScenarioStore
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.usecase.GetStationByIdUseCase
import com.example.autodroid.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RadioDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scenarioStore = DemoScenarioStore()
    private val nowPlayingStore = NowPlayingStore()

    private fun createViewModel(id: String) =
        RadioDetailViewModel(id, GetStationByIdUseCase(FakeRadioRepository(scenarioStore)), nowPlayingStore)

    @Test
    fun `known id shows the station`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel("3")
        advanceUntilIdle()

        val state = viewModel.uiState.value as RadioDetailUiState.Success
        assertEquals("News Radio", state.station.name)
    }

    @Test
    fun `unknown id shows not found`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel("999")
        advanceUntilIdle()

        assertEquals(RadioDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `failure shows error`() = runTest(mainDispatcherRule.dispatcher) {
        scenarioStore.select(DemoScenario.ERROR)
        val viewModel = createViewModel("1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RadioDetailUiState.Error)
    }
}
