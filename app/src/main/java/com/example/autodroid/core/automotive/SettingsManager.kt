package com.example.autodroid.core.automotive

import com.example.autodroid.core.designsystem.Brand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor() {
    private val _currentBrand = MutableStateFlow(Brand.NORTHWIND)
    val currentBrand: StateFlow<Brand> = _currentBrand.asStateFlow()

    fun cycleBrand() {
        _currentBrand.value = _currentBrand.value.next()
    }
}
