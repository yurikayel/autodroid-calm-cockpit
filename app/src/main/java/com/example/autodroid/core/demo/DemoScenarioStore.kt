package com.example.autodroid.core.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class DemoScenario { NORMAL, EMPTY, ERROR }

/** Debug-only switch that lets a demo force the empty and error states on purpose. */
@Singleton
class DemoScenarioStore @Inject constructor() {
    private val _scenario = MutableStateFlow(DemoScenario.NORMAL)
    val scenario: StateFlow<DemoScenario> = _scenario.asStateFlow()

    fun select(scenario: DemoScenario) {
        _scenario.value = scenario
    }
}
