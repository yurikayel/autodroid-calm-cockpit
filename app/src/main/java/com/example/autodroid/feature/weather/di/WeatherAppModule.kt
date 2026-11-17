package com.example.autodroid.feature.weather.di

import android.content.Context
import com.example.autodroid.feature.weather.data.location.AndroidDeviceLocationProvider
import com.example.autodroid.feature.weather.data.util.LogCrashReporter
import com.example.autodroid.feature.weather.data.util.NetworkConnectivityObserver
import com.example.autodroid.feature.weather.domain.location.DeviceLocationProvider
import com.example.autodroid.feature.weather.domain.util.ConnectivityObserver
import com.example.autodroid.feature.weather.domain.util.CrashReporter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherAppModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: LogCrashReporter): CrashReporter

    @Binds
    @Singleton
    abstract fun bindDeviceLocationProvider(impl: AndroidDeviceLocationProvider): DeviceLocationProvider

    companion object {
        @Provides
        @Singleton
        fun provideClock(): java.time.Clock {
            return java.time.Clock.systemDefaultZone()
        }

        @Provides
        @Singleton
        fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
            return NetworkConnectivityObserver(context)
        }
    }
}
