package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherCountryWarmerTest {

    private val repository = mockk<WeatherRepository>(relaxed = true)
    private val catalog = mockk<CountryCityCatalog>(relaxed = true)
    
    @Test
    fun `addCountryWarmBudget triggers prefetch`() = runTest {
        val warmer = WeatherCountryWarmer(repository, catalog, backgroundScope)
        
        warmer.addCountryWarmBudget("GB", 2)
        
        advanceTimeBy(1000)
        io.mockk.coVerify(exactly = 1) { repository.prefetchCountryCities("GB", 2) }
    }
}

