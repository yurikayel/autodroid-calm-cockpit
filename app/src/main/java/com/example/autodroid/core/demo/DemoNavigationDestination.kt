package com.example.autodroid.core.demo

/**
 * Default demo route for voice "weather on the way" / route weather (Seattle area → Portland).
 */
object DemoNavigationDestination {
    const val NAME = "Portland"
    const val LONGITUDE = -122.6784
    const val LATITUDE = 45.5152

    /** Fallback origin when device location is unavailable (Seattle). */
    const val FALLBACK_ORIGIN_LON = -122.3321
    const val FALLBACK_ORIGIN_LAT = 47.6062
}
