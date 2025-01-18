package com.malek.toiletesparis.ui.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.malek.toiletesparis.R
import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.Service
import com.malek.toiletesparis.ui.theme.ToiletesParisTheme
import com.malek.toiletesparis.utils.OnBottomReached
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PublicToiletsListActivity : ComponentActivity() {
    private val viewModel by viewModels<PublicToiletsListViewModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                onUserGivePermissionToGetLastLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                onUserGivePermissionToGetLastLocation()
            }

            else -> {
                Timber.e("Permisson refused")
                viewModel.updateCurrentLocationRefused(currentLocationRefused = true)
            }
        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val lazyListState = rememberLazyListState()
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            ToiletesParisTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            modifier = Modifier
                                .fillMaxWidth(),
                            collapsedHeight = 84.dp,
                            title = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Paris Toiltes"
                                    )
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(end = 8.dp)
                                            .horizontalScroll(state = rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnimatedVisibility(state.locationMode) {
                                            FilterChip(selected = true, label = {
                                                Text(stringResource(R.string.around_user_filter))
                                            }, onClick = {
                                                viewModel.resetLocation()
                                            })
                                        }
                                        for (service in Service.entries) {
                                            FilterChip(
                                                selected = service in state.listOfServiceSelected,
                                                label = {
                                                    Text(stringResource(service.getLabel()))
                                                },
                                                onClick = {
                                                    if (!state.isLoading) {
                                                        viewModel.toggleService(service = service)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                    floatingActionButton = {
                        if (state.currentLocationRefused == null || state.currentLocationRefused == false) {
                            FloatingActionButton(
                                onClick = {
                                    if (!state.currentLocationFetching) {
                                        getCurrentLocalisation()
                                    }
                                },
                            ) {
                                if (!state.currentLocationFetching) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = "filter par localisation"
                                    )
                                } else {
                                    CircularProgressIndicator()
                                }

                            }
                        }

                    }) { innerPadding ->
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
                        if (state.publicToiletsFetched.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.empty_list_result_message)
                                )
                            }

                        } else {
                            LazyColumn(
                                Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                state = lazyListState,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(
                                    state.publicToiletsFetched,
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

    private fun openMaps(latLong: Pair<Double, Double>, label: String) {
        val uri =
            Uri.parse("geo:${latLong.first},${latLong.second}?q=${latLong.first},${latLong.second}(${label})")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e("No app found")
        }
    }

    private fun getCurrentLocalisation() {
        viewModel.onCurrentLocationFetching()
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun onUserGivePermissionToGetLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            viewModel.onCurrentLocationFetched(latLong = it.latitude to it.longitude)
        }.addOnFailureListener {
            viewModel.onCurrentLocationFetched(latLong = null)
        }
    }
}


@Composable
fun PublicToiletItemCompose(
    modifier: Modifier = Modifier,
    toilet: PublicToilet,
    onNavigateClick: (Pair<Double, Double>, String) -> Unit
) {
    Card(modifier = modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = toilet.address.capitalize(
                    Locale.current
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.open_hours,
                    toilet.hours ?: stringResource(R.string.open_hours_unavailable)
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (toilet.servicesAvailable.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.services_title),
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
                Button(onClick = { onNavigateClick(toilet.latLong, toilet.address) }) {
                    Text(text = stringResource(R.string.open_maps_cta))
                }
            }
        }
    }
}

@StringRes
fun Service.getLabel(): Int {
    return when (this) {
        Service.BABY_RELY -> R.string.baby_rely_message
        Service.PRM_ACCESS -> R.string.prm_access_message
    }
}
