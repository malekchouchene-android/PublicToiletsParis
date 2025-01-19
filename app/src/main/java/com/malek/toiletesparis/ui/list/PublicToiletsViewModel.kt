package com.malek.toiletesparis.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malek.domain.GetPublicToiletsUseCase
import com.malek.domain.Query
import com.malek.domain.models.PublicToilet
import com.malek.domain.models.Service
import com.malek.utlis.BackgroundDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PublicToiletsListViewModel @Inject constructor(
    private val useCase: GetPublicToiletsUseCase,
    @BackgroundDispatcher
    private val backgroundDispatcher: CoroutineDispatcher,
) : ViewModel() {


    private val query: MutableStateFlow<Query> = MutableStateFlow(
        Query()
    )
    private val _state: MutableStateFlow<PublicToiletsListState> = MutableStateFlow(
        initialState()
    )
    val state: StateFlow<PublicToiletsListState> = _state

    private var fetchJob: Job? = null

    init {
        fetchJob = initFetchJob()
    }

    private fun initFetchJob(): Job = viewModelScope.launch {
        withContext(backgroundDispatcher) {
            query.onEach { localQuery ->
                _state.update {
                    if (localQuery.firstIndex == 0) {
                        initialState(
                            locationMode = localQuery.latLong != null,
                            listOfServiceSelected = localQuery.services,
                        )
                    } else {
                        it.copy(
                            isLoading = true,
                            locationMode = localQuery.latLong != null,
                            listOfServiceSelected = localQuery.services,
                            error = null
                        )
                    }
                }
            }.map {
                useCase.getPublicToiletsByQuery(
                    query = it
                )
            }.collectLatest { result ->
                result.onSuccess { publicToiletListPageResult ->
                    val lastState = _state.value
                    val toilets = lastState.publicToiletsFetched.toMutableList()
                    toilets.addAll(publicToiletListPageResult.result)
                    val newList = toilets.distinctBy { it.id }
                    val totalFetched =
                        lastState.totalFetched + publicToiletListPageResult.pageSize
                    _state.update {
                        it.copy(
                            publicToiletsFetched = newList,
                            isLoading = false,
                            error = null,
                            totalFetched = totalFetched,
                            endReached = totalFetched == publicToiletListPageResult.totalNumber,
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable
                        )
                    }
                }
            }
        }
    }

    fun requestNextPage() {
        if (state.value.endReached || state.value.isLoading || state.value.error != null) return
        viewModelScope.launch {
            query.update {
                it.copy(
                    firstIndex = _state.value.totalFetched,
                )
            }
        }
    }

    fun onCurrentLocationFetching() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentLocationFetching = true,
                )
            }
        }
    }

    fun onCurrentLocationFetched(latLong: Pair<Double, Double>?) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentLocationFetching = false,
                    currentLocationRefused = false
                )
            }
            if (query.value.latLong != latLong) {
                query.update {
                    it.copy(
                        latLong = latLong,
                        firstIndex = 0,
                    )
                }
            }
        }
    }

    fun updateCurrentLocationRefused() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentLocationRefused = true,
                    currentLocationFetching = false,
                    locationMode = false
                )
            }
        }
    }


    fun toggleService(service: Service) {
        viewModelScope.launch(backgroundDispatcher) {
            val serviceSelected = state.value.listOfServiceSelected.toMutableList()
            if (serviceSelected.any { it == service }) {
                serviceSelected.remove(service)
            } else {
                serviceSelected.add(service)
            }
            query.update {
                it.copy(
                    firstIndex = 0,
                    services = serviceSelected
                )
            }
        }
    }

    fun resetLocation() {
        viewModelScope.launch {
            query.update {
                it.copy(
                    latLong = null,
                    firstIndex = 0,
                )
            }
        }
    }

    fun retry() {
        fetchJob?.cancel()
        fetchJob = initFetchJob()
    }
}


data class PublicToiletsListState(
    val publicToiletsFetched: List<PublicToilet>,
    val isLoading: Boolean,
    val error: Throwable?,
    val endReached: Boolean,
    val totalFetched: Int,
    val currentLocationFetching: Boolean = false,
    val currentLocationRefused: Boolean? = null,
    val listOfServiceSelected: List<Service> = emptyList(),
    val locationMode: Boolean = false
)

private fun initialState(
    locationMode: Boolean = false,
    listOfServiceSelected: List<Service> = emptyList(),
): PublicToiletsListState =
    PublicToiletsListState(
        publicToiletsFetched = emptyList(),
        isLoading = true,
        error = null,
        totalFetched = 0,
        endReached = false,
        locationMode = locationMode,
        listOfServiceSelected = listOfServiceSelected
    )