package com.malek.toiletesparis.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malek.toiletesparis.di.BackgroundDispatcher
import com.malek.toiletesparis.domain.GetPublicToiletsUseCase
import com.malek.toiletesparis.domain.Query
import com.malek.toiletesparis.domain.models.PublicToilet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    init {
        viewModelScope.launch {
            withContext(backgroundDispatcher) {
                query.onEach {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
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
                            PublicToiletsListState(
                                publicToiletsFetched = newList,
                                isLoading = false,
                                error = null,
                                totalFetched = totalFetched,
                                endReached = totalFetched == publicToiletListPageResult.totalNumber
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
    }

    fun requestNextPage() {
        if (state.value.endReached || state.value.isLoading) return
        viewModelScope.launch {
            Timber.e("firstIndex ${_state.value.totalFetched}")
            query.update {
                it.copy(
                    firstIndex = _state.value.totalFetched,
                )
            }
        }
    }
}


data class PublicToiletsListState(
    val publicToiletsFetched: List<PublicToilet>,
    val isLoading: Boolean,
    val error: Throwable?,
    val endReached: Boolean,
    val totalFetched: Int,
)

private fun initialState(): PublicToiletsListState =
    PublicToiletsListState(
        publicToiletsFetched = emptyList(),
        isLoading = true,
        error = null,
        totalFetched = 0,
        endReached = false
    )