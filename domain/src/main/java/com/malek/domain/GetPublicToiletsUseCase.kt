package com.malek.domain

import com.malek.domain.models.PublicToiletListPageResult
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