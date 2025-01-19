package com.malek.toiletesparis.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.malek.domain.models.PublicToilet
import com.malek.toiletesparis.ui.shared.ErrorState
import com.malek.toiletesparis.utils.OnBottomReached

@Composable
fun PublicToiletsListScreen(
    modifier: Modifier = Modifier,
    publicToiletsFetched: List<PublicToilet>,
    isLoading: Boolean,
    error: Throwable?,
    onRetry: () -> Unit,
    openMaps: (Pair<Double, Double>, String) -> Unit,
    requestNextPage: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, start = 8.dp, end = 8.dp)
    ) {
        items(
            publicToiletsFetched,
            key = {
                it.id
            }
        ) { publicToilet ->
            PublicToiletItemCompose(
                modifier = Modifier
                    .fillMaxSize(),
                publicToilet
            ) { latLong, label ->
                openMaps(latLong, label)
            }
        }
        if (isLoading) {
            item("Loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (error != null) {
            item("error") {
                ErrorState(modifier = Modifier.fillMaxWidth()) {
                    onRetry()
                }
            }
        }
    }

    lazyListState.OnBottomReached {
        requestNextPage()
    }
}