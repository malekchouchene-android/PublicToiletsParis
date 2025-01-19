package com.malek.toiletesparis.ui.list

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.malek.toiletparis.domain.GetPublicToiletsUseCase
import com.malek.toiletparis.domain.Query
import com.malek.toiletparis.domain.models.PublicToilet
import com.malek.toiletparis.domain.models.PublicToiletListPageResult
import com.malek.toiletparis.domain.models.Service
import com.malek.testingutlis.CoroutinesTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test


class PublicToiletsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmField
    @Rule
    val coroutinesTestRule = CoroutinesTestRule(testDispatcher)
    val publicToiletFixture = PublicToilet(
        address = "test address, 75010",
        servicesAvailable = listOf(
            Service.PRM_ACCESS, Service.BABY_RELY
        ),
        id = "id",
        equipmentInfoUrl = "url",
        latLong = null,
        hours = "24/24"
    )

    @Test
    fun should_update_state_on_get_list() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 1,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture
                        )
                    )
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.error).isNull()
                Truth.assertThat(firstState.publicToiletsFetched).isEmpty()
                Truth.assertThat(firstState.endReached).isFalse()
                val resultState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState.isLoading).isFalse()
                Truth.assertThat(resultState.error).isNull()
                Truth.assertThat(resultState.publicToiletsFetched).isEqualTo(
                    listOf(publicToiletFixture)
                )
                Truth.assertThat(resultState.endReached).isTrue()
                viewModel.requestNextPage()
                expectNoEvents()
                coVerify(atMost = 1, atLeast = 1) {
                    useCase.getPublicToiletsByQuery(any())
                }
            }
        }
    }


    @Test
    fun should_get_next_and_stop_when_all_fetched() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 3,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture.copy(id = "1")
                        )
                    )
                )
            )

            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 1,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 3,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture.copy(
                                id = "2"
                            )
                        )
                    )
                )
            )


            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 2,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 3,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture.copy(
                                id = "3"
                            )
                        )
                    )
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.error).isNull()
                Truth.assertThat(firstState.publicToiletsFetched).isEmpty()
                Truth.assertThat(firstState.endReached).isFalse()
                val resultState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState.isLoading).isFalse()
                Truth.assertThat(resultState.error).isNull()
                Truth.assertThat(resultState.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture.copy(
                            id = "1"
                        )
                    )
                )
                Truth.assertThat(resultState.endReached).isFalse()
                viewModel.requestNextPage()
                val loadingState1 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingState1.isLoading).isTrue()
                Truth.assertThat(loadingState1.error).isNull()
                Truth.assertThat(loadingState1.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture.copy(
                            id = "1"
                        )
                    )
                )
                val result1 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(result1.isLoading).isFalse()
                Truth.assertThat(result1.error).isNull()
                Truth.assertThat(result1.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture.copy(
                            id = "1",
                        ),
                        publicToiletFixture.copy(
                            id = "2"
                        )
                    )
                )
                Truth.assertThat(result1.endReached).isFalse()
                viewModel.requestNextPage()
                val loadingState2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingState2.isLoading).isTrue()
                Truth.assertThat(loadingState2.error).isNull()
                Truth.assertThat(loadingState2.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture.copy(
                            id = "1",
                        ),
                        publicToiletFixture.copy(
                            id = "2"
                        )
                    )
                )
                val result2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(result2.isLoading).isFalse()
                Truth.assertThat(result2.error).isNull()
                Truth.assertThat(result2.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture.copy(
                            id = "1",
                        ),
                        publicToiletFixture.copy(
                            id = "2"
                        ),
                        publicToiletFixture.copy(
                            id = "3"
                        )
                    )
                )
                Truth.assertThat(result2.endReached).isTrue()
                viewModel.requestNextPage()
                expectNoEvents()
                coVerify(atMost = 3, atLeast = 3) {
                    useCase.getPublicToiletsByQuery(any())
                }
            }
        }
    }


    @Test
    fun should_handel_error() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.failure(
                    RuntimeException()
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.error).isNull()
                Truth.assertThat(firstState.publicToiletsFetched).isEmpty()
                Truth.assertThat(firstState.endReached).isFalse()
                val resultState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState.isLoading).isFalse()
                Truth.assertThat(resultState.error).isInstanceOf(RuntimeException::class.java)
                Truth.assertThat(resultState.publicToiletsFetched).isEqualTo(
                    emptyList<PublicToilet>()
                )
                Truth.assertThat(resultState.endReached).isFalse()
                viewModel.requestNextPage()
                expectNoEvents()
                coVerify(atMost = 1, atLeast = 1) {
                    useCase.getPublicToiletsByQuery(any())
                }
            }
        }
    }

    @Test
    fun should_search_by_geo() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 1,
                        pageSize = 1,
                        result = emptyList()
                    )
                )
            )

            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = 4.2 to 4.1,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 1,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture
                        )
                    )
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.currentLocationFetching).isFalse()
                val resultEmpty = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultEmpty.isLoading).isFalse()
                Truth.assertThat(resultEmpty.error).isNull()
                Truth.assertThat(resultEmpty.publicToiletsFetched).isEmpty()
                // User give permission
                viewModel.onCurrentLocationFetching()
                val resultState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState.currentLocationFetching).isTrue()
                // get current Location
                viewModel.onCurrentLocationFetched(latLong = 4.2 to 4.1)
                val resultState2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState2.currentLocationFetching).isFalse()
                Truth.assertThat(resultState2.isLoading).isFalse()
                val loadingState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingState.isLoading).isTrue()
                Truth.assertThat(loadingState.error).isNull()
                Truth.assertThat(loadingState.publicToiletsFetched).isEmpty()
                Truth.assertThat(loadingState.endReached).isFalse()
                Truth.assertThat(loadingState.locationMode).isTrue()
                val resultState3 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState3.isLoading).isFalse()
                Truth.assertThat(resultState3.error).isNull()
                Truth.assertThat(resultState3.publicToiletsFetched).isEqualTo(
                    listOf(publicToiletFixture)
                )
                Truth.assertThat(resultState3.endReached).isTrue()
                Truth.assertThat(loadingState.locationMode).isTrue()
                viewModel.resetLocation()
                val loadingRest = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingRest.isLoading).isTrue()
                Truth.assertThat(loadingRest.error).isNull()
                Truth.assertThat(loadingRest.publicToiletsFetched).isEmpty()
                Truth.assertThat(loadingRest.endReached).isFalse()
                Truth.assertThat(loadingRest.locationMode).isFalse()
                val resultRest = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultRest.isLoading).isFalse()
                Truth.assertThat(resultRest.error).isNull()
                Truth.assertThat(resultRest.publicToiletsFetched).isEmpty()
                Truth.assertThat(resultRest.endReached).isTrue()
                Truth.assertThat(resultRest.locationMode).isFalse()

            }
        }
    }


    @Test
    fun should_search_by_service() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 1,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture, publicToiletFixture.copy(id = "2")
                        )
                    )
                )
            )

            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = listOf(Service.PRM_ACCESS),
                    )
                )
            }.returns(
                Result.success(
                    PublicToiletListPageResult(
                        totalNumber = 1,
                        pageSize = 1,
                        result = listOf(
                            publicToiletFixture
                        )
                    )
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.listOfServiceSelected).isEmpty()
                val resultFilterLess = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultFilterLess.isLoading).isFalse()
                Truth.assertThat(resultFilterLess.error).isNull()
                Truth.assertThat(resultFilterLess.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture, publicToiletFixture.copy(id = "2")
                    )
                )
                // User give permission
                viewModel.toggleService(service = Service.PRM_ACCESS)
                val loadingStateFilter = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingStateFilter.isLoading).isTrue()
                Truth.assertThat(loadingStateFilter.error).isNull()
                Truth.assertThat(loadingStateFilter.publicToiletsFetched).isEmpty()
                Truth.assertThat(loadingStateFilter.listOfServiceSelected)
                    .isEqualTo(listOf(Service.PRM_ACCESS))
                val resultStateFilter = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultStateFilter.isLoading).isFalse()
                Truth.assertThat(resultStateFilter.error).isNull()
                Truth.assertThat(resultStateFilter.publicToiletsFetched).isEqualTo(
                    listOf(publicToiletFixture)
                )
                Truth.assertThat(resultStateFilter.listOfServiceSelected)
                    .isEqualTo(listOf(Service.PRM_ACCESS))
                Truth.assertThat(
                    resultStateFilter.endReached
                ).isTrue()

                viewModel.toggleService(service = Service.PRM_ACCESS)
                val loadingStateFilter2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingStateFilter2.isLoading).isTrue()
                Truth.assertThat(loadingStateFilter2.error).isNull()
                Truth.assertThat(loadingStateFilter2.publicToiletsFetched).isEmpty()
                Truth.assertThat(loadingStateFilter2.listOfServiceSelected).isEmpty()
                val resultStateFilter2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultStateFilter2.isLoading).isFalse()
                Truth.assertThat(resultStateFilter2.error).isNull()
                Truth.assertThat(resultStateFilter2.publicToiletsFetched).isEqualTo(
                    listOf(
                        publicToiletFixture, publicToiletFixture.copy(id = "2")
                    )
                )
                Truth.assertThat(resultStateFilter2.listOfServiceSelected).isEmpty()
                Truth.assertThat(
                    resultStateFilter2.endReached
                ).isTrue()
            }
        }
    }

    @Test
    fun should_retry_last_query_when_error() {
        runTest {
            val useCase = mockk<GetPublicToiletsUseCase>()
            val viewModel = PublicToiletsListViewModel(
                useCase = useCase,
                backgroundDispatcher = testDispatcher
            )
            coEvery {
                useCase.getPublicToiletsByQuery(
                    query = Query(
                        firstIndex = 0,
                        latLong = null,
                        services = emptyList(),
                    )
                )
            }.returns(
                Result.failure(
                    RuntimeException()
                )
            )
            viewModel.state.test {
                val firstState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(firstState.isLoading).isTrue()
                Truth.assertThat(firstState.error).isNull()
                Truth.assertThat(firstState.publicToiletsFetched).isEmpty()
                Truth.assertThat(firstState.endReached).isFalse()
                val resultState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState.isLoading).isFalse()
                Truth.assertThat(resultState.error).isInstanceOf(RuntimeException::class.java)
                Truth.assertThat(resultState.publicToiletsFetched).isEqualTo(
                    emptyList<PublicToilet>()
                )
                Truth.assertThat(resultState.endReached).isFalse()
                coEvery {
                    useCase.getPublicToiletsByQuery(
                        query = Query(
                            firstIndex = 0,
                            latLong = null,
                            services = emptyList(),
                        )
                    )
                }.returns(
                    Result.success(
                        PublicToiletListPageResult(
                            totalNumber = 1,
                            pageSize = 1,
                            result = listOf(
                                publicToiletFixture
                            )
                        )
                    )
                )

                viewModel.retry()

                val loadingState = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(loadingState.isLoading).isTrue()
                Truth.assertThat(loadingState.error).isNull()
                Truth.assertThat(loadingState.publicToiletsFetched).isEmpty()
                Truth.assertThat(loadingState.endReached).isFalse()
                val resultState2 = awaitItem()
                advanceUntilIdle()
                Truth.assertThat(resultState2.isLoading).isFalse()
                Truth.assertThat(resultState2.error).isNull()
                Truth.assertThat(resultState2.publicToiletsFetched).isEqualTo(
                    listOf(publicToiletFixture)
                )
                Truth.assertThat(resultState2.endReached).isTrue()
            }
        }
    }
}
