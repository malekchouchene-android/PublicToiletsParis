package com.malek.toiletesparis.domain

import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.PublicToiletListPageResult
import com.malek.toiletesparis.domain.models.Service

interface PublicToiletRepository {
    suspend fun getPublicToilets(
        query: Query
    ): Result<PublicToiletListPageResult>
}


data class Query(
    val latLong: Pair<Double, Double>? = null,
    val firstIndex: Int = 0,
    val distance: Int = 1000,
    val filterByPrmAccess: Boolean = false
)