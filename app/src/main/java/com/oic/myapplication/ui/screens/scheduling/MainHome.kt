package com.oic.myapplication.ui.screens.scheduling

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oic.myapplication.R
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.IrrigationLog
import com.oic.myapplication.ui.components.WeatherCard
import com.oic.myapplication.ui.palette.CardXL
import com.oic.myapplication.ui.palette.Cocoa
import com.oic.myapplication.ui.palette.CocoaDeep
import com.oic.myapplication.ui.palette.GoldLight
import com.oic.myapplication.ui.palette.Latte
import com.oic.myapplication.ui.palette.Pill
import com.oic.myapplication.ui.palette.SurfaceWhite
import com.oic.myapplication.ui.screens.weather.WeatherViewModel
import com.oic.myapplication.ui.screens.notifications.NotificationsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/* ---------------- Constants ---------------- */

private const val LAT = 3.5952
private const val LON = 98.6722

/* ---------------- Heads-up bubble / confetti ---------------- */

private data class NotificationEventUi(val title: String, val message: String, val danger: Boolean)

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

@Composable
private fun WaterConfettiOverlay(
    modifier: Modifier = Modifier,
    drops: Int = 18,
    onFinished: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { List(drops) { Animatable(0f) } }
    val alpha    = remember { List(drops) { Animatable(0f) } }
    val scale    = remember { List(drops) { Animatable(0.8f) } }
    val rotate   = remember { List(drops) { Animatable(0f) } }

    data class DropParams(val duration: Int, val startDelay: Int, val x: Int, val sway: Int, val rotation: Float)
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

/* ---------------- Main screen ---------------- */

@Composable
fun MainHomeScreen(
    onOpenReporting: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenNotifications: () -> Unit,
    onBackToSites: () -> Unit,
) {
    // Date/time
    val zone = remember { ZoneId.systemDefault() }
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(zone); delay(60_000) } }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    val timeText by remember(now) {
        derivedStateOf { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) }
    }

    // Irrigation run state
    val isIrrigating by IrrigationRepo.isRunning.collectAsState()
    var showSplash by remember { mutableStateOf(false) }

    // Weather
    val weatherVm: WeatherViewModel = viewModel()
    val weather by weatherVm.state.collectAsState()
    LaunchedEffect(Unit) { weatherVm.refresh(LAT, LON) }

    // Bell badge
    val unread by NotificationsRepo.unreadCount.collectAsState(initial = 0)

    // Heads-up bubble state
    var headUp by remember { mutableStateOf<NotificationEventUi?>(null) }

    // Recent Activity (last irrigation log)
    val db = remember { DatabaseController() }
    var recentLoading by remember { mutableStateOf(true) }
    var recentError by remember { mutableStateOf<String?>(null) }
    var recentDate by remember { mutableStateOf<String?>(null) }
    var recentLog by remember { mutableStateOf<IrrigationLog?>(null) }

    LaunchedEffect(Unit) {
        recentLoading = true; recentError = null
        try {
            val all: List<DailyLog> = db.getAllDailyLogs()
            if (all.isEmpty()) {
                recentDate = null
                recentLog = null
            } else {
                // Most recent day by ISO date (yyyy-MM-dd)
                val latestDay: DailyLog = all.maxBy { it.date }
                recentDate = latestDay.date

                // Determine the "latest" log within that day
                val timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                fun parseTime(s: String?): java.time.LocalTime? =
                    runCatching { java.time.LocalTime.parse((s ?: "").uppercase(), timeFmt) }.getOrNull()

                recentLog = latestDay.logs.values
                    .maxWithOrNull(
                        compareBy<IrrigationLog> { parseTime(it.endTime) ?: java.time.LocalTime.MIN }
                            .thenBy { parseTime(it.startTime) ?: java.time.LocalTime.MIN }
                    )
            }
        } catch (t: Throwable) {
            recentError = t.message
            recentDate = null
            recentLog = null
        } finally {
            recentLoading = false
        }
    }

    // Background
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
                TopBar(
                    unread = unread,
                    dateText = now.format(dateFmt),
                    timeText = timeText,
                    onBack = onBackToSites,
                    onOpenNotifications = onOpenNotifications
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Current status card (manual start/stop)
                item {
                    CurrentStatusCard(
                        isIrrigating = isIrrigating,
                        onToggle = { start ->
                            if (start) {
                                IrrigationRepo.start()
                                headUp = NotificationEventUi(
                                    title = "Started",
                                    message = "Manual irrigation system is now running",
                                    danger = false
                                )
                                showSplash = true
                            } else {
                                IrrigationRepo.stop()
                                headUp = NotificationEventUi(
                                    title = "Stopped",
                                    message = "Manual irrigation has been stopped",
                                    danger = false
                                )
                                showSplash = false
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .fillMaxWidth()
                    )
                }

                // Weather
                item {
                    WeatherCard(
                        temperatureC = weather.temperatureC,
                        humidityPct = weather.humidityPct,
                        windKmh = weather.windKmh,
                        loading = weather.loading,
                        error = weather.error,
                        onRefresh = { weatherVm.refresh(LAT, LON) },
                        onUseDeviceLocation = { /* TODO: GPS */ }
                    )
                }

                // Recent Activity header
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

                // Recent Activity content
                when {
                    recentLoading -> {
                        item {
                            Surface(
                                shape = CardXL,
                                color = SurfaceWhite,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                    recentError != null -> {
                        item {
                            Surface(
                                shape = CardXL,
                                color = SurfaceWhite,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Couldn’t load recent activity", color = CocoaDeep, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(recentError ?: "", color = Cocoa)
                                }
                            }
                        }
                    }
                    recentLog == null -> {
                        item {
                            Surface(
                                shape = CardXL,
                                color = SurfaceWhite,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("No irrigation runs yet", color = CocoaDeep, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Your latest irrigation activity will appear here.", color = Cocoa)
                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            val log = recentLog!!
                            val type = if (log.scheduled) "Scheduled" else "Manual"
                            val zones = if (log.zone.isEmpty()) "—" else log.zone.joinToString(", ")
                            val timeRange = listOfNotNull(
                                log.startTime.takeIf { it.isNotBlank() },
                                log.endTime.takeIf { it.isNotBlank() }
                            ).joinToString("–")

                            // ✨ Only change: format recentDate (yyyy-MM-dd) to dd/MM/yy
                            val formattedDate = runCatching {
                                java.time.LocalDate.parse(recentDate).format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                            }.getOrElse { "—" }

                            Surface(
                                shape = CardXL,
                                color = SurfaceWhite,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("$type irrigation", color = CocoaDeep, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Date: $formattedDate", color = Cocoa)
                                    Text("Time: ${if (timeRange.isBlank()) "—" else timeRange}", color = Cocoa)
                                    Text("Litres: ${log.litres}", color = Cocoa)
                                    Text("Zones: $zones", color = Cocoa)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(18.dp)) }

            }
        }

        // Overlays
        if (showSplash) {
            WaterConfettiOverlay(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CardXL)
            ) { showSplash = false }
        }
        val headUpState = headUp
        if (headUpState != null) {
            HeadsUpBubble(
                title = headUpState.title,
                message = headUpState.message,
                danger = headUpState.danger,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) { headUp = null }
        }
    }
}

/* ---------------- UI pieces ---------------- */

@Composable
private fun TopBar(
    unread: Int,
    dateText: String,
    timeText: String,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit
) {
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
                Text("Selamat Datang, Bang Nanda 😊", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = SurfaceWhite)

                Box {
                    IconButton(onClick = onOpenNotifications) { Text("🔔", fontSize = 26.sp, lineHeight = 26.sp) }
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
            Text("$dateText  •  $timeText", color = SurfaceWhite.copy(alpha = 0.9f))
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

/* --- Misc shared UI --- */

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

/* ---------------- Preview ---------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    MainHomeScreen(
        onOpenReporting = {},
        onOpenAccount = {},
        onOpenNotifications = {},
        onBackToSites = {}
    )
}
