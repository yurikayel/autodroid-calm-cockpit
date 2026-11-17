package com.example.autodroid.feature.weather.di

import com.example.autodroid.feature.weather.data.catalog.CountryCityCatalogLoader
import com.example.autodroid.feature.weather.domain.usecase.CountryCityCatalog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the bundled country-city catalog used for GPS country-warm prefetch.
 */
@Module
@InstallIn(SingletonComponent::class)
object CatalogModule {

    /** Parses assets once and exposes a pure in-memory [CountryCityCatalog]. */
    @Provides
    @Singleton
    fun provideCountryCityCatalog(loader: CountryCityCatalogLoader): CountryCityCatalog {
        return CountryCityCatalog(loader.load())
    }
}

