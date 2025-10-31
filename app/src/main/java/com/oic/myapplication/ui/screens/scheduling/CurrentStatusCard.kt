package com.oic.myapplication.ui.screens.scheduling

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.Day
import com.oic.myapplication.services.database.models.ScheduleDay
import com.oic.myapplication.services.database.models.ScheduleEntry
import com.oic.myapplication.ui.palette.CardXL
import com.oic.myapplication.ui.palette.Cocoa
import com.oic.myapplication.ui.palette.CocoaDeep
import com.oic.myapplication.ui.palette.GoldDark
import com.oic.myapplication.ui.palette.Latte
import com.oic.myapplication.ui.palette.Pill
import com.oic.myapplication.ui.palette.SurfaceWhite
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * Current status card that:
 *  - Shows whether irrigation is running (manual or scheduled)
 *  - Lets user manually Start/Stop via [onToggle]
 *  - Auto-detects "scheduled running" by checking today's schedule and current time.
 *  - Auto-stops manual runs based on the flow model (27 L/min, min 5 min).
 *  - Displays the remaining litres while a run is active and stops manual when it reaches 0.
 *
 * @param isIrrigating Whether manual irrigation is currently running (from your state holder).
 * @param onToggle Called when Start/Stop is pressed (true = start, false = stop).
 */
