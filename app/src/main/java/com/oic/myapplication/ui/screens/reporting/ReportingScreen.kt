package com.oic.myapplication.ui.screens.reporting

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.oic.myapplication.services.database.databaseController
import com.oic.myapplication.services.database.models.DailyLog

import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.oic.myapplication.ui.palette.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Composable
fun ReportingScreen() {
    val dbController = remember { databaseController() }

    // state to hold the fetched logs
    var dailyLogs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch all logs ONCE when the screen loads
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val logs = dbController.getAllDailyLogs()  // <-- no callback here
            dailyLogs = logs
            Log.d("Firestore", "Fetched ${logs.size} logs")
        } catch (e: Exception) {
            error = e.message
            Log.e("Firestore", "Error fetching logs", e)
        } finally {
            isLoading = false
        }
    }



    // --- Live date/time (ticks every minute) ---
    val zone = ZoneId.systemDefault()
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now(zone)
            delay(60_000)
        }
    }

    val headerDateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    val headerTimeFmt = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val cardDateFmt   = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }

//    // Last 4 weekly dates: today, -1wk, -2wk, -3wk (newest first)
//    val days = remember(now.toLocalDate()) {
//        (0..3).map { w -> LocalDate.now(zone).minusWeeks(w.toLong()) }
//    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Latte)
            .systemBarsPadding()
    ) {
        HeaderReporting(
            title = "Reporting",
            dateLine = "${now.format(headerDateFmt)} • ${now.format(headerTimeFmt)}"
        )

        Spacer(Modifier.height(12.dp))

        dailyLogs
            .sortedByDescending { LocalDate.parse(it.date) } // latest date first
            .forEach { day ->
                val date = LocalDate.parse(day.date)
                val logs = day.logs

                Surface(
                    shape = CardXL,
                    color = SurfaceWhite,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "DATE: ${date.format(cardDateFmt)}",
                            color = CocoaDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Irrigation Logs:",
                            color = Cocoa.copy(alpha = .8f)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Loop through each irrigation log
                        logs.forEach { (timeKey, logEntry) ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text("Start: ${logEntry.startTime}", color = Cocoa)
                                Text("End: ${logEntry.endTime}", color = Cocoa)
                                Text("Zones: ${logEntry.zone.joinToString(", ")}", color = Cocoa)
                                Text("Litres: ${logEntry.litres}", color = Cocoa)
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = Cocoa.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }

    }
}

@Composable
private fun HeaderReporting(
    title: String,
    dateLine: String
) {
    Surface(color = Cocoa.copy(alpha = .30f)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(26.dp)
        ) {
            Text(
                title,
                color = SurfaceWhite,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                dateLine,
                color = SurfaceWhite.copy(alpha = .9f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    ReportingScreen()
}

