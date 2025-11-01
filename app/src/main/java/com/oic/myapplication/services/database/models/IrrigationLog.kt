package com.oic.myapplication.services.database.models

/**
 * Represents a single irrigation event.
 */
data class IrrigationLog(
    /** Start time of the irrigation (24h format). */
    val startTime: String,

    /** End time of the irrigation (24h format). */
    val endTime: String,

    /** Zones involved in this run (e.g., ["0", "1"]). */
    val zone: List<String>,

    /** Total litres used during this run. */
    val litres: Int,

    /** True if scheduled automatically, false if started manually. */
    val scheduled: Boolean
)
