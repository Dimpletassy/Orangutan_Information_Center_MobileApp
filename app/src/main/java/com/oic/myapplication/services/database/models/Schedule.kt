package com.oic.myapplication.services.database.models

enum class Day(val displayName: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    override fun toString(): String = displayName
}

data class Schedule(
    val day: Day,
    val startTime: String,
    val zone: List<String>, // 0, 1
    val durationMins: Int
)
