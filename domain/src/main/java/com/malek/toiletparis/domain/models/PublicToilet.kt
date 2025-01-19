package com.malek.toiletparis.domain.models

data class PublicToilet(
    val id: String,
    val address: String,
    val latLong: Pair<Double, Double>?,
    val hours: String?,
    val servicesAvailable: List<Service>,
    val equipmentInfoUrl: String?
)

data class PublicToiletListPageResult(
    val totalNumber: Int,
    val pageSize: Int,
    val result: List<PublicToilet>,
)

enum class Service {
    PRM_ACCESS, BABY_RELY
}