@Composable
fun CurrentStatusCard(
    isIrrigating: Boolean,
    onToggle: (start: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val db = remember { DatabaseController() }
    val scope = rememberCoroutineScope()

    // Live clock (30s tick)
    val zone = remember { ZoneId.systemDefault() }
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now(zone)
            delay(30_000)
        }
    }

    // Load today's schedule doc
    var todayDoc by remember { mutableStateOf<ScheduleDay?>(null) }
    var loading by remember { mutableStateOf(true) }
    var lastErr by remember { mutableStateOf<String?>(null) }

    val todayEnum = remember(now) {
        when (now.dayOfWeek) {
            DayOfWeek.MONDAY -> Day.MONDAY
            DayOfWeek.TUESDAY -> Day.TUESDAY
            DayOfWeek.WEDNESDAY -> Day.WEDNESDAY
            DayOfWeek.THURSDAY -> Day.THURSDAY
            DayOfWeek.FRIDAY -> Day.FRIDAY
            DayOfWeek.SATURDAY -> Day.SATURDAY
            DayOfWeek.SUNDAY -> Day.SUNDAY
        }
    }

    LaunchedEffect(todayEnum) {
        loading = true; lastErr = null
        db.getScheduleDay(todayEnum) { res ->
            res.onSuccess { doc -> todayDoc = doc; loading = false }
                .onFailure { e -> lastErr = e.message; todayDoc = null; loading = false }
        }
    }

    // Detect scheduled run "now" and identify active period if any
    val activeScheduled by remember(now, todayDoc) {
        mutableStateOf(findActiveScheduledRun(todayDoc, now))
    }
    val scheduledRunning = activeScheduled != null

    // Manual litres quick-pick (used for manual auto-stop & log)
    var manualLitres by remember { mutableStateOf(20) }

    // Track manual start to compute remaining litres locally
    var manualStartAt by remember { mutableStateOf<LocalDateTime?>(null) }
    var manualTotalLitres by remember { mutableStateOf(0) }

    // Auto-stop timer for manual runs
    var manualJob by remember { mutableStateOf<Job?>(null) }

    val isRunning = isIrrigating || scheduledRunning
    val statusLabel = when {
        scheduledRunning -> "Running (Scheduled)"
        isIrrigating     -> "Running (Manual)"
        else             -> "Stopped"
    }

    // Compute remaining litres for the active run (manual > scheduled > none)
    val remainingLitres: Int? = when {
        isIrrigating && manualStartAt != null -> {
            remainingLitresAt(
                totalLitres = manualTotalLitres,
                startedAt = manualStartAt!!,
                now = now
            )
        }
        scheduledRunning -> {
            val (start, litres) = activeScheduled!!
            remainingLitresAt(
                totalLitres = litres,
                startedAt = start,
                now = now
            )
        }
        else -> null
    }

    // Guard to stop manual instantly when remaining litres hit 0 (even if timer drifted/app resumed)
    var autoStopping by remember { mutableStateOf(false) }
    LaunchedEffect(remainingLitres, isIrrigating) {
        if (!autoStopping &&
            isIrrigating &&
            manualStartAt != null &&
            remainingLitres != null &&
            remainingLitres <= 0
        ) {
            autoStopping = true
            manualJob?.cancel()
            manualJob = null
            manualStartAt = null
            manualTotalLitres = 0
            db.endManualRun { /* optionally toast/log errors */ }
            onToggle(false)
            autoStopping = false
        }
    }

    val chipBg = if (isRunning) Latte else SurfaceWhite.copy(alpha = 0.85f)
    val chipFg = if (isRunning) CocoaDeep else Cocoa

    Surface(
        shape = CardXL,
        color = SurfaceWhite,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "Current Status",
                color = Cocoa.copy(alpha = .85f),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // Status row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Irrigation", color = Cocoa, fontWeight = FontWeight.SemiBold)
                Surface(shape = Pill, color = chipBg, shadowElevation = 0.dp) {
                    Text(
                        statusLabel,
                        color = chipFg,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Remaining litres row (only when running)
            if (remainingLitres != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remaining litres", color = Cocoa, fontWeight = FontWeight.SemiBold)
                    Surface(shape = Pill, color = SurfaceWhite.copy(alpha = 0.9f), shadowElevation = 0.dp) {
                        Text(
                            "${max(0, remainingLitres)} L",
                            color = CocoaDeep,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (loading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (lastErr != null) {
                Spacer(Modifier.height(8.dp))
                Text("Schedule load error: $lastErr", color = Cocoa)
            }

            Spacer(Modifier.height(12.dp))

            // Manual litres quick-pick (disabled while a scheduled run is active)
            Text("Manual run litres", color = Cocoa, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 5
            ) {
                listOf(10, 20, 30, 40, 50).forEach { n ->
                    val sel = n == manualLitres
                    FilledTonalButton(
                        onClick = { manualLitres = n },
                        enabled = !scheduledRunning,
                        shape = Pill,
                        colors = if (sel)
                            ButtonDefaults.filledTonalButtonColors(containerColor = GoldDark, contentColor = SurfaceWhite)
                        else
                            ButtonDefaults.filledTonalButtonColors(containerColor = Latte, contentColor = CocoaDeep)
                    ) {
                        Text("$n L", fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            // Start / Stop manual with auto-stop according to flow model
            Button(
                onClick = {
                    if (isIrrigating) {
                        // Stop manual early
                        manualJob?.cancel()
                        manualJob = null
                        manualStartAt = null
                        manualTotalLitres = 0
                        db.endManualRun { /* optionally surface errors */ }
                        onToggle(false)
                    } else {
                        // Start manual
                        val mins = runtimeMinutes(manualLitres)
                        db.beginManualRun(manualLitres) { /* optionally surface errors */ }
                        onToggle(true)

                        manualStartAt = now
                        manualTotalLitres = manualLitres

                        // restart timer if existed
                        manualJob?.cancel()
                        manualJob = scope.launch {
                            delay(mins * 60_000L)
                            db.endManualRun { /* optionally surface errors */ }
                            manualStartAt = null
                            manualTotalLitres = 0
                            onToggle(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CardXL,
                enabled = !loading, // still allow if schedule failed to load
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIrrigating) Latte else GoldDark,
                    contentColor = if (isIrrigating) CocoaDeep else SurfaceWhite
                )
            ) {
                Text(
                    if (isIrrigating) "Stop manual irrigation" else "Start manual irrigation",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* ---------------- Helpers ---------------- */

private const val FLOW_LPM = 27.0 // litres per minute

/** Flow model: ~27 L/min, minimum 5 minutes. According to the Engineering team*/
private fun runtimeMinutes(litres: Int): Int = max(5, ceil(litres / FLOW_LPM).toInt())

/** Remaining litres given a start time and total litres at 27 L/min (floored to int, never below 0). */
private fun remainingLitresAt(
    totalLitres: Int,
    startedAt: LocalDateTime,
    now: LocalDateTime
): Int {
    val elapsedMin = max(0.0, Duration.between(startedAt, now).toMillis() / 60_000.0)
    val used = floor(elapsedMin * FLOW_LPM).toInt()
    return max(0, totalLitres - used)
}

private data class ActiveRun(val start: LocalDateTime, val litres: Int)

/**
 * Returns the currently active scheduled run (if any) with its start time and litres.
 * Active = now ∈ [startTime, startTime + runtime(litres)].
 */
private fun findActiveScheduledRun(
    dayDoc: ScheduleDay?,
    now: LocalDateTime
): ActiveRun? {
    if (dayDoc == null) return null
    val fmt = DateTimeFormatter.ofPattern("h:mm a") // e.g. "7:00 AM"

    fun active(entry: ScheduleEntry?): ActiveRun? {
        if (entry == null || !entry.enabled) return null
        if (entry.startTime.isBlank()) return null
        return try {
            val startLocalTime = LocalTime.parse(entry.startTime.uppercase(), fmt)
            val start = LocalDateTime.of(now.toLocalDate(), startLocalTime)
            val minutes = runtimeMinutes(entry.litres)
            val end = start.plusMinutes(minutes.toLong())
            if (!now.isBefore(start) && now.isBefore(end)) ActiveRun(start, entry.litres) else null
        } catch (_: Throwable) {
            null
        }
    }

    return active(dayDoc.morning) ?: active(dayDoc.midday) ?: active(dayDoc.afternoon)
}

/** Returns true if any enabled scheduled period is currently active. */
@Suppress("unused")
private fun isAnyPeriodRunningNow(
    dayDoc: ScheduleDay?,
    now: LocalDateTime
): Boolean = findActiveScheduledRun(dayDoc, now) != null
