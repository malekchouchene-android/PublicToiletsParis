package com.malek.utlis

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackgroundDispatcher


@Module
@InstallIn(SingletonComponent::class)
class CoroutineDispatcherModule {

    @BackgroundDispatcher
    @Provides
    fun provideBackgroundDispatcher(): CoroutineDispatcher = Dispatchers.IO
}