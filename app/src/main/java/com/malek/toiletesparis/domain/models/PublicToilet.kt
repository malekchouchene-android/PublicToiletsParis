package com.malek.toiletesparis.domain.models

data class PublicToilet(
    val address: String,
    val latLong: Pair<Double, Double>?,
    val hours: String?,
    val servicesAvailable: List<Service>,
    val equipmentInfoUrl: String?
)

enum class Service {
    BABY_RELY, PRM_ACCESS
}