package com.oic.myapplication.ui.screens.scheduling

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oic.myapplication.R
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.IrrigationLog
import com.oic.myapplication.ui.palette.*
import com.oic.myapplication.ui.screens.notifications.NotificationsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

import com.oic.myapplication.ui.screens.weather.WeatherViewModel
import java.time.LocalDate

/* ---------------- Model ---------------- */

private enum class Period { Morning, Midday, Afternoon }

/* tiny UI holder for the heads-up bubble */
private data class NotificationEventUi(val title: String, val message: String, val danger: Boolean)

/* ---------------- Screen ---------------- */

@Composable
fun SchedulingScreen(
    onOpenReporting: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    // Live date/time (minute tick)
    val zone = ZoneId.systemDefault()
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) {
        while (true) { now = LocalDateTime.now(zone); delay(60_000) }
    }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    // Safe-on-all-API time text
    val timeText = remember(now) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    // Slot selection
    var selectedPeriod by rememberSaveable { mutableStateOf(Period.Morning) }

    // Time windows & state
    val morningOpts = remember { windowOptions("7:00 AM", "8:00 AM") }
    val middayOpts  = remember { windowOptions("12:00 PM", "1:00 PM") }
    val afternoonOpts = remember { windowOptions("4:00 PM", "5:00 PM") }

    var morningStart by remember { mutableStateOf(morningOpts.first()) }
    var morningEnd   by remember { mutableStateOf(morningOpts.last()) }
    var middayStart  by remember { mutableStateOf(middayOpts.first()) }
    var middayEnd    by remember { mutableStateOf(middayOpts.last()) }
    var afternoonStart by remember { mutableStateOf(afternoonOpts.first()) }
    var afternoonEnd   by remember { mutableStateOf(afternoonOpts.last()) }

    // Litres per slot
    var morningLitres by remember { mutableStateOf(20) }
    var middayLitres by remember { mutableStateOf(20) }
    var afternoonLitres by remember { mutableStateOf(20) }

    // Irrigation running state (persist across navigation via shared repo)
    val isIrrigating by IrrigationRepo.isRunning.collectAsState()
    var showSplash by remember { mutableStateOf(false) } // water confetti

    // Logging
    val morningLog = IrrigationLog(
        startTime = morningStart,
        endTime = morningEnd,
        scheduled = true,
        litres = morningLitres,
        zone = listOf("0")
    )

    val middayLog = IrrigationLog(
        startTime = middayStart,
        endTime = middayEnd,
        scheduled = true,
        litres = middayLitres,
        zone = listOf("0")
    )

    val afternoonLog = IrrigationLog(
        startTime = afternoonStart,
        endTime = afternoonEnd,
        scheduled = true,
        litres = afternoonLitres,
        zone = listOf("0")
    )

    val logMap: Map<String, IrrigationLog> = mapOf(
        morningLog.startTime to morningLog,
        middayLog.startTime to middayLog,
        afternoonLog.startTime to afternoonLog
    )

    val todayDate = LocalDate.now().toString() // "2025-10-14"
    val dailyLog = DailyLog(
        date = todayDate,
        logs = logMap
    )

    val dbController = DatabaseController()



    // Weather (Medan)
    val LAT = 3.5952
    val LON = 98.6722
    val weatherVm: WeatherViewModel = viewModel()
    val weatherState by weatherVm.state.collectAsState()
    LaunchedEffect(Unit) { weatherVm.refresh(LAT, LON) }

    val safeToWater =
        (weatherState.temperatureC ?: 25) in 10..34 &&
                (weatherState.humidityPct ?: 60) < 80 &&
                (weatherState.windKmh ?: 10) < 25

    // Bell badge
    val unread by NotificationsRepo.unreadCount.collectAsState(initial = 0)

    // Heads-up bubble state (must be nullable -> start as null)
    var headUp by remember { mutableStateOf<NotificationEventUi?>(null) }

    // ======= Background image + scrim =======
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
                        0f to Color(0xCC000000),  // darker at top for readability
                        0.35f to Color(0x66000000),
                        0.7f to Color(0x22000000),
                        1f to Color(0x11000000)
                    )
                )
        )

        // ======= Foreground UI =======
        Scaffold(
            containerColor = Color.Transparent,
            // 🔧 Let the parent Scaffold own insets (so bottom nav stays visible)
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 22.dp, start = 26.dp, end = 26.dp, bottom = 16.dp)
                ) {
                    Column {
                        Text(
                            "Selamat Datang, Bang Nanda 😊",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SurfaceWhite
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${now.format(dateFmt)}  •  $timeText",
                            color = SurfaceWhite.copy(alpha = 0.9f)
                        )
                    }
                    // bell + badge
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(onClick = onOpenNotifications) {
                            Text("🔔", fontSize = 26.sp, lineHeight = 26.sp)
                        }
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
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),                // keep only innerPadding
                // ⬇️ No windowInsetsPadding here
                contentPadding = PaddingValues(bottom = 24.dp) // no extra +72.dp
            ) {

                /* ---------------- Current Irrigation ---------------- */
                item {
                    Surface(
                        shape = CardXL,
                        color = SurfaceWhite,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CardXL)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(SurfaceWhite, SurfaceWhite.copy(alpha = 0.96f))
                                    )
                                )
                        ) {
                            /* IRRIGATION INPUT FIELDS */
                            Column(Modifier.padding(18.dp)) {
                                Text("Current Irrigation Status", color = Cocoa.copy(alpha = .85f), fontSize = 20.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Location: Permaculture Zone", color = Cocoa.copy(alpha = .65f), fontSize = 14.sp)

                                Spacer(Modifier.height(16.dp))

                                PeriodChips(selected = selectedPeriod, onSelect = { selectedPeriod = it })

                                Spacer(Modifier.height(12.dp))

                                when (selectedPeriod) {
                                    Period.Morning -> SlotEditor(
                                        title = "Morning (7–8 AM)",
                                        start = morningStart,
                                        onStart = { s -> morningStart = s; morningEnd = clampEndAfter(s, morningEnd, morningOpts) },
                                        end = morningEnd,
                                        onEnd = { morningEnd = it },
                                        allOptions = morningOpts,
                                        litres = morningLitres,
                                        onLitres = { morningLitres = it }
                                    )
                                    Period.Midday -> SlotEditor(
                                        title = "Midday (12–1 PM)",
                                        start = middayStart,
                                        onStart = { s -> middayStart = s; middayEnd = clampEndAfter(s, middayEnd, middayOpts) },
                                        end = middayEnd,
                                        onEnd = { middayEnd = it },
                                        allOptions = middayOpts,
                                        litres = middayLitres,
                                        onLitres = { middayLitres = it }
                                    )
                                    Period.Afternoon -> SlotEditor(
                                        title = "Afternoon (4–5 PM)",
                                        start = afternoonStart,
                                        onStart = { s -> afternoonStart = s; afternoonEnd = clampEndAfter(s, afternoonEnd, afternoonOpts) },
                                        end = afternoonEnd,
                                        onEnd = { afternoonEnd = it },
                                        allOptions = afternoonOpts,
                                        litres = afternoonLitres,
                                        onLitres = { afternoonLitres = it }
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                val startStopLabel = if (isIrrigating) "Stop irrigation" else "Start irrigation"
                                FilledWideButton(text = startStopLabel) {
                                    val nowText = timeText
                                    if (isIrrigating) {
                                        IrrigationRepo.stop() // persist across navigation
                                        NotificationsRepo.pushStop(nowText)
                                        headUp = NotificationEventUi(
                                            title = "Stopped",
                                            message = "Automated watering • $nowText",
                                            danger = false
                                        )
                                        showSplash = false
                                    } else {
                                        IrrigationRepo.start() // persist across navigation
                                        dbController.createDailyLog(dailyLog)
                                        showSplash = true
                                        NotificationsRepo.pushStart(nowText)
                                        headUp = NotificationEventUi(
                                            title = "Started",
                                            message = "Automated watering • $nowText",
                                            danger = false
                                        )
                                    }
                                }
                            }

                            // 💦 Confetti overlay
                            if (showSplash) {
                                WaterConfettiOverlay(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CardXL)
                                ) { showSplash = false }
                            }
                        }
                    }
                }

                /* ---------------- Weather & Humidity ---------------- */
                item {
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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = weatherState.temperatureC?.let { "$it°" } ?: "—",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CocoaDeep
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatPill("Temp", weatherState.temperatureC?.let { "$it°C" } ?: "—", minWidth = 120.dp)
                                StatPill("Humidity", weatherState.humidityPct?.let { "$it%" } ?: "—", minWidth = 120.dp)
                                StatPill("Wind", weatherState.windKmh?.let { "$it km/h" } ?: "—", minWidth = 120.dp)
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilledTonalButton(
                                    onClick = { weatherVm.refresh(LAT, LON) },
                                    shape = Pill,
                                    enabled = !weatherState.loading,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = SurfaceWhite.copy(alpha = 0.85f),
                                        contentColor = CocoaDeep
                                    )
                                ) {
                                    if (weatherState.loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = CocoaDeep
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(if (weatherState.loading) "Refreshing…" else "Refresh")
                                }

                                OutlinedButton(onClick = { /* TODO: GPS later */ }, shape = Pill) {
                                    Text("Use device location")
                                }
                            }

                            if (weatherState.error != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Couldn’t update weather: ${weatherState.error}",
                                    color = Cocoa.copy(alpha = .75f),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (safeToWater) "✅ It is safe to water today." else "⚠️ Not ideal to water now.",
                                color = if (safeToWater) LeafGreen else CocoaDeep,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                /* ---------------- Recent Activity ---------------- */
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(GoldLight),
                            contentAlignment = Alignment.Center
                        ) { Text("▢") }
                        Spacer(Modifier.width(8.dp))
                        Text("Recent Activity", fontWeight = FontWeight.SemiBold, color = SurfaceWhite, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                }
                item {
                    ActivityTile(
                        "Report",
                        "Most recent general report (placeholder) — will be fetched from the database."
                    ) { onOpenReporting() }
                    Spacer(Modifier.height(18.dp))
                }
            }
        }

        // ===== Heads-up bubble overlay on top of everything =====
        headUp?.let { data ->
            HeadsUpBubble(
                title = data.title,
                message = data.message,
                danger = data.danger,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) { headUp = null }
        }
    }
}

