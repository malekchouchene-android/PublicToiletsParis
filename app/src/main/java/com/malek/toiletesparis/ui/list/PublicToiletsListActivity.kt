package com.malek.toiletesparis.ui.list

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.malek.toiletesparis.R
import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.Service
import com.malek.toiletesparis.ui.theme.ToiletesParisTheme
import com.malek.toiletesparis.utils.OnBottomReached
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PublicToiletsListActivity : ComponentActivity() {
    val viewModel by viewModels<PublicToiletsListViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val lazyListState = rememberLazyListState()

            ToiletesParisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (state.isLoading && state.publicToiletsFetched.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                state.publicToiletsFetched,
                                key = {
                                    it.id
                                }
                            ) { publicToilet ->
                                PublicToiletItemCompose(
                                    modifier = Modifier.fillMaxSize(),
                                    publicToilet
                                ) { (lat, long) ->
                                    val uri =
                                        Uri.parse("geo:${lat},${long}?q=${lat},${long}(${publicToilet.address})")

                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Timber.e("No app found")
                                    }

                                }
                            }
                            if (state.isLoading) {
                                item {
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

                        }
                    }
                }
            }
            lazyListState.OnBottomReached {
                viewModel.requestNextPage()
            }
        }
    }
}


@Composable
fun PublicToiletItemCompose(
    modifier: Modifier = Modifier,
    toilet: PublicToilet,
    onNavigateClick: (Pair<Double, Double>) -> Unit
) {
    Card(modifier = modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.adresse_public_toilet, toilet.address),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Horaires : ${toilet.hours ?: "Non spécifié"}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (toilet.servicesAvailable.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.services_tilte),
                    style = MaterialTheme.typography.bodyMedium
                )
                toilet.servicesAvailable.forEach { service ->
                    val serviceText = when (service) {
                        Service.BABY_RELY -> stringResource(R.string.baby_rely_message)
                        Service.PRM_ACCESS -> stringResource(R.string.prm_access_message)
                    }
                    Text(text = serviceText)
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            if (toilet.latLong != null) {
                Button(onClick = { onNavigateClick(toilet.latLong) }) {
                    Text(text = stringResource(R.string.open_maps_cta))
                }
            }
        }
    }
}
