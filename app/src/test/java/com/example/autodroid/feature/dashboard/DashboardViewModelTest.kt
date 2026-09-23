package com.example.autodroid.feature.dashboard

import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.automotive.SettingsManager
import com.example.autodroid.core.designsystem.Brand
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.core.voice.VoiceAssistantManager
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.usecase.GetTelemetryUseCase
import com.example.autodroid.testing.MainDispatcherRule
import com.example.autodroid.testing.TestTelemetryRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val telemetry = TestTelemetryRepository()
    private val settingsManager = SettingsManager()
    private val voiceAssistantManager: VoiceAssistantManager = mockk(relaxed = true)
    private val nowPlayingStore = NowPlayingStore()

    private fun TestScope.createViewModel() = DashboardViewModel(
        getTelemetryUseCase = GetTelemetryUseCase(telemetry),
        drivingStateManager = DrivingStateManager(telemetry, backgroundScope),
        settingsManager = settingsManager,
        voiceAssistantManager = voiceAssistantManager,
        nowPlayingStore = nowPlayingStore
    )

    @Test
    fun `exposes latest telemetry`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        telemetry.emit(VehicleState(speedKmh = 42f, gear = 2))

        assertEquals(42f, viewModel.vehicleState.value.speedKmh)
    }

    @Test
    fun `is driving only above the speed threshold`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        telemetry.emit(VehicleState(speedKmh = DrivingStateManager.DRIVING_SPEED_THRESHOLD_KMH))
        assertFalse(viewModel.isDriving.value)

        telemetry.emit(VehicleState(speedKmh = 30f))
        assertTrue(viewModel.isDriving.value)
    }

    @Test
    fun `fuel command speaks the current fuel level`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        telemetry.emit(VehicleState(fuelLevelPercent = 75.6f))

        viewModel.handleCommand("How is the fuel?")

        verify { voiceAssistantManager.speak("Your fuel level is at 75 percent.") }
    }

    @Test
    fun `unknown command asks the driver to repeat`() = runTest(mainDispatcherRule.dispatcher) {
        createViewModel().handleCommand("Sing a song")

        verify { voiceAssistantManager.speak("I didn't understand.") }
    }

    @Test
    fun `cycle brand rotates through all brands and wraps around`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.cycleBrand()
        assertEquals(Brand.SUMMIT, settingsManager.currentBrand.value)

        repeat(Brand.entries.size - 1) { viewModel.cycleBrand() }
        assertEquals(Brand.NORTHWIND, settingsManager.currentBrand.value)
    }
}
