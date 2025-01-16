package com.malek.toiletesparis.data.api.dto

import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.Service

fun RecordDto.toPublicToilet(): PublicToilet? {
    return if (this.recordId == null || this.fields?.adresse == null) {
        null
    } else {
        val services = mutableListOf<Service>()
        if (this.fields.prmAccess == Yes) {
            services.add(Service.PRM_ACCESS)
        }
        if (this.fields.babyRely == Yes) {
            services.add(Service.BABY_RELY)
        }
        PublicToilet(
            id = this.recordId,
            address = "${this.fields.adresse}, ${this.fields.arrondissement} ",
            hours = this.fields.horaire,
            latLong = this.fields.geoPoint2d?.takeIf { it.size == 2 }?.let {
                it[0] to it[1]
            },
            servicesAvailable = services.toList(),
            equipmentInfoUrl = this.fields.equipmentInfoUrl
        )

    }
}