package com.malek.data

import com.malek.data.api.ParisDataApi
import com.malek.data.api.dto.Yes
import com.malek.data.api.dto.toPublicToilet
import com.malek.domain.PublicToiletRepository
import com.malek.domain.Query
import com.malek.domain.models.PublicToiletListPageResult
import com.malek.domain.models.Service
import com.malek.utlis.BackgroundDispatcher
import com.malek.utlis.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PublicToiletRepositoryImp @Inject constructor(
    private val parisDataApi: ParisDataApi,
    @BackgroundDispatcher val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PublicToiletRepository {

    override suspend fun getPublicToilets(query: Query): Result<PublicToiletListPageResult> {
        return runSuspendCatching {
            withContext(backgroundDispatcher) {
                val parisDataDto = parisDataApi.getRecordsByQuery(
                    geo = query.latLong?.let {
                        "${it.first},${it.second},${query.distance}"
                    },
                    firstIndex = query.firstIndex,
                    pmrAcces = if (query.services.any { it == Service.PRM_ACCESS }) Yes else null,
                    babyRely = if (query.services.any { it == Service.BABY_RELY }) Yes else null
                )
                PublicToiletListPageResult(
                    totalNumber = parisDataDto.total,
                    pageSize = parisDataDto.records.size,
                    firstIndex = query.firstIndex,
                    result = parisDataDto.records.mapNotNull {
                        it.toPublicToilet()
                    }
                )
            }
        }.onFailure {
            Timber.e(it.toString())
        }
    }
}