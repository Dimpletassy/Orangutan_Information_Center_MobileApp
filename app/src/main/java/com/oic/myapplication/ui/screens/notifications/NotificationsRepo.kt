package com.oic.myapplication.ui.screens.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.R
import com.oic.myapplication.ui.components.*
import com.oic.myapplication.ui.palette.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/** One notification row in the feed. */
data class NotificationEvent(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val atMillis: Long = System.currentTimeMillis(),   // when it happened
    val danger: Boolean = false,
    val read: Boolean = false
)

/**
 * Simple in-memory store so Scheduling + Notifications screens can talk.
 * (Swap with a DB later without touching UI).
 */
object NotificationsRepo {
    private val _items = MutableStateFlow<List<NotificationEvent>>(emptyList())
    /** Full list, newest first. */
    val items: Flow<List<NotificationEvent>> = _items

    /** Live unread count for the red badge. */
    val unreadCount: Flow<Int> = _items.map { list -> list.count { !it.read } }

    /** Generic push. */
    fun push(title: String, message: String, danger: Boolean = false) {
        val now = System.currentTimeMillis()
        _items.value = listOf(
            NotificationEvent(title = title, message = message, atMillis = now, danger = danger)
        ) + _items.value
    }

    /** Helpers used by SchedulingScreen. */
    fun pushStart(timeText: String) = push("STARTED: Automated Watering", timeText)
    fun pushStop(timeText: String)  = push("STOPPED: Automated Watering", timeText)
    fun pushManual(message: String) = push("MANUAL OVERRIDE", message, danger = true)

    /** Call when user opens Notifications screen to clear the badge. */
    fun markAllRead() {
        _items.value = _items.value.map { it.copy(read = true) }
    }
}

/* --------- shared format helpers (API 21+) --------- */

private val TIME_FMT = SimpleDateFormat("h:mm a", Locale.getDefault())
private val DATE_FMT = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault())

fun Long.asTime(): String = TIME_FMT.format(Date(this))
fun Long.asDate(): String = DATE_FMT.format(Date(this))

