package com.malek.toiletesparis.data.di

import com.malek.toiletesparis.data.api.ParisDataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://data.ratp.fr/api/records/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideParisDataApi(
        retrofit: Retrofit
    ): ParisDataApi {
        return retrofit.create(ParisDataApi::class.java)
    }
}