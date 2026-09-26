package com.example.autodroid.feature.weather.data.util

import android.util.Log
import com.example.autodroid.feature.weather.domain.util.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AutoDroidWeather"

/**
 * Logs exceptions locally. Replace with Crashlytics in production builds when configured.
 */
@Singleton
class LogCrashReporter @Inject constructor() : CrashReporter {
    /** Writes [throwable] to logcat, prefixing [message] when the caller supplied one. */
    override fun recordException(throwable: Throwable, message: String?) {
        if (message != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, "Unhandled exception", throwable)
        }
    }
}

