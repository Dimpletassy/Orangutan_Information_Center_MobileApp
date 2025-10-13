package com.oic.myapplication.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oic.myapplication.ui.palette.*   // Latte, Cocoa, SurfaceWhite, GoldDark, CocoaDeep, CardXL

import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AccountScreen(
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
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

    // --- Form state (kept as you had it) ---
    var first by remember { mutableStateOf("Jane") }
    var last  by remember { mutableStateOf("Doe") }
    var email by remember { mutableStateOf("janedoe@gmail.com") }
    var site  by remember { mutableStateOf("Jl. Pantai buaya, Bukit MAS, Kec. Besitang, Kabupaten Langkat, Sumatera Utara 20859") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Latte)
            .systemBarsPadding()
    ) {
        // Header
        Surface(color = Cocoa.copy(alpha = .30f)) {
            Column(Modifier.fillMaxWidth().padding(26.dp)) {
                Text("My Account:", color = SurfaceWhite, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${now.format(headerDateFmt)} • ${now.format(headerTimeFmt)}",
                    color = SurfaceWhite.copy(alpha = .9f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LabeledCard(label = "First Name/s :") {
            TextField(value = first, onValueChange = { first = it }, singleLine = true)
        }
        LabeledCard(label = "Surname :") {
            TextField(value = last, onValueChange = { last = it }, singleLine = true)
        }
        LabeledCard(label = "Phone Number/ Email:") {
            TextField(value = email, onValueChange = { email = it }, singleLine = true)
        }

        // Password row – link to Reset
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text("Password", color = GoldDark, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("●●●●●●●●", color = CocoaDeep)
                TextButton(onClick = onChangePassword) { Text("Change password") }
            }
        }

        LabeledCard(label = "Site Address:") {
            TextField(value = site, onValueChange = { site = it })
        }

        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* save profile later */ },
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = SurfaceWhite),
                shape = CardXL,
                modifier = Modifier.weight(1f)
            ) { Text("Save") }

            OutlinedButton(
                onClick = onLogout,
                shape = CardXL,
                modifier = Modifier.weight(1f)
            ) { Text("Log out", color = CocoaDeep) }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun LabeledCard(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(label, color = GoldDark, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Surface(shape = CardXL, color = SurfaceWhite, tonalElevation = 1.dp) {
            Box(Modifier.padding(12.dp)) { content() }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AccountPreview() {
    AccountScreen(onLogout = {}, onChangePassword = {})
}
