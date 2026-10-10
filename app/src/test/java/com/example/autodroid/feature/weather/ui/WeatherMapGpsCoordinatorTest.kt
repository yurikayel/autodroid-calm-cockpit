package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.data.preferences.FirstRunThemeSettler
import com.example.autodroid.feature.weather.domain.location.DeviceLocationProvider
import com.example.autodroid.feature.weather.domain.model.AppError
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.util.ConnectivityStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMapGpsCoordinatorTest {

    private val repository = mockk<WeatherRepository>()
    private val deviceLocationProvider = mockk<DeviceLocationProvider>()
    private val firstRunThemeSettler = mockk<FirstRunThemeSettler>(relaxed = true)
    
    private val coordinator = WeatherMapGpsCoordinator(repository, deviceLocationProvider, firstRunThemeSettler)

    @Test
    fun `resolveDeviceLocation with null coords returns null and settles theme`() = runTest {
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns null
        
        val result = coordinator.resolveDeviceLocation(ConnectivityStatus.Available)
        
        assertNull(result)
        coVerify { firstRunThemeSettler.settle() }
    }

    @Test
    fun `resolveDeviceLocation offline returns null and settles theme`() = runTest {
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns Location(1, "x", 1.0, 1.0, "XX", null)
        
        val result = coordinator.resolveDeviceLocation(ConnectivityStatus.Lost)
        
        assertNull(result)
        coVerify { firstRunThemeSettler.settle() }
    }

    @Test
    fun `resolveDeviceLocation error returns null and settles theme`() = runTest {
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns Location(1, "x", 1.0, 1.0, "XX", null)
        coEvery { repository.reverseGeocode(1.0, 1.0) } returns Result.Error(AppError.NetworkError.ServerError(500))
        
        val result = coordinator.resolveDeviceLocation(ConnectivityStatus.Available)
        
        assertNull(result)
        coVerify { firstRunThemeSettler.settle() }
    }

    @Test
    fun `resolveDeviceLocation success returns location`() = runTest {
        val expected = Location(1, "London", 51.5, -0.1, "GB", null)
        coEvery { deviceLocationProvider.getLastKnownLocation() } returns Location(1, "x", 51.5, -0.1, "GB", null)
        coEvery { repository.reverseGeocode(51.5, -0.1) } returns Result.Success(expected)
        
        val result = coordinator.resolveDeviceLocation(ConnectivityStatus.Available)
        
        assertEquals(expected, result)
        coVerify { firstRunThemeSettler.settle() }
    }
}

