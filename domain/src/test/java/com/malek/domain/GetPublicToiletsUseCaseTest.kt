package com.malek.domain

import com.google.common.truth.Truth
import com.malek.domain.models.PublicToilet
import com.malek.domain.models.PublicToiletListPageResult
import com.malek.testingutlis.CoroutinesTestRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GetPublicToiletsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmField
    @Rule
    val coroutinesTestRule = CoroutinesTestRule(testDispatcher)

    @Test
    fun should_return_result_ok_when_repo_return_ok() {
        runTest {
            // given
            val repository: PublicToiletRepository = mockk()
            val expectedRes = PublicToiletListPageResult(
                totalNumber = 1,
                pageSize = 1,
                result = listOf(
                    PublicToilet(
                        id = "test",
                        address = "test",
                        latLong = null,
                        hours = "24/24",
                        servicesAvailable = emptyList(),
                        equipmentInfoUrl = null
                    )
                )
            )
            coEvery {
                repository.getPublicToilets(Query())
            } returns  Result.success(expectedRes)
            // when
            val useCase = GetPublicToiletsUseCase(repository)
            val res = useCase.getPublicToiletsByQuery(Query())
            advanceUntilIdle()
            // Then

            Truth.assertThat(res.getOrThrow().result).isEqualTo(expectedRes.result)


        }
    }

    @Test
    fun should_return_result_failure_when_repo_return_ko() {
        runTest {
            // given
            val repository: PublicToiletRepository = mockk()
            val expectedThrowable = RuntimeException()
            coEvery {
                repository.getPublicToilets(Query())
            } returns  Result.failure(expectedThrowable)
            // when
            val useCase = GetPublicToiletsUseCase(repository)
            val res = useCase.getPublicToiletsByQuery(Query())
            advanceUntilIdle()
            // Then

            Truth.assertThat(res.isFailure).isEqualTo(true)
            Truth.assertThat(res.exceptionOrNull()).isEqualTo(expectedThrowable)


        }
    }

}



