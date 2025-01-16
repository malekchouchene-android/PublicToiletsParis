package com.malek.toiletesparis.data

import com.malek.toiletesparis.data.api.ParisDataApi
import com.malek.toiletesparis.data.api.dto.Yes
import com.malek.toiletesparis.data.api.dto.toPublicToilet
import com.malek.toiletesparis.di.BackgroundDispatcher
import com.malek.toiletesparis.domain.PublicToiletRepository
import com.malek.toiletesparis.domain.Query
import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.PublicToiletListPageResult
import com.malek.toiletesparis.utils.runSuspendCatching
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
                    pmrAcces = if (query.filterByPrmAccess) Yes else null
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