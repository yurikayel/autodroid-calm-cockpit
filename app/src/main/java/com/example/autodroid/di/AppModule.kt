package com.example.autodroid.di

import com.example.autodroid.data.repository.FakeTelemetryRepository
import com.example.autodroid.domain.repository.TelemetryRepository
import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.repository.RadioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(
        fakeTelemetryRepository: FakeTelemetryRepository
    ): TelemetryRepository

    @Binds
    @Singleton
    abstract fun bindRadioRepository(
        fakeRadioRepository: FakeRadioRepository
    ): RadioRepository
}
