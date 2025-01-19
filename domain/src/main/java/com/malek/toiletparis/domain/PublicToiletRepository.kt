package com.malek.toiletparis.domain

import com.malek.toiletparis.domain.models.PublicToiletListPageResult
import com.malek.toiletparis.domain.models.Service

interface PublicToiletRepository {
    suspend fun getPublicToilets(
        query: Query
    ): Result<PublicToiletListPageResult>
}


data class Query(
    val latLong: Pair<Double, Double>? = null,
    val firstIndex: Int = 0,
    val distance: Int = 1000,
    val services: List<Service> = emptyList(),
)