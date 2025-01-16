package com.malek.toiletesparis.data

import com.malek.toiletesparis.domain.PublicToiletRepository
import com.malek.toiletesparis.domain.Query
import com.malek.toiletesparis.domain.models.PublicToilet

class PublicToiletRepositoryImp : PublicToiletRepository {
    override suspend fun getPublicToilets(query: Query): Result<List<PublicToilet>> {
        TODO("Not yet implemented")
    }
}