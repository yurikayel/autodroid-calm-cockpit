package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.AppError
import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.model.Result
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.util.ConnectivityStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherSearchCoordinatorTest {

    private val repository = mockk<WeatherRepository>()
    private val coordinator = WeatherSearchCoordinator(repository)

    @Test
    fun `query less than or equal to 2 chars is filtered out`() = runTest {
        val connectivity = MutableStateFlow(ConnectivityStatus.Available)
        val queries = flowOf("L", "Lo")
        
        val results = mutableListOf<SearchOutcome>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeSearch(queries, connectivity, MutableStateFlow(emptyList())).toList(results)
        }
        
        advanceTimeBy(1000)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `offline emits network error`() = runTest {
        val connectivity = MutableStateFlow(ConnectivityStatus.Lost)
        val queries = flowOf("London")
        
        val results = mutableListOf<SearchOutcome>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeSearch(queries, connectivity, MutableStateFlow(emptyList())).toList(results)
        }
        
        advanceTimeBy(1000)
        assertEquals(2, results.size)
        assertTrue(results[0].searchState is SearchUiState.Loading)
        assertTrue(results[1].searchState is SearchUiState.Failed)
    }

    @Test
    fun `search failure emits failed outcome`() = runTest {
        coEvery { repository.searchCity(any()) } returns Result.Error(AppError.NetworkError.ServerError(500))
        val connectivity = MutableStateFlow(ConnectivityStatus.Available)
        val queries = flowOf("London")
        
        val results = mutableListOf<SearchOutcome>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeSearch(queries, connectivity, MutableStateFlow(emptyList())).toList(results)
        }
        
        advanceTimeBy(1000)
        assertEquals(2, results.size)
        assertTrue(results[1].searchState is SearchUiState.Failed)
    }

    @Test
    fun `subsequent query cancels ongoing search`() = runTest {
        coEvery { repository.searchCity("London") } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.Success(listOf(Location(1, "London", 51.5, -0.1, "GB", null)))
        }
        coEvery { repository.searchCity("Paris") } returns Result.Success(listOf(Location(2, "Paris", 48.8, 2.3, "FR", null)))
        
        val connectivity = MutableStateFlow(ConnectivityStatus.Available)
        val queries = MutableStateFlow("London")
        
        val results = mutableListOf<SearchOutcome>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.observeSearch(queries, connectivity, MutableStateFlow(emptyList())).toList(results)
        }
        
        advanceTimeBy(501) // Debounce passes, London search starts
        queries.value = "Paris" // Emit Paris while London is searching
        advanceTimeBy(501) // Debounce passes, Paris search starts
        advanceTimeBy(1000) // Paris search completes
        
        val finalResult = results.last()
        advanceTimeBy(2000)
        assertTrue(results.none { it.searchState is SearchUiState.Results && (it.searchState as SearchUiState.Results).locations.any { loc -> loc.name == "London" } })
        assertTrue(finalResult.searchState is SearchUiState.Results)
        assertEquals("Paris", (finalResult.searchState as SearchUiState.Results).locations.first().name)
    }
}


