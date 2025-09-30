package com.oic.myapplication.helper

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getcurTime(): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}

fun getcurDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

fun timestampToDate(timestamp: Timestamp): String{
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return date.format(timestamp.toDate())
}