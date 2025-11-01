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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.ui.palette.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter

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

    // Formats
    val headerDateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    val headerTimeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }   // 24h
    val cardDateFmt   = remember { DateTimeFormatter.ofPattern("dd/MM/yy") } // dd/MM/yy for cards

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
            // Sort by derived LocalDate (timestamp preferred, fallback to date string)
            dailyLogs
                .sortedByDescending { logLocalDate(it) }
                .forEach { day ->
                    val date = logLocalDate(day)
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
                            // Sort entries by parsed start time (ascending). Ties fall back to the raw string.
                            logs.entries
                                .sortedWith(
                                    compareBy(
                                        { parseLocalTimeOrNull(it.value.startTime) ?: LocalTime.MIDNIGHT },
                                        { it.value.startTime ?: "" }
                                    )
                                )
                                .forEach { (_, logEntry) ->
                                    Column(Modifier.padding(vertical = 4.dp)) {
                                        Text("Start: ${to24h(logEntry.startTime)}", color = Cocoa)
                                        Text("End: ${to24h(logEntry.endTime)}", color = Cocoa)
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

/* -------- Helpers for new time/date handling -------- */

private val DISPLAY_DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")

/** Prefer Firestore timestamp; fallback to parsing DailyLog.date with common formats. */
private fun logLocalDate(log: DailyLog): LocalDate {
    log.timestamp?.let { ts ->
        return ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val raw = log.date
    val candidates = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,          // "2025-09-28"
        DateTimeFormatter.ofPattern("dd/MM/yy"),   // "28/09/25"
        DateTimeFormatter.ofPattern("dd/MM/yyyy")  // "28/09/2025"
    )
    for (fmt in candidates) {
        try { return LocalDate.parse(raw, fmt) } catch (_: Throwable) {}
    }
    // last resort
    return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
}

/** Render any stored time (12h or 24h, with/without seconds) as 24-hour 'HH:mm'. */
private fun to24h(timeStr: String?): String {
    if (timeStr.isNullOrBlank()) return ""
    val outs = DateTimeFormatter.ofPattern("HH:mm")
    val candidates = listOf(
        "HH:mm:ss", "HH:mm",
        "H:mm:ss", "H:mm",
        "h:mm:ss a", "hh:mm:ss a",
        "h:mm a", "hh:mm a"
    ).map { DateTimeFormatter.ofPattern(it) }

    for (fmt in candidates) {
        try {
            val t = LocalTime.parse(timeStr.trim(), fmt)
            return t.format(outs)
        } catch (_: Throwable) {}
    }
    // If nothing matched, just return the original (won't crash UI)
    return timeStr
}

private fun parseLocalTimeOrNull(raw: String?): LocalTime? {
    if (raw.isNullOrBlank()) return null
    val candidates = listOf(
        "HH:mm:ss", "HH:mm",
        "H:mm:ss",  "H:mm",
        "h:mm:ss a","hh:mm:ss a",
        "h:mm a",   "hh:mm a"
    ).map { DateTimeFormatter.ofPattern(it) }

    for (fmt in candidates) {
        try { return LocalTime.parse(raw.trim(), fmt) } catch (_: Throwable) {}
    }
    return null
}
