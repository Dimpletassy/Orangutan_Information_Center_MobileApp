package com.oic.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.ui.palette.*

@Composable
fun WeatherCard(
    temperatureC: Int?,
    humidityPct: Int?,
    windKmh: Int?,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onUseDeviceLocation: () -> Unit
) {
    val safeToWater =
        (temperatureC ?: 25) in 10..34 &&
                (humidityPct ?: 60) < 80 &&
                (windKmh ?: 10) < 25

    Surface(
        shape = CardXL,
        color = SurfaceWhite.copy(alpha = 0.92f),
        tonalElevation = 1.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "☀️ ☔️️ Weather & Humidity",
                color = CocoaDeep,
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            // --- Temperature and safety row ---
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = temperatureC?.let { "$it°" } ?: "—",
                    fontSize = 42.sp,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CocoaDeep
                )

                Text(
                    text = if (safeToWater) "✅ Safe to water today" else "⚠️ Not ideal to water",
                    color = if (safeToWater) LeafGreen else CocoaDeep,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- Weather stats ---
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 3
            ) {
                StatPill("Temp", temperatureC?.let { "$it°C" } ?: "—", minWidth = 120.dp)
                StatPill("Humidity", humidityPct?.let { "$it%" } ?: "—", minWidth = 120.dp)
                StatPill("Wind", windKmh?.let { "$it km/h" } ?: "—", minWidth = 120.dp)
            }

            Spacer(Modifier.height(16.dp))

            // --- Buttons row ---
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRefresh,
                    shape = Pill,
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceWhite.copy(alpha = 0.85f),
                        contentColor = CocoaDeep
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CocoaDeep
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (loading) "Refreshing…" else "Refresh")
                }

                OutlinedButton(onClick = onUseDeviceLocation, shape = Pill) {
                    Text("Use device location")
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Couldn’t update weather: $error",
                    color = Cocoa.copy(alpha = .75f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* --- Helper stat pills --- */

@Composable
private fun StatPill(title: String, value: String, minWidth: Dp = 0.dp) {
    Surface(shape = Pill, color = SurfaceWhite, shadowElevation = 1.dp) {
        Row(
            Modifier
                .widthIn(min = minWidth)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$title: ", color = Cocoa)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = CocoaDeep)
        }
    }
}
