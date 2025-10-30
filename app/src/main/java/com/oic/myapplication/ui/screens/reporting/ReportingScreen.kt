package com.oic.myapplication.ui.screens.reporting

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.dummyDatasets.populateDatabaseFromAssets
import com.oic.myapplication.services.database.models.DailyLog
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.oic.myapplication.ui.palette.*

@Composable
fun ReportingScreen() {

    val dbController = remember { DatabaseController() }
    var dailyLogs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Toggle: true = show chart, false = show daily logs
    var showChart by remember { mutableStateOf(true) }

    // Fetch logs once
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            dailyLogs = dbController.getAllDailyLogs()
            Log.d("Firestore", "Fetched ${dailyLogs.size} logs")
        } catch (e: Exception) {
            error = e.message
            Log.e("Firestore", "Error fetching logs", e)
        } finally {
            isLoading = false
        }
    }

    // Live date/time
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
    val cardDateFmt = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }

    // Weekly litres aggregation
    val weeklyLitres = remember(dailyLogs) {
        val weekMap = mutableMapOf<LocalDate, Int>()
        dailyLogs.forEach { day ->
            val date = LocalDate.parse(day.date)
            val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
            val total = day.logs.values.sumOf { it.litres }
            weekMap[weekStart] = (weekMap[weekStart] ?: 0) + total
        }
        weekMap.toSortedMap()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Latte)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderReporting(
            title = "Reporting",
            dateLine = "${now.format(headerDateFmt)} • ${now.format(headerTimeFmt)}"
        )

        // Toggle buttons
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showChart = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (showChart) GoldDark else SurfaceWhite)
            ) {
                Text("Water Usage Chart", color = if (showChart) SurfaceWhite else Cocoa)
            }
            Button(
                onClick = { showChart = false },
                colors = ButtonDefaults.buttonColors(containerColor = if (!showChart) GoldDark else SurfaceWhite)
            ) {
                Text("Daily Logs", color = if (!showChart) SurfaceWhite else Cocoa)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (showChart) {
            if (dailyLogs.isNotEmpty()) {
                Charts.WeeklyLitresChart(dailyLogs)
                Charts.MonthlyLitresChart(dailyLogs)
            } else {
                Text(
                    "No data for chart",
                    modifier = Modifier.padding(20.dp),
                    color = CocoaDeep
                )
            }
        } else {
            dailyLogs.sortedByDescending { LocalDate.parse(it.date) }.forEach { day ->
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
                        Text("Irrigation Logs:", color = Cocoa.copy(alpha = .8f))
                        Spacer(Modifier.height(8.dp))
                        logs.forEach { (_, logEntry) ->
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
}

@Composable
private fun HeaderReporting(title: String, dateLine: String) {
    Surface(color = Cocoa.copy(alpha = .30f)) {
        Column(Modifier.fillMaxWidth().padding(26.dp)) {
            Text(title, color = SurfaceWhite, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(dateLine, color = SurfaceWhite.copy(alpha = .9f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    ReportingScreen()
}
