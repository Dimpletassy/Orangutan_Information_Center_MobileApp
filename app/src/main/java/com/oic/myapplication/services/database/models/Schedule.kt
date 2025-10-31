package com.oic.myapplication.services.database.models

import com.google.firebase.Timestamp

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

enum class Period { MORNING, MIDDAY, AFTERNOON }

/** One period’s settings (stored as a map on the day doc). */
data class ScheduleEntry(
    val startTime: String = "",          // e.g., "7:00 AM"
    val litres: Int = 0,                 // e.g., 20
    val enabled: Boolean = true,         // on/off
    val zone: List<String> = emptyList() // kept for future; can be empty
)

/** Whole day doc (document id = day.name). Each period is a nested object. */
data class ScheduleDay(
    val day: Day = Day.MONDAY,
    val morning: ScheduleEntry? = null,
    val midday: ScheduleEntry? = null,
    val afternoon: ScheduleEntry? = null,
    val updatedAt: Timestamp? = null
)