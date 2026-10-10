package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.local.dao.WeatherDao
import com.example.autodroid.feature.weather.data.local.entity.LocationEntity
import com.example.autodroid.feature.weather.domain.model.Location
import io.mockk.coEvery
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
        weatherDao = mockk()
        collaborator = WeatherHistoryCollaborator(weatherDao, java.time.Clock.systemUTC())
    }

    @Test
    fun `resolveCanonicalLocation prefers incoming positive ID over existing negative Nominatim ID`() = runTest {
        val incoming = Location(id = 123L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null)
        val existingEntity = LocationEntity(id = -999L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null, countryCode = "GB", lastUpdated = 0L, lastViewedAt = 0L)
        
        coEvery { weatherDao.getLocation(incoming.id) } returns null
        coEvery { weatherDao.findLocationsNear(any(), any(), any()) } returns listOf(existingEntity)
        
        val resolved = collaborator.resolveCanonicalLocation(incoming)
        
        // Should prefer incoming (123L) because it is > 0 and existing is < 0
        assertEquals(123L, resolved.id)
    }

    @Test
    fun `resolveCanonicalLocation uses existing positive ID over incoming Nominatim negative ID`() = runTest {
        val incoming = Location(id = -888L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null)
        val existingEntity = LocationEntity(id = 123L, name = "London", latitude = 51.5, longitude = -0.1, country = "GB", admin1 = null, countryCode = "GB", lastUpdated = 0L, lastViewedAt = 0L)
        
        coEvery { weatherDao.getLocation(incoming.id) } returns null
        coEvery { weatherDao.findLocationsNear(any(), any(), any()) } returns listOf(existingEntity)
        
        val resolved = collaborator.resolveCanonicalLocation(incoming)
        
        // Should prefer existing (123L) because incoming is negative
        assertEquals(123L, resolved.id)
    }

    @Test
    fun `observeRecentLocations collapses duplicates based on HistoryDeduper`() = runTest {
        val entity1 = LocationEntity(id = 1L, name = "Paris", country = "FR", admin1 = null, latitude = 48.8, longitude = 2.3, lastUpdated = 0L, lastViewedAt = 100L)
        val entity2 = LocationEntity(id = -2L, name = "Paris", country = "FR", admin1 = null, latitude = 48.801, longitude = 2.301, lastUpdated = 0L, lastViewedAt = 200L)
        val entity3 = LocationEntity(id = 3L, name = "Lyon", country = "FR", admin1 = null, latitude = 45.7, longitude = 4.8, lastUpdated = 0L, lastViewedAt = 50L)

        // The DAO returns 3 entities, but Paris should be collapsed into one (the most recently viewed).
        every { weatherDao.getRecentLocationsFlow(any()) } returns flowOf(listOf(entity2, entity1, entity3))
        
        val recent = collaborator.observeRecentLocations(2).first()
        
        assertEquals(2, recent.size)
        assertEquals(-2L, recent[0].id)
        assertEquals("Paris", recent[0].name)
        assertEquals(3L, recent[1].id)
        assertEquals("Lyon", recent[1].name)
    }
}

