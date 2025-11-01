package com.oic.myapplication.services.database.models

import com.google.firebase.Timestamp

/**
 * Represents irrigation activity for a single day in Firestore.
 * Each document holds all irrigation logs for that date.
 */
data class DailyLog(
    /** The date for this log (e.g., "2025-09-28"). Used as the document ID. */
    val date: String,

    /** Timestamp of when this log was created or last updated. */
    val timestamp: Timestamp = Timestamp.now(),

    /** Map of irrigation entries keyed by start time (e.g., "07:30:00"). */
    val logs: Map<String, IrrigationLog>
)
