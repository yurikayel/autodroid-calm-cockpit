package com.example.autodroid.feature.weather.domain.util

fun String?.toIsoCountryCode(): String? {
    return this?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
}
