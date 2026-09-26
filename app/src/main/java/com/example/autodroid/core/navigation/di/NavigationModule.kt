package com.example.autodroid.core.navigation.di

import com.example.autodroid.core.navigation.data.remote.OsrmApi
import com.example.autodroid.core.navigation.data.repository.NavigationRepositoryImpl
import com.example.autodroid.core.navigation.domain.repository.NavigationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    @Provides
    @Singleton
    fun provideOsrmApi(): OsrmApi {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OsrmApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNavigationRepository(
        api: OsrmApi
    ): NavigationRepository {
        return NavigationRepositoryImpl(api)
    }
}
