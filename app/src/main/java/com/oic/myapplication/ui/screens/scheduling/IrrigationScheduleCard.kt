package com.oic.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.Day
import com.oic.myapplication.services.database.models.Period
import com.oic.myapplication.services.database.models.ScheduleEntry
import com.oic.myapplication.ui.palette.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationScheduleCard(
    modifier: Modifier = Modifier,
    onSaved: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val db = remember { DatabaseController() }

    // Day dropdown state
    var selectedDay by rememberSaveable { mutableStateOf(Day.MONDAY) }
    var dayExpanded by remember { mutableStateOf(false) }

    // Time options
    val morningTimes = remember { listOf("6:00 AM","6:15 AM","6:30 AM","6:45 AM","7:00 AM","7:15 AM","7:30 AM","7:45 AM","8:00 AM") }
    val middayTimes  = remember { listOf("12:00 PM","12:15 PM","12:30 PM","12:45 PM","1:00 PM") }
    val afternoonTimes = remember { listOf("4:00 PM","4:15 PM","4:30 PM","4:45 PM","5:00 PM") }

    // Morning block
    var morningEnabled by rememberSaveable { mutableStateOf(true) }
    var morningTime by rememberSaveable { mutableStateOf("7:00 AM") }
    var morningLitres by rememberSaveable { mutableStateOf(20) }
    var morningTimeExpanded by remember { mutableStateOf(false) }

    // Midday block
    var middayEnabled by rememberSaveable { mutableStateOf(false) }
    var middayTime by rememberSaveable { mutableStateOf("12:00 PM") }
    var middayLitres by rememberSaveable { mutableStateOf(20) }
    var middayTimeExpanded by remember { mutableStateOf(false) }

    // Afternoon block
    var afternoonEnabled by rememberSaveable { mutableStateOf(false) }
    var afternoonTime by rememberSaveable { mutableStateOf("4:00 PM") }
    var afternoonLitres by rememberSaveable { mutableStateOf(20) }
    var afternoonTimeExpanded by remember { mutableStateOf(false) }

    // UI state
    var loading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    // Load existing day from DB
    LaunchedEffect(selectedDay) {
        loading = true; status = null
        db.getScheduleDay(selectedDay) { res ->
            loading = false
            res.onFailure { status = "Failed to load ${selectedDay.displayName}: ${it.message}" }
                .onSuccess { day ->
                    // defaults
                    morningEnabled = false; morningTime = "7:00 AM"; morningLitres = 20
                    middayEnabled = false;  middayTime  = "12:00 PM"; middayLitres  = 20
                    afternoonEnabled = false; afternoonTime = "4:00 PM"; afternoonLitres = 20
                    // apply existing
                    day?.morning?.let   { se -> morningEnabled   = true; morningTime   = se.startTime; morningLitres   = se.litres }
                    day?.midday?.let    { se -> middayEnabled    = true; middayTime    = se.startTime; middayLitres    = se.litres }
                    day?.afternoon?.let { se -> afternoonEnabled = true; afternoonTime = se.startTime; afternoonLitres = se.litres }
                }
        }
    }

    Surface(shape = CardXL, color = SurfaceWhite, tonalElevation = 2.dp, shadowElevation = 2.dp, modifier = modifier) {
        Column(Modifier.clip(CardXL).padding(18.dp)) {
            Text("Irrigation Schedule", color = Cocoa.copy(alpha = .85f), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Pick a day and configure morning/midday/afternoon runs.", color = Cocoa.copy(alpha = .65f), fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            // Day of week selector
            ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = selectedDay.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Day of week") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    )
                )
                DropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                    Day.entries.forEach { d ->
                        DropdownMenuItem(text = { Text(d.displayName) }, onClick = { selectedDay = d; dayExpanded = false })
                    }
                }
            }

            if (loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            status?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Cocoa.copy(alpha = .7f), fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Period sections
            PeriodSection(
                title = "Morning",
                enabled = morningEnabled,
                onEnabledChange = { morningEnabled = it },
                time = morningTime,
                onTimeChange = { morningTime = it },
                times = morningTimes,
                timeExpanded = morningTimeExpanded,
                onTimeExpandedChange = { morningTimeExpanded = it },
                litres = morningLitres,
                onLitresChange = { morningLitres = it }
            )

            Spacer(Modifier.height(12.dp))

            PeriodSection(
                title = "Midday",
                enabled = middayEnabled,
                onEnabledChange = { middayEnabled = it },
                time = middayTime,
                onTimeChange = { middayTime = it },
                times = middayTimes,
                timeExpanded = middayTimeExpanded,
                onTimeExpandedChange = { middayTimeExpanded = it },
                litres = middayLitres,
                onLitresChange = { middayLitres = it }
            )

            Spacer(Modifier.height(12.dp))

            PeriodSection(
                title = "Afternoon",
                enabled = afternoonEnabled,
                onEnabledChange = { afternoonEnabled = it },
                time = afternoonTime,
                onTimeChange = { afternoonTime = it },
                times = afternoonTimes,
                timeExpanded = afternoonTimeExpanded,
                onTimeExpandedChange = { afternoonTimeExpanded = it },
                litres = afternoonLitres,
                onLitresChange = { afternoonLitres = it }
            )

            Spacer(Modifier.height(16.dp))

            // Save: delete disabled periods + upsert enabled periods
            Button(
                onClick = {
                    val anyEnabled = morningEnabled || middayEnabled || afternoonEnabled
                    val anyDisabled = !morningEnabled || !middayEnabled || !afternoonEnabled
                    if (!anyEnabled && !anyDisabled) {
                        onError("Nothing to save.")
                        return@Button
                    }

                    saving = true; status = null

                    // Build entries for enabled ones
                    val morningEntry = if (morningEnabled) ScheduleEntry(startTime = morningTime, litres = morningLitres, enabled = true, zone = emptyList()) else null
                    val middayEntry  = if (middayEnabled)  ScheduleEntry(startTime = middayTime,  litres = middayLitres,  enabled = true, zone = emptyList()) else null
                    val afternoonEntry = if (afternoonEnabled) ScheduleEntry(startTime = afternoonTime, litres = afternoonLitres, enabled = true, zone = emptyList()) else null

                    // We will:
                    // 1) delete disabled periods
                    // 2) upsert all enabled periods in one call (merge)
                    var pending = 0
                    var failed: Throwable? = null
                    fun doneOne() {
                        pending -= 1
                        if (pending == 0) {
                            saving = false
                            if (failed == null) {
                                status = "Saved ${selectedDay.displayName} schedule."
                                onSaved()
                            } else {
                                status = "Save failed: ${failed?.message}"
                                onError(failed?.message ?: "Save failed")
                            }
                        }
                    }

                    // Count pending ops
                    if (!morningEnabled) pending++
                    if (!middayEnabled) pending++
                    if (!afternoonEnabled) pending++
                    // Always run an upsert (it only writes non-null periods)
                    pending++

                    // 1) Deletes
                    if (!morningEnabled) {
                        db.deleteSchedulePeriod(selectedDay, Period.MORNING) { r ->
                            r.onFailure { failed = it }; doneOne()
                        }
                    }
                    if (!middayEnabled) {
                        db.deleteSchedulePeriod(selectedDay, Period.MIDDAY) { r ->
                            r.onFailure { failed = it }; doneOne()
                        }
                    }
                    if (!afternoonEnabled) {
                        db.deleteSchedulePeriod(selectedDay, Period.AFTERNOON) { r ->
                            r.onFailure { failed = it }; doneOne()
                        }
                    }

                    // 2) Upsert enabled
                    db.upsertDay(
                        day = selectedDay,
                        morning = morningEntry,
                        midday = middayEntry,
                        afternoon = afternoonEntry
                    ) { r ->
                        r.onFailure { failed = it }
                        doneOne()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CardXL,
                enabled = !saving && !loading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = SurfaceWhite)
            ) {
                if (saving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (saving) "Saving…" else "Save schedule(s)", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/* --------------------------------- Components --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSection(
    title: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    times: List<String>,
    timeExpanded: Boolean,
    onTimeExpandedChange: (Boolean) -> Unit,
    litres: Int,
    onLitresChange: (Int) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title, color = CocoaDeep, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", color = Cocoa)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SurfaceWhite,
                        checkedTrackColor = GoldDark,     // match selected litres
                        uncheckedThumbColor = SurfaceWhite,
                        uncheckedTrackColor = Latte,      // match unselected litres
                        uncheckedBorderColor = CocoaDeep.copy(alpha = 0.4f),
                        checkedBorderColor = GoldDark
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = timeExpanded,
            onExpandedChange = { onTimeExpandedChange(!timeExpanded) }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = time,
                onValueChange = {},
                readOnly = true,
                label = { Text("Start time") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                enabled = enabled
            )
            DropdownMenu(
                expanded = timeExpanded,
                onDismissRequest = { onTimeExpandedChange(false) }
            ) {
                times.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onTimeChange(opt); onTimeExpandedChange(false) },
                        enabled = enabled
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Litres", color = Cocoa, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 5
        ) {
            listOf(10, 20, 30, 40, 50).forEach { n ->
                val sel = n == litres
                FilledTonalButton(
                    onClick = { onLitresChange(n) },
                    shape = Pill,
                    enabled = enabled,
                    colors = if (sel)
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = GoldDark,
                            contentColor = SurfaceWhite
                        )
                    else
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = Latte,
                            contentColor = CocoaDeep
                        )
                ) {
                    Text(
                        "$n L",
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
