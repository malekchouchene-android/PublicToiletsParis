package com.malek.toiletesparis.domain

import com.malek.toiletesparis.domain.models.PublicToiletListPageResult
import javax.inject.Inject


class GetPublicToiletsUseCase @Inject constructor(
    private val publicToiletRepository: PublicToiletRepository
) {
    suspend fun getPublicToiletsByQuery(
        query: Query
    ): Result<PublicToiletListPageResult> {
        return publicToiletRepository.getPublicToilets(query)
    }
}