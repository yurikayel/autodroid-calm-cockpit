package com.example.autodroid.feature.dashboard

import app.cash.turbine.test
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.automotive.SettingsManager
import com.example.autodroid.core.demo.DemoNavigationDestination
import com.example.autodroid.core.media.NowPlayingStore
import com.example.autodroid.core.navigation.NavCoordinate
import com.example.autodroid.core.navigation.NavigationManager
import com.example.autodroid.core.navigation.Route
import com.example.autodroid.core.navigation.domain.repository.NavigationRepository
import com.example.autodroid.core.designsystem.Brand
import com.example.autodroid.core.voice.VoiceAssistantManager
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.domain.usecase.GetTelemetryUseCase
import com.example.autodroid.feature.weather.domain.location.DeviceLocationProvider
import com.example.autodroid.testing.MainDispatcherRule
import com.example.autodroid.testing.TestTelemetryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val telemetry = TestTelemetryRepository()
    private val settingsManager = SettingsManager()
    private val voiceAssistantManager: VoiceAssistantManager = mockk(relaxed = true)
    private val nowPlayingStore = NowPlayingStore()
    private val navigationManager: NavigationManager = mockk(relaxed = true)
    private val navigationRepository: NavigationRepository = mockk()
    private val deviceLocationProvider: DeviceLocationProvider = mockk(relaxed = true)

    private fun TestScope.createViewModel() = DashboardViewModel(
        getTelemetryUseCase = GetTelemetryUseCase(telemetry),
        drivingStateManager = DrivingStateManager(telemetry, backgroundScope),
        settingsManager = settingsManager,
        voiceAssistantManager = voiceAssistantManager,
        nowPlayingStore = nowPlayingStore,
        navigationManager = navigationManager,
        navigationRepository = navigationRepository,
        deviceLocationProvider = deviceLocationProvider
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
    fun `weather command emits navigation event`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.navEvents.test {
            viewModel.handleCommand("what is the weather")
            assertEquals(DashboardNavEvent.Weather, awaitItem())
        }
    }

    @Test
    fun `route weather starts navigation when route resolves`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns null
        val route = Route(
            originName = "Current Location",
            destinationName = DemoNavigationDestination.NAME,
            polyline = listOf(NavCoordinate(47.6, -122.3)),
            estimatedDuration = 90.minutes
        )
        coEvery {
            navigationRepository.getRoute(
                originName = any(),
                originLon = DemoNavigationDestination.FALLBACK_ORIGIN_LON,
                originLat = DemoNavigationDestination.FALLBACK_ORIGIN_LAT,
                destName = DemoNavigationDestination.NAME,
                destLon = DemoNavigationDestination.LONGITUDE,
                destLat = DemoNavigationDestination.LATITUDE
            )
        } returns route

        val viewModel = createViewModel()
        viewModel.navEvents.test {
            viewModel.handleCommand("weather on the way")
            testScheduler.advanceUntilIdle()
            assertEquals(DashboardNavEvent.WeatherRoute, awaitItem())
        }

        verify { navigationManager.startNavigation(route) }
        verify { voiceAssistantManager.speak("Showing weather along your route.") }
    }

    @Test
    fun `route weather speaks failure when route is null`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns null
        coEvery {
            navigationRepository.getRoute(
                originName = any(),
                originLon = any(),
                originLat = any(),
                destName = any(),
                destLon = any(),
                destLat = any()
            )
        } returns null

        val viewModel = createViewModel()
        viewModel.handleCommand("weather on the way")
        testScheduler.advanceUntilIdle()

        coVerify { navigationRepository.getRoute(any(), any(), any(), any(), any(), any()) }
        verify { voiceAssistantManager.speak("Could not calculate route.") }
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
