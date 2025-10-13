package com.oic.myapplication.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oic.myapplication.ui.palette.*

@Composable
fun NotificationsScreen() {
    val list by NotificationsRepo.items.collectAsState(initial = emptyList())

    // mark as read when we open the screen
    LaunchedEffect(Unit) { NotificationsRepo.markAllRead() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Latte)
            .systemBarsPadding()
    ) {
        Surface(color = Cocoa.copy(alpha = .30f)) {
            Column(Modifier.fillMaxWidth().padding(26.dp)) {
                Text("Notifications", color = SurfaceWhite, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                // ⬇️ header date
                Text(System.currentTimeMillis().asDate(), color = SurfaceWhite.copy(alpha = .9f))
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (list.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications yet", color = Cocoa.copy(alpha = .6f))
                    }
                }
            } else {
                items(list, key = { it.id }) { n ->
                    Notice(
                        title = n.title,
                        body = n.message,
                        time = n.atMillis.asTime(),         // ⬅️ use helper
                        danger = n.danger
                    )
                }
            }
        }
    }
}

@Composable
private fun Notice(
    title: String,
    body: String,
    time: String,
    danger: Boolean = false
) {
    val chip = if (danger) GoldLight.copy(alpha = .85f) else GoldLight
    Surface(
        shape = CardXL,
        color = chip,
        tonalElevation = 1.dp,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = if (danger) androidx.compose.ui.graphics.Color.Red else CocoaDeep,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(body, color = Cocoa.copy(alpha = .8f))
            }
            Text(time, color = CocoaDeep)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    NotificationsScreen()
}
