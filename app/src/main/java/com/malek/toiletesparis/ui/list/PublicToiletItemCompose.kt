package com.malek.toiletesparis.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.malek.toiletesparis.R
import com.malek.toiletesparis.domain.models.PublicToilet
import com.malek.toiletesparis.domain.models.Service

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