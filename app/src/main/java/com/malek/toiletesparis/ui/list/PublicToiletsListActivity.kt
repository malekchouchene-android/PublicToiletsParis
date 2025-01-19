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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.malek.domain.models.Service
import com.malek.toiletesparis.R
import com.malek.toiletesparis.ui.shared.EmptyState
import com.malek.toiletesparis.ui.shared.ErrorState
import com.malek.toiletesparis.ui.shared.FullScreenLoader
import com.malek.toiletesparis.ui.theme.ToiletesParisTheme
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
                viewModel.updateCurrentLocationRefused()
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
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            ToiletesParisTheme {
                Scaffold(
                    modifier = Modifier
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

                    },
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        if (state.isLoading && state.publicToiletsFetched.isEmpty()) {
                            FullScreenLoader()
                        } else {
                            if (state.publicToiletsFetched.isEmpty()) {
                                if (state.error == null) {
                                    EmptyState()
                                } else {
                                    ErrorState(Modifier.fillMaxSize()) {
                                        viewModel.retry()
                                    }
                                }

                            } else {
                                PublicToiletsListScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    publicToiletsFetched = state.publicToiletsFetched,
                                    isLoading = state.isLoading,
                                    openMaps = { latLong, label ->
                                        openMaps(latLong = latLong, label = label)
                                    },
                                    error = state.error,
                                    requestNextPage = {
                                        viewModel.requestNextPage()
                                    },
                                    onRetry = {
                                        viewModel.retry()
                                    }
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    private fun openMaps(latLong: Pair<Double, Double>, label: String) {
        val uri =
            Uri.parse(
                "geo:${latLong.first},${latLong.second}?q=${latLong.first}," +
                        "${latLong.second}(${label})"
            )

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


@StringRes
fun Service.getLabel(): Int {
    return when (this) {
        Service.BABY_RELY -> R.string.baby_rely_message
        Service.PRM_ACCESS -> R.string.prm_access_message
    }
}