package com.malek.toiletesparis.domain

import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.Service

interface PublicToiletRepository {
    suspend fun getPublicToilets(
        query: Query
    ): Result<List<PublicToilet>>
}


data class Query(
    val latLong: Pair<Double, Double>?,
    val distance: Int?,
    val startOffset: Int = 0,
    val listOfService: List<Service> = emptyList()
)