package com.malek.toiletesparis.data.api

import com.malek.toiletesparis.data.api.dto.ParisDataDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ParisDataApi {
    @GET("search/")
    suspend fun getRecordsByQuery(
        @Query("dataset") dataset: String = "sanisettesparis2011",
        @Query("rows") rows: Int = 20,
        @Query("refine.acces_pmr") pmrAcces: String? = null,
        @Query("refine.relais_bebe") babyRely: String? = null,
        @Query("start") firstIndex: Int = 0,
        @Query("geofilter.distance") geo: String? = null
    ): ParisDataDto
}