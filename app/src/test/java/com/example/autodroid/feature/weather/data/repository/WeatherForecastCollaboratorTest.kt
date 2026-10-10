package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.data.local.entity.DailyForecastEntity
import com.example.autodroid.feature.weather.data.local.entity.LocationEntity
import com.example.autodroid.feature.weather.domain.model.CachePolicy
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherForecastCollaboratorTest {

    private lateinit var collaborator: WeatherForecastCollaborator

    @Before
    fun setup() {
        collaborator = WeatherForecastCollaborator(mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
    }

    @Test
    fun `isWeatherFresh returns true when under TTL and has days`() {
        val now = 1000000L
        val lastUpdated = now - CachePolicy.WEATHER_TTL_MS + 1000 // 1 second before TTL expires
        val entity = LocationEntity(id = 1L, name = "Test", latitude = 0.0, longitude = 0.0, country = "", admin1 = null, lastUpdated = lastUpdated, lastViewedAt = 0L)
        val days = listOf(DailyForecastEntity(locationId = 1L, date = "2023-01-01", maxTemp = 0.0, minTemp = 0.0, weatherCode = 0, precipitationSum = 0.0, maxWindSpeed = 0.0, snowfallSum = 0.0))
        
        assertTrue(collaborator.isWeatherFresh(entity, days, now))
    }

    @Test
    fun `isWeatherFresh returns false when TTL expired`() {
        val now = 1000000L
        val lastUpdated = now - CachePolicy.WEATHER_TTL_MS - 1000 // 1 second after TTL expires
        val entity = LocationEntity(id = 1L, name = "Test", latitude = 0.0, longitude = 0.0, country = "", admin1 = null, lastUpdated = lastUpdated, lastViewedAt = 0L)
        val days = listOf(DailyForecastEntity(locationId = 1L, date = "2023-01-01", maxTemp = 0.0, minTemp = 0.0, weatherCode = 0, precipitationSum = 0.0, maxWindSpeed = 0.0, snowfallSum = 0.0))
        
        assertFalse(collaborator.isWeatherFresh(entity, days, now))
    }

    @Test
    fun `isWeatherFresh returns false when no days exist`() {
        val now = 1000000L
        val entity = LocationEntity(id = 1L, name = "Test", latitude = 0.0, longitude = 0.0, country = "", admin1 = null, lastUpdated = now, lastViewedAt = 0L)
        
        assertFalse(collaborator.isWeatherFresh(entity, emptyList(), now))
    }
}