/* ---------------- Pieces ---------------- */

@Composable
private fun PeriodChips(
    selected: Period,
    onSelect: (Period) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(Period.Morning to "Morning", Period.Midday to "Midday", Period.Afternoon to "Afternoon").forEach { (p, label) ->
            val isSel = p == selected
            FilledTonalButton(
                onClick = { onSelect(p) },
                shape = Pill,
                modifier = Modifier.weight(1f),
                colors = if (isSel)
                    ButtonDefaults.filledTonalButtonColors(containerColor = GoldDark, contentColor = SurfaceWhite)
                else
                    ButtonDefaults.filledTonalButtonColors(containerColor = Latte, contentColor = CocoaDeep)
            ) {
                Text(
                    label,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SlotEditor(
    title: String,
    start: String,
    onStart: (String) -> Unit,
    end: String,
    onEnd: (String) -> Unit,
    allOptions: List<String>,
    litres: Int,
    onLitres: (Int) -> Unit
) {
    Text(title, color = CocoaDeep, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    val startIndex = allOptions.indexOf(start).coerceAtLeast(0)
    val endOptions = allOptions.drop(startIndex + 1).ifEmpty { listOf(allOptions.last()) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            DropdownField("Start", start, onStart, allOptions, enabled = true)
        }
        Box(Modifier.weight(1f)) {
            DropdownField("End", end.coerceInOptions(endOptions), onEnd, endOptions, enabled = true)
        }
    }

    Spacer(Modifier.height(12.dp))
    Text("Litres for this run", color = Cocoa.copy(alpha = .85f), fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 3
    ) {
        listOf(10, 20, 30, 40, 50).forEach { n ->
            val sel = n == litres
            FilledTonalButton(
                onClick = { onLitres(n) },
                shape = Pill,
                colors = if (sel)
                    ButtonDefaults.filledTonalButtonColors(containerColor = GoldDark, contentColor = SurfaceWhite)
                else
                    ButtonDefaults.filledTonalButtonColors(containerColor = Latte, contentColor = CocoaDeep)
            ) { Text("$n L", fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, enabled),
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                disabledContainerColor = SurfaceWhite.copy(alpha = 0.6f)
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { onValueChange(opt); expanded = false },
                    enabled = enabled
                )
            }
        }
    }
}

/* --- Helpers --- */

private fun windowOptions(start: String, end: String): List<String> = when (start to end) {
    "7:00 AM" to "8:00 AM"   -> listOf("7:00 AM","7:15 AM","7:30 AM","7:45 AM","8:00 AM")
    "12:00 PM" to "1:00 PM"  -> listOf("12:00 PM","12:15 PM","12:30 PM","12:45 PM","1:00 PM")
    "4:00 PM" to "5:00 PM"   -> listOf("4:00 PM","4:15 PM","4:30 PM","4:45 PM","5:00 PM")
    else -> listOf(start, end)
}

private fun String.coerceInOptions(opts: List<String>): String =
    if (this in opts) this else opts.first()

private fun clampEndAfter(start: String, currentEnd: String, options: List<String>): String {
    val startIdx = options.indexOf(start).coerceAtLeast(0)
    val validEnds = options.drop(startIdx + 1)
    return when {
        currentEnd in validEnds -> currentEnd
        validEnds.isNotEmpty()  -> validEnds.first()
        else                    -> start
    }
}

@Composable
private fun FilledWideButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardXL,
        colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = SurfaceWhite)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

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
            Text(value, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
        }
    }
}

