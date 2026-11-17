package com.example.autodroid.feature.weather.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {

    private val now = 10_000_000_000L

    @Test
    fun `isWeatherFresh is true within TTL when forecasts exist`() {
        val lastUpdated = now - CachePolicy.WEATHER_TTL_MS + 1_000L
        assertTrue(CachePolicy.isWeatherFresh(lastUpdated, hasForecasts = true, now = now))
    }

    @Test
    fun `isWeatherFresh is false once TTL has elapsed`() {
        val lastUpdated = now - CachePolicy.WEATHER_TTL_MS
        assertFalse(CachePolicy.isWeatherFresh(lastUpdated, hasForecasts = true, now = now))
    }

    @Test
    fun `isWeatherFresh is false without stored forecasts`() {
        assertFalse(CachePolicy.isWeatherFresh(now, hasForecasts = false, now = now))
    }

    @Test
    fun `shouldFetchPlaceImage fetches when metadata never confirmed`() {
        assertTrue(CachePolicy.shouldFetchPlaceImage("https://img", metadataAt = 0L, now = now))
    }

    @Test
    fun `shouldFetchPlaceImage skips fresh hit and refetches stale hit`() {
        val fresh = now - CachePolicy.PLACE_METADATA_TTL_MS + 1L
        val stale = now - CachePolicy.PLACE_METADATA_TTL_MS
        assertFalse(CachePolicy.shouldFetchPlaceImage("https://img", fresh, now))
        assertTrue(CachePolicy.shouldFetchPlaceImage("https://img", stale, now))
    }

    @Test
    fun `shouldFetchPlaceImage retries a confirmed miss after miss TTL`() {
        val recentMiss = now - CachePolicy.PLACE_METADATA_MISS_TTL_MS + 1L
        val oldMiss = now - CachePolicy.PLACE_METADATA_MISS_TTL_MS
        assertFalse(CachePolicy.shouldFetchPlaceImage(null, recentMiss, now))
        assertTrue(CachePolicy.shouldFetchPlaceImage(null, oldMiss, now))
    }
}
