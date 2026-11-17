package com.example.autodroid.feature.weather.ui

/** Result of capturing and sharing the weather card image. */
internal data class ShareWeatherOutcome(
    val shared: Boolean,
    /** Whether the same PNG was written to Downloads. Independent of [shared]. */
    val savedToDownloads: Boolean = false
)

