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
import com.oic.myapplication.services.database.models.ManualRunDoc
import com.oic.myapplication.services.database.models.ScheduleDay
import com.oic.myapplication.services.database.models.ScheduleEntry
import com.oic.myapplication.ui.palette.*
import com.google.firebase.firestore.ListenerRegistration
import com.oic.myapplication.ui.screens.notifications.NotificationsRepo   // ⬅️ notifications
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * - Reads/listens to the active manual run doc from Firestore.
 * - Remaining litres = f(now, startAt, totalLitres) so it survives navigation.
 * - Pushes small in-memory notifications for manual & scheduled state changes.
 */
@Composable
fun CurrentStatusCard(
    isIrrigating: Boolean,
    onToggle: (start: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val db = remember { DatabaseController() }

    // Live clock (30s tick)
    val zone = remember { ZoneId.systemDefault() }
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now(zone)
            delay(30_000)
        }
    }
    val time12 = remember(now) { now.format(DateTimeFormatter.ofPattern("h:mm a")) }

    // === 1) Load today's schedule ===
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

    // === 2) Real-time manual run state from DB ===
    var manualDoc by remember { mutableStateOf<ManualRunDoc?>(null) }

    // Instant updates
    DisposableEffect(Unit) {
        var registration: ListenerRegistration? = null
        try {
            registration = db.listenActiveManualRun { docOrNull ->
                manualDoc = docOrNull
            }
        } catch (_: Throwable) { /* listener optional */ }
        onDispose { registration?.remove() }
    }

    // === 3) Compute remaining litres from DB-backed manual run ===
    val manualRemaining: Int? = remember(manualDoc, now) {
        val d = manualDoc ?: return@remember null
        if (!d.running) return@remember null
        val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(d.startAtEpochMs), zone)
        remainingLitresAt(d.totalLitres, startedAt, now)
    }

    // === 4) Scheduled run detection (derived) ===
    val activeScheduled = remember(now, todayDoc) { findActiveScheduledRun(todayDoc, now) }
    val scheduledRunning = activeScheduled != null

    // Notify when scheduled state flips (start/stop)
    var prevScheduled by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(scheduledRunning) {
        val prev = prevScheduled
        prevScheduled = scheduledRunning
        if (prev == null) return@LaunchedEffect  // ignore first snapshot
        if (scheduledRunning && !prev) {
            // Started
            val litres = activeScheduled?.litres
            val msg = buildString {
                append("Started at $time12")
                if (litres != null) append(" • ${litres}L")
            }
            NotificationsRepo.pushStart(msg)
        } else if (!scheduledRunning && prev) {
            // Stopped
            NotificationsRepo.pushStop("Stopped at $time12")
        }
    }

    // === 5) UI selections ===
    var manualLitresPick by remember { mutableStateOf(20) }

    // === 6) Auto-stop guard based on DB state ===
    LaunchedEffect(manualRemaining) {
        val r = manualRemaining
        if (r != null && r <= 0) {
            db.endManualRun { /* optionally surface errors */ }
            NotificationsRepo.push("Manual irrigation finished", "Auto-stopped at $time12", danger = false)
            onToggle(false)
        }
    }

    val isManualRunning = manualDoc?.running == true
    val isRunning = isManualRunning || scheduledRunning
    val statusLabel = when {
        scheduledRunning -> "Running (Scheduled)"
        isManualRunning  -> "Running (Manual)"
        else             -> "Stopped"
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
            Text("Current Status", color = Cocoa.copy(alpha = .85f), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

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

            // Remaining litres row (manual > scheduled)
            val displayRemaining = manualRemaining ?: run {
                if (scheduledRunning) {
                    val (start, litres) = activeScheduled!!
                    remainingLitresAt(litres, start, now)
                } else null
            }

            if (displayRemaining != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remaining litres", color = Cocoa, fontWeight = FontWeight.SemiBold)
                    Surface(shape = Pill, color = SurfaceWhite.copy(alpha = 0.9f), shadowElevation = 0.dp) {
                        Text(
                            "${max(0, displayRemaining)} L",
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
            Text("Manual run litres", color = Cocoa, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 5
            ) {
                listOf(10, 20, 30, 40, 50).forEach { n ->
                    val sel = n == manualLitresPick
                    FilledTonalButton(
                        onClick = { manualLitresPick = n },
                        enabled = !scheduledRunning && !isManualRunning,
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

            Button(
                onClick = {
                    if (isManualRunning) {
                        db.endManualRun { /* optionally errors */ }
                        NotificationsRepo.push("Manual irrigation stopped", "Stopped at $time12")
                        onToggle(false)
                    } else {
                        db.beginManualRun(manualLitresPick) { /* optionally errors */ }
                        NotificationsRepo.push("Manual irrigation started", "Started at $time12 • ${manualLitresPick}L")
                        onToggle(true)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CardXL,
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isManualRunning) Latte else GoldDark,
                    contentColor = if (isManualRunning) CocoaDeep else SurfaceWhite
                )
            ) {
                Text(
                    if (isManualRunning) "Stop manual irrigation" else "Start manual irrigation",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* ---------------- Helpers & types ---------------- */

private const val FLOW_LPM = 27.0 // litres per minute

/** Flow model: ~27 L/min, minimum 5 minutes. */
private fun runtimeMinutes(litres: Int): Int = max(5, ceil(litres / FLOW_LPM).toInt())

/** Remaining litres (floored to int, never below 0). */
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

/** Returns the currently active scheduled run (if any) with its start time and litres. */
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