@Composable
private fun ActivityTile(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardXL,
        color = SurfaceWhite,
        tonalElevation = 1.dp,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Cocoa.copy(alpha = .6f))
        }
    }
}

/* ---- Water confetti overlay (fancier) ---- */

@Composable
private fun WaterConfettiOverlay(
    modifier: Modifier = Modifier,
    drops: Int = 18,
    onFinished: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // per-drop animatables
    val progress = remember { List(drops) { Animatable(0f) } }   // 0..1
    val alpha    = remember { List(drops) { Animatable(0f) } }
    val scale    = remember { List(drops) { Animatable(0.8f) } }
    val rotate   = remember { List(drops) { Animatable(0f) } }

    // randomized params (duration, x, sway amplitude)
    val seeds = remember {
        List(drops) {
            val dur = (900..1600).random()
            val startDelay = (0..350).random()
            val x = (8..320).random()
            val sway = (6..18).random()
            val rot = (-25..25).random()
            DropParams(dur, startDelay, x, sway, rot.toFloat())
        }
    }

    LaunchedEffect(Unit) {
        seeds.forEachIndexed { i, p ->
            scope.launch {
                delay(p.startDelay.toLong())
                alpha[i].animateTo(1f, tween(150))
                launch { progress[i].animateTo(1f, tween(p.duration)) }
                launch { rotate[i].animateTo(p.rotation, tween(p.duration)) }
                launch {
                    scale[i].animateTo(1.05f, tween(p.duration / 2))
                    scale[i].animateTo(1f,   tween(p.duration / 2))
                }
                alpha[i].animateTo(0f, tween(220))
            }
        }
        val maxDur = seeds.maxOf { it.startDelay + it.duration } + 260
        delay(maxDur.toLong())
        onFinished()
    }

    Box(modifier = modifier) {
        seeds.forEachIndexed { i, p ->
            val t = progress[i].value
            val sway = kotlin.math.sin(t * Math.PI).toFloat() * p.sway
            Text(
                "💧",
                modifier = Modifier
                    .graphicsLayer(
                        translationX = p.x + sway,
                        translationY = t * 220f,
                        rotationZ = rotate[i].value,
                        scaleX = scale[i].value,
                        scaleY = scale[i].value,
                        alpha = alpha[i].value
                    )
                    .padding(start = 0.dp),
                fontSize = 22.sp
            )
        }
    }
}

private data class DropParams(
    val duration: Int,
    val startDelay: Int,
    val x: Int,
    val sway: Int,
    val rotation: Float
)

/* ---- Heads-up bubble ---- */

@Composable
private fun HeadsUpBubble(
    title: String,
    message: String,
    danger: Boolean,
    modifier: Modifier = Modifier,
    onGone: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(-30f) }
    LaunchedEffect(Unit) {
        y.animateTo(0f, tween(250))
        alpha.animateTo(1f, tween(250))
        delay(1800)
        alpha.animateTo(0f, tween(250))
        onGone()
    }

    Surface(
        modifier = modifier
            .graphicsLayer(alpha = alpha.value, translationY = y.value)
            .padding(horizontal = 20.dp),
        shape = Pill,
        color = if (danger) Color(0xFFFFEAEA) else Color(0xFFF1F8E9),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (danger) "⚠️" else "💧", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
                Text(message, color = Cocoa.copy(alpha = .85f), fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    SchedulingScreen(onOpenReporting = {}, onOpenAccount = {}, onOpenNotifications = {})
}
