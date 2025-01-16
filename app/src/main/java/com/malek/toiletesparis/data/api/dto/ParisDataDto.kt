package com.malek.toiletesparis.data.api.dto

import com.google.gson.annotations.SerializedName

data class ParisDataDto(
    @SerializedName("nhits") val total: Int,
    @SerializedName("records") val records: List<RecordDto>,
)

data class RecordDto(
    @SerializedName("recordid") val recordId: String?,
    @SerializedName("fields") val fields: FieldsDto?,
)


data class FieldsDto(
    @SerializedName("complement_adresse") val complementAdresse: String?,
    @SerializedName("horaire") val horaire: String?,
    @SerializedName("acces_pmr") val prmAccess: String?,
    @SerializedName("relais_bebe") val babyRely: String?,
    @SerializedName("arrondissement") val arrondissement: Int?,
    @SerializedName("geo_point_2d") val geoPoint2d: List<Double>?,
    @SerializedName("source") val source: String?,
    @SerializedName("gestionnaire") val gestionnaire: String?,
    @SerializedName("adresse") val adresse: String?,
    @SerializedName("url_fiche_equipement") val equipmentInfoUrl:String?,
)


const val Yes = "Oui"

