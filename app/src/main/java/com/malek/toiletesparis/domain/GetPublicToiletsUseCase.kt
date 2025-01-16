package com.malek.toiletesparis.domain

import com.malek.toiletesparis.domain.models.PublicToilet
import javax.inject.Inject


class GetPublicToiletsUseCase @Inject constructor(
    private val publicToiletRepository: PublicToiletRepository
) {
    suspend fun getPublicToiletsByQuery(
        query: Query
    ): Result<List<PublicToilet>> {
        return publicToiletRepository.getPublicToilets(query)
    }
}