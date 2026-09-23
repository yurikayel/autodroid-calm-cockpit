package com.example.autodroid.core.media

import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NowPlayingStore @Inject constructor() {
    private val stations = FakeRadioRepository.STATIONS
    private val _station = MutableStateFlow(stations.first())
    val station: StateFlow<RadioStation> = _station.asStateFlow()

    fun select(station: RadioStation) {
        _station.value = station
    }

    fun selectById(id: String) {
        stations.firstOrNull { it.id == id }?.let { _station.value = it }
    }

    fun previous() {
        val index = stations.indexOfFirst { it.id == _station.value.id }.coerceAtLeast(0)
        _station.value = stations[(index - 1 + stations.size) % stations.size]
    }

    fun next() {
        val index = stations.indexOfFirst { it.id == _station.value.id }.coerceAtLeast(0)
        _station.value = stations[(index + 1) % stations.size]
    }
}
