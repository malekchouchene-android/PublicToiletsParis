package com.malek.toiletesparis.data.di

import com.malek.toiletesparis.data.PublicToiletRepositoryImp
import com.malek.toiletesparis.domain.PublicToiletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepoModule() {
    @Binds
    abstract fun bindPublicToiletRepository(
        imp: PublicToiletRepositoryImp
    ): PublicToiletRepository
}