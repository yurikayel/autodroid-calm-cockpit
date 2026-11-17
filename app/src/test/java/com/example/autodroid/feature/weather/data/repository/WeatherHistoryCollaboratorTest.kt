package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.data.local.entity.LocationEntity
import com.example.autodroid.feature.weather.domain.model.Location
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherHistoryCollaboratorTest {

    private lateinit var weatherDao: WeatherDao
    private lateinit var collaborator: WeatherHistoryCollaborator

    @Before
    fun setup() {
        weatherDao = mockk(relaxed = true)
        collaborator = WeatherHistoryCollaborator(weatherDao, java.time.Clock.systemUTC())
    }

    @Test
    fun `rekey - mergeViewedLocation prefers incoming positive ID over existing negative ID`() = runTest {
        val incoming = Location(id = 123L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null)
        val existingEntity = LocationEntity(id = -999L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null, countryCode = "GB", lastUpdated = 0L, lastViewedAt = 0L)
        
        coEvery { weatherDao.getLocation(incoming.id) } returns null
        coEvery { weatherDao.findLocationsNear(any(), any(), any()) } returns listOf(existingEntity)
        
        collaborator.markLocationViewed(incoming)
        
        coVerify(exactly = 1) { weatherDao.rekeyLocation(-999L, match { it.id == 123L }) }
    }

    @Test
    fun `evict - evictStaleLocationsIfNeeded removes unviewed then least recently viewed if over capacity`() = runTest {
        coEvery { weatherDao.getLocationCount() } returns 102
        coEvery { weatherDao.getUnviewedOldestIds(2) } returns listOf(1L)
        
        coEvery { weatherDao.getLocationCount() } returnsMany listOf(102, 101)
        coEvery { weatherDao.getLeastRecentlyViewedIds(1) } returns listOf(2L)
        
        collaborator.evictStaleLocationsIfNeeded()
        
        coVerify(exactly = 1) { weatherDao.deleteLocationWithForecasts(1L) }
        coVerify(exactly = 1) { weatherDao.deleteLocationWithForecasts(2L) }
    }

    @Test
    fun `backfill - backfillCountryCode updates DAO if existing code is blank`() = runTest {
        collaborator.backfillCountryCode(10L, " GB ", null)
        coVerify(exactly = 1) { weatherDao.updateCountryCode(10L, "GB") }
    }

    @Test
    fun `nome+pais - findDuplicateLocation matches by name and country if proximity fails`() = runTest {
        val location = Location(id = 1L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null)
        coEvery { weatherDao.findLocationsNear(any(), any(), any()) } returns emptyList()
        coEvery { weatherDao.findLocationsByNameAndCountry("London", "GB") } returns listOf(
            LocationEntity(id = 2L, name = "London", country = "GB", admin1 = null, latitude = 51.5, longitude = -0.1, lastUpdated = 0L, lastViewedAt = 0L)
        )
        
        val result = collaborator.findDuplicateLocation(location)
        assertEquals(2L, result?.id)
    }
}
