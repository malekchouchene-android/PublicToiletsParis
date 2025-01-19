package com.malek.data.api.dto

import com.malek.domain.models.PublicToilet
import com.malek.domain.models.Service

fun RecordDto.toPublicToilet(): PublicToilet? {
    return if (this.recordId == null || this.fields?.adresse == null) {
        null
    } else {
        val services = mutableListOf<com.malek.domain.models.Service>()
        if (this.fields.prmAccess == Yes) {
            services.add(com.malek.domain.models.Service.PRM_ACCESS)
        }
        if (this.fields.babyRely == Yes) {
            services.add(com.malek.domain.models.Service.BABY_RELY)
        }
        com.malek.domain.models.PublicToilet(
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