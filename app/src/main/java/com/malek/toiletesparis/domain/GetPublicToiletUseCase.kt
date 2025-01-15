package com.malek.toiletesparis.domain

import com.malek.toiletesparis.domain.models.PublicToilet


class GetPublicToiletUseCase(private val publicToiletRepository: PublicToiletRepository) {
    suspend fun getPublicToiletByQuery(
        query: Query
    ): Result<List<PublicToilet>> {
        return publicToiletRepository.getPublicToilets(query)
    }
}