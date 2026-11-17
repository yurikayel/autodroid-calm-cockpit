package com.example.autodroid.feature.weather.ui

import com.example.autodroid.feature.weather.domain.model.Location
import com.example.autodroid.feature.weather.domain.repository.WeatherRepository
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class WeatherCountryWarmer(
    private val repository: WeatherRepository,
    private val countryCityCatalog: CountryCityCatalog,
    private val coroutineScope: CoroutineScope
) {
    private var countryWarmCode: String? = null
    private val countryWarmBudget = AtomicInteger(0)
    private var countryWarmJob: Job? = null

    fun addCountryWarmBudget(countryCode: String, extra: Int) {
        val code = countryCode.trim().uppercase()
        if (code.isEmpty() || extra <= 0) return
        if (countryWarmCode != code) {
            countryWarmCode = code
            countryWarmBudget.set(0)
            countryWarmJob?.cancel()
            countryWarmJob = null
        }
        countryWarmBudget.addAndGet(extra)
        if (countryWarmJob?.isActive == true) return
        countryWarmJob = coroutineScope.launch { drainCountryWarm(code) }
    }

    private suspend fun drainCountryWarm(code: String) {
        while (drainCountryWarmSlice(code)) {
            // Leftover budget from a concurrent selection nudge.
        }
    }

    private suspend fun drainCountryWarmSlice(code: String): Boolean {
        val limit = countryWarmBudget.getAndSet(0)
        if (limit <= 0) return false
        val result = try {
            repository.prefetchCountryCities(code, limit)
        } catch (e: CancellationException) {
            countryWarmBudget.addAndGet(limit)
            throw e
        }
        val catalogDone = result.remaining <= 0
        val concurrent = countryWarmBudget.get()
        if (!catalogDone) {
            val unused = (limit - result.warmed).coerceAtLeast(0)
            if (unused > 0) countryWarmBudget.addAndGet(unused)
        }
        return !catalogDone && result.warmed > 0 && concurrent > 0
    }

    private fun countryIsoFor(location: Location): String? {
        val stored = location.countryCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        return stored ?: countryCityCatalog.isoForCountryName(location.country)
    }
}


