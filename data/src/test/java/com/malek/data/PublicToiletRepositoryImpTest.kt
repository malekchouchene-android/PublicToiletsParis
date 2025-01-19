package com.malek.data

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.malek.data.api.ParisDataApi
import com.malek.data.api.dto.FieldsDto
import com.malek.data.api.dto.ParisDataDto
import com.malek.data.api.dto.RecordDto
import com.malek.domain.Query
import com.malek.domain.models.Service
import com.malek.testingutlis.CoroutinesTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PublicToiletRepositoryImpTest {

    private lateinit var parisDataApi: ParisDataApi
    private lateinit var publicToiletRepository: PublicToiletRepositoryImp
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmField
    @Rule
    val coroutinesTestRule = CoroutinesTestRule(testDispatcher)


    @Before
    fun setUp() {
        parisDataApi = mockk()
        publicToiletRepository = PublicToiletRepositoryImp(parisDataApi, testDispatcher)
    }

    @Test
    fun `getPublicToilets returns success`() = runTest {
        val query = Query(
            latLong = Pair(48.8566, 2.3522),
            firstIndex = 0,
            services = listOf(Service.PRM_ACCESS)
        )
        val fieldsDto = FieldsDto(
            adresse = "123 Street",
            arrondissement = 75001,
            prmAccess = "Oui",
            babyRely = "Oui",
            geoPoint2d = listOf(48.8566, 2.3522),
            horaire = "24/7",
            equipmentInfoUrl = "http://example.com",
            source = null,
            gestionnaire = null,
            complementAdresse = "test"
        )
        val recordDto = RecordDto(recordId = "1", fields = fieldsDto)
        val parisDataDto = ParisDataDto(total = 1, records = listOf(recordDto))
        coEvery {
            parisDataApi.getRecordsByQuery(
                dataset = "sanisettesparis2011",
                rows = 20,
                pmrAcces = "Oui",
                babyRely = null,
                firstIndex = 0,
                geo = "48.8566,2.3522,1000"
            )
        } returns parisDataDto

        val result = publicToiletRepository.getPublicToilets(query)
        advanceUntilIdle()

        Truth.assertThat(result.isSuccess).isTrue()
        val publicToiletListPageResult = result.getOrNull()
        assertThat(publicToiletListPageResult).isNotNull()
        assertThat(publicToiletListPageResult?.totalNumber).isEqualTo(1)
        assertThat(publicToiletListPageResult?.pageSize).isEqualTo(1)
        assertThat(publicToiletListPageResult?.result?.first()?.id).isEqualTo("1")
    }


    @Test
    fun `getPublicToilets returns success and drop corrupted data`() = runTest {
        val query = Query(
            latLong = null,
            firstIndex = 0,
            services = emptyList()
        )
        val fieldsDto1 = FieldsDto(
            adresse = "123 Street",
            arrondissement = 75001,
            prmAccess = "Oui",
            babyRely = "Oui",
            geoPoint2d = listOf(48.8566, 2.3522),
            horaire = "24/7",
            equipmentInfoUrl = "http://example.com",
            source = null,
            gestionnaire = null,
            complementAdresse = "test"
        )
        val recordDto1 = RecordDto(recordId = "1", fields = fieldsDto1)

        val fieldsDto2 = FieldsDto(
            adresse = null,
            arrondissement = 75001,
            prmAccess = "Oui",
            babyRely = "Oui",
            geoPoint2d = listOf(48.8566),
            horaire = "24/7",
            equipmentInfoUrl = "http://example.com",
            source = null,
            gestionnaire = null,
            complementAdresse = "test"
        )
        val recordDto2 = RecordDto(recordId = "2", fields = fieldsDto2)
        val parisDataDto = ParisDataDto(total = 2, records = listOf(recordDto1, recordDto2))
        coEvery {
            parisDataApi.getRecordsByQuery(
                dataset = "sanisettesparis2011",
                rows = 20,
                pmrAcces = null,
                babyRely = null,
                firstIndex = 0,
                geo = null,
            )
        } returns parisDataDto

        val result = publicToiletRepository.getPublicToilets(query)
        advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()
        val publicToiletListPageResult = result.getOrNull()
        assertThat(publicToiletListPageResult).isNotNull()
        assertThat(publicToiletListPageResult?.totalNumber).isEqualTo(2)
        assertThat(publicToiletListPageResult?.pageSize).isEqualTo(2)
        assertThat(publicToiletListPageResult?.result?.size).isEqualTo(1)
        assertThat(publicToiletListPageResult?.result?.first()?.id).isEqualTo("1")
    }

    @Test
    fun `should call api with filter service when are in the query`() {
        runTest {
            // PMR Only service
            val queryPMR = Query(
                latLong = null,
                firstIndex = 0,
                services = listOf(Service.PRM_ACCESS)
            )
            coEvery {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = "Oui",
                    babyRely = null,
                    firstIndex = 0,
                    geo = null,
                )
            } returns mockk()

            // 2 service
            publicToiletRepository.getPublicToilets(queryPMR)
            advanceUntilIdle()
            coVerify {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = "Oui",
                    babyRely = null,
                    firstIndex = 0,
                    geo = null,
                )
            }


            val queryBabyOnly = Query(
                latLong = null,
                firstIndex = 0,
                services = listOf(Service.BABY_RELY)
            )

            coEvery {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = null,
                    babyRely = "Oui",
                    firstIndex = 0,
                    geo = null,
                )
            } returns mockk()

            publicToiletRepository.getPublicToilets(queryBabyOnly)

            coVerify {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = null,
                    babyRely = "Oui",
                    firstIndex = 0,
                    geo = null,
                )
            }
            // 2 services
            val query2 = Query(
                latLong = null,
                firstIndex = 0,
                services = listOf(Service.PRM_ACCESS, Service.BABY_RELY)
            )

            coEvery {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = "Oui",
                    babyRely = null,
                    firstIndex = 0,
                    geo = null,
                )
            } returns mockk()

            publicToiletRepository.getPublicToilets(query2)

            coVerify {
                parisDataApi.getRecordsByQuery(
                    dataset = "sanisettesparis2011",
                    rows = 20,
                    pmrAcces = "Oui",
                    babyRely = "Oui",
                    firstIndex = 0,
                    geo = null,
                )
            }
        }
    }


    @Test
    fun `getPublicToilets returns failure`() = runTest(testDispatcher) {
        val query = Query(
            latLong = Pair(48.8566, 2.3522),
            firstIndex = 0,
            services = listOf(Service.PRM_ACCESS)
        )
        coEvery {
            parisDataApi.getRecordsByQuery(
                any(),
                any(),
                any(),
                any()
            )
        } throws RuntimeException("API error")

        val result = publicToiletRepository.getPublicToilets(query)
        advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
    }
}