package com.oic.myapplication.ui.screens.scheduling

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ListenerRegistration
import com.oic.myapplication.R
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.ScheduleDay
import com.oic.myapplication.services.database.models.ScheduleEntry
import com.oic.myapplication.ui.palette.*
import com.oic.myapplication.ui.screens.IrrigationScheduleCard
import com.oic.myapplication.ui.screens.notifications.NotificationsRepo
import com.oic.myapplication.ui.screens.weather.WeatherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Scheduling page with View / Edit tabs.
 * - View: live list of schedule-day docs via Firestore snapshot listener
 * - Edit: IrrigationScheduleCard to create/update a day’s periods
 */
@Composable
fun SchedulingScreen(
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val zone = remember { ZoneId.systemDefault() }
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(zone); delay(60_000) } }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    val timeText = remember(now) { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) }

    // If you want weather elsewhere, you can keep/remove this VM.
    val LAT = 3.5952
    val LON = 98.6722
    val weatherVm: WeatherViewModel = viewModel()
    val weather by weatherVm.state.collectAsState()
    LaunchedEffect(Unit) { weatherVm.refresh(LAT, LON) }

    val unread by NotificationsRepo.unreadCount.collectAsState(initial = 0)
    val context = LocalContext.current
    val db = remember { DatabaseController() }

    // === UI state ===
    var selectedTab by rememberSaveable { mutableStateOf("View") } // "View" or "Edit"
    var isLoading by remember { mutableStateOf(true) }

    // Live schedules (ScheduleDay list from Firestore)
    var schedules by remember { mutableStateOf<List<ScheduleDay>>(emptyList()) }

    // Subscribe to all schedules when on the View tab
    DisposableEffect(selectedTab) {
        var reg: ListenerRegistration? = null
        if (selectedTab == "View") {
            isLoading = true
            reg = db.getAllSchedules { result ->
                result
                    .onSuccess {
                        schedules = it
                        isLoading = false
                    }
                    .onFailure { e ->
                        isLoading = false
                        Toast.makeText(context, "Error loading schedules: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        onDispose { reg?.remove() }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.oic6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xCC000000),
                        0.35f to Color(0x66000000),
                        0.7f to Color(0x22000000),
                        1f to Color(0x11000000)
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 26.dp, vertical = 16.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Scheduling", color = SurfaceWhite, fontWeight = FontWeight.SemiBold)
                            Box {
                                IconButton(onClick = onOpenNotifications) { Text("🔔", color = SurfaceWhite) }
                                if (unread > 0) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935))
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("${now.format(dateFmt)}  •  $timeText", color = SurfaceWhite.copy(alpha = 0.9f))
                    }
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(y = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // Tabs
                item {
                    Surface(
                        shape = CardXL,
                        color = SurfaceWhite.copy(alpha = 0.85f),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("View", "Edit").forEach { tab ->
                                val selected = tab == selectedTab
                                Button(
                                    onClick = { selectedTab = tab },
                                    shape = Pill,
                                    colors =
                                        if (selected)
                                            ButtonDefaults.buttonColors(
                                                containerColor = GoldDark,
                                                contentColor = SurfaceWhite
                                            )
                                        else
                                            ButtonDefaults.buttonColors(
                                                containerColor = Latte,
                                                contentColor = CocoaDeep
                                            ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tab, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                when (selectedTab) {
                    "View" -> {
                        when {
                            isLoading -> {
                                item {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) { CircularProgressIndicator() }
                                }
                            }
                            schedules.isEmpty() -> {
                                item {
                                    Text(
                                        "No schedules found.",
                                        color = SurfaceWhite.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                            else -> {
                                // One card per day doc
                                items(schedules, key = { it.day.ordinal }) { dayDoc ->
                                    ScheduleDayCard(dayDoc)
                                }
                                item { Spacer(Modifier.height(12.dp)) }
                            }
                        }
                    }
                    "Edit" -> {
                        item {
                            IrrigationScheduleCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                onSaved = {
                                    Toast.makeText(context, "✅ Schedule saved.", Toast.LENGTH_SHORT).show()
                                    // Snapshot listener will update View tab automatically.
                                    // If you want to flip back to View after save:
                                    // selectedTab = "View"
                                },
                                onError = { msg ->
                                    Toast.makeText(context, "❌ $msg", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Rendering of a single day document ---------- */

@Composable
private fun ScheduleDayCard(day: ScheduleDay) {
    Surface(
        shape = CardXL,
        color = SurfaceWhite,
        tonalElevation = 1.dp,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = day.day.displayName,
                color = CocoaDeep,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // List of label–entry pairs for each period
            val rows = listOf(
                "Morning" to day.morning,
                "Midday" to day.midday,
                "Afternoon" to day.afternoon
            )

            rows.forEachIndexed { index, (label, entry) ->
                val backgroundColor = SurfaceWhite.copy(alpha = 0.8f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: label
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        color = CocoaDeep,
                        modifier = Modifier.weight(0.35f)
                    )

                    // Column 2: entry details
                    if (entry == null) {
                        Text(
                            text = "—",
                            color = Cocoa.copy(alpha = 0.7f),
                            modifier = Modifier.weight(0.65f)
                        )
                    } else {
                        val enabledText = if (entry.enabled) "Enabled" else "Disabled"
                        Text(
                            text = "${entry.startTime}  •  ${entry.litres} L  •  $enabledText",
                            color = Cocoa.copy(alpha = 0.85f),
                            modifier = Modifier.weight(0.65f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PeriodRow(label: String, entry: ScheduleEntry?) {
    if (entry == null) {
        Text("• $label: —", color = Cocoa)
    } else {
        val enabledText = if (entry.enabled) "Enabled" else "Disabled"
        Text("• $label: ${entry.startTime} • ${entry.litres} L • $enabledText", color = Cocoa)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SchedulingScreenPreview() {
    SchedulingScreen(onBack = {}, onOpenNotifications = {})
}
