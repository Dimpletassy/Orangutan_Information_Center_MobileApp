package com.oic.myapplication.services.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.oic.myapplication.services.database.models.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.floor
import kotlin.math.max

// -------------------------
// Firestore collection constants
// -------------------------
private const val LOG_COLLECTION = "IrrigationLogs"          // Stores DailyLog docs
private const val SCHEDULE_COLLECTION = "IrrigationSchedules" // Stores weekly schedule templates
private const val STATE_COLLECTION = "IrrigationState"        // (Legacy path for system state)
private const val STATE_DOC       = "current"                 // (Legacy path for system state)
private const val CURRENT_SITE_ID = "Site-01"                 // Default site document id

// ---- Manual run flow model ----
private const val FLOW_LPM = 27.0 // litres per minute

// Local time formatting for logs (adjust to your desired format)
private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/** Compute used litres since startAt -> now, floored to whole litres. */
private fun usedLitresSince(startedAtMs: Long, nowMs: Long = System.currentTimeMillis()): Int {
    val elapsedMin = max(0.0, (nowMs - startedAtMs) / 60_000.0)
    return floor(elapsedMin * FLOW_LPM).toInt()
}

/** Convenience to convert epoch ms -> LocalDateTime in system zone. */
private fun ldt(ms: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())

/**
 * Handles all Firestore operations related to:
 *  - Daily irrigation logs
 *  - Weekly irrigation schedules
 *  - Manual irrigation state tracking
 *
 * Each helper encapsulates Firestore read/write logic and hides async complexity.
 */
class DatabaseController {
    companion object {
        private const val TAG = "Firestore"
    }

    private val db = FirebaseFirestore.getInstance()

    // --------------------------------------------------------------------------
    // DAILY LOG MANAGEMENT
    // --------------------------------------------------------------------------

    /**
     * Creates a new DailyLog document (if it doesn't already exist) inside [LOG_COLLECTION].
     * Each log represents all irrigation events for a specific date.
     */
    fun createDailyLog(dailyLog: DailyLog) {
        val docRef = db.collection(LOG_COLLECTION).document(dailyLog.date)
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d(TAG, "Irrigation log ${dailyLog.date} already exists")
                } else {
                    docRef.set(dailyLog)
                        .addOnSuccessListener { Log.d(TAG, "Log for ${dailyLog.date} written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing log", e) }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "GET failed for ${dailyLog.date}", e)
            }
    }

    /**
     * Adds or updates a specific [IrrigationLog] entry inside an existing DailyLog.
     * - Uses the `logs.{startTime}` field map for nested key-based storage.
     * - Automatically creates the `logs` map if missing.
     */
    fun addIrrigationLog(dailyLog: DailyLog, irrigationLog: IrrigationLog) {
        db.collection(LOG_COLLECTION).document(dailyLog.date)
            .update("logs.${irrigationLog.startTime}", irrigationLog)
            .addOnSuccessListener { Log.d(TAG, "Log added successfully!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error adding log", e) }
    }

    // --------------------------------------------------------------------------
    // MANUAL IRRIGATION STATE (REAL-TIME)
    // --------------------------------------------------------------------------

    /**
     * Listens in real-time to the "manualRun" state document.
     * This reflects whether a manual irrigation is currently active.
     *
     * @param onUpdate callback invoked with the latest [ManualRunDoc] (or null if missing)
     * @return Firestore [ListenerRegistration] (should be removed on screen dispose)
     */
    fun listenActiveManualRun(onUpdate: (ManualRunDoc?) -> Unit): ListenerRegistration {
        return db.collection("sites")
            .document(CURRENT_SITE_ID)
            .collection("state")
            .document("manualRun")
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                onUpdate(snap?.toObject<ManualRunDoc>())
            }
    }

    /**
     * Fetches the current "manualRun" state document once (no listener).
     * Useful for initial state load or polling fallback.
     */
    fun getActiveManualRun(callback: (Result<ManualRunDoc?>) -> Unit) {
        db.collection("sites")
            .document(CURRENT_SITE_ID)
            .collection("state")
            .document("manualRun")
            .get()
            .addOnSuccessListener { d -> callback(Result.success(d.toObject<ManualRunDoc>())) }
            .addOnFailureListener { e -> callback(Result.failure(e)) }
    }

    /**
     * Begins a manual irrigation session.
     * Writes a "manualRun" state doc with:
     *  - `running = true`
     *  - `totalLitres = user-selected amount`
     *  - `startAtEpochMs = System.currentTimeMillis()`
     *
     * This doc becomes the single source of truth for all UIs observing irrigation state.
     */
    fun beginManualRun(totalLitres: Int, callback: (Result<Unit>) -> Unit) {
        val payload = mapOf(
            "running" to true,
            "totalLitres" to totalLitres,
            "startAtEpochMs" to System.currentTimeMillis(),
            // clear any leftover state fields from previous runs
            "usedLitres" to null,
            "remainingLitres" to null,
            "stoppedAtEpochMs" to null
        )
        db.collection("sites")
            .document(CURRENT_SITE_ID)
            .collection("state")
            .document("manualRun")
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { e -> callback(Result.failure(e)) }
    }

    /**
     * Ends a manual irrigation session and LOGS the actual used litres.
     *
     * Steps:
     * 1) Read current /sites/{CURRENT_SITE_ID}/state/manualRun
     * 2) If running, compute used/remaining based on startAtEpochMs and FLOW_LPM
     * 3) Append a DailyLog entry (scheduled=false, litres=used) under IrrigationLogs/{date}
     * 4) Update manualRun state: running=false, usedLitres, remainingLitres, stoppedAtEpochMs
     *
     * Safe to call even if not running (it will just mark running=false).
     */
    fun endManualRun(callback: (Result<Unit>) -> Unit) {
        val stateRef = db.collection("sites")
            .document(CURRENT_SITE_ID)
            .collection("state")
            .document("manualRun")

        stateRef.get()
            .addOnSuccessListener { snap ->
                val state = snap.toObject<ManualRunDoc>()
                val nowMs = System.currentTimeMillis()

                // If no state doc or not running, just mark not-running and return
                if (state == null || state.running != true) {
                    stateRef.set(mapOf("running" to false), SetOptions.merge())
                        .addOnSuccessListener { callback(Result.success(Unit)) }
                        .addOnFailureListener { e -> callback(Result.failure(e)) }
                    return@addOnSuccessListener
                }

                // Compute used/remaining
                val startMs = state.startAtEpochMs
                val total = state.totalLitres
                val used = usedLitresSince(startMs, nowMs).coerceAtMost(max(0, total))
                val remaining = max(0, total - used)

                // Build log row (manual runs are unscheduled)
                val startLdt = ldt(startMs)
                val endLdt   = ldt(nowMs)
                val dateId   = DATE_FMT.format(startLdt)               // DailyLog doc id
                val startStr = TIME_FMT.format(startLdt)               // key & field value
                val endStr   = TIME_FMT.format(endLdt)

                // Ensure DailyLog exists (lightweight create if missing)
                val dailyLogDoc = db.collection(LOG_COLLECTION).document(dateId)
                dailyLogDoc.get()
                    .addOnSuccessListener { dailySnap ->
                        if (!dailySnap.exists()) {
                            val newDaily = DailyLog(
                                date = dateId,
                                timestamp = Timestamp.now(),
                                logs = emptyMap()
                            )
                            dailyLogDoc.set(newDaily)
                                .addOnFailureListener { e -> Log.w(TAG, "Failed to create DailyLog $dateId", e) }
                        }

                        // Write the actual manual-run log using your existing schema
                        // We use 'litres = used' because that is what actually flowed.
                        val logRow = IrrigationLog(
                            zone = emptyList(),          // set real zones if you track them for manual runs
                            scheduled = false,
                            litres = used,
                            startTime = startStr,
                            endTime = endStr
                        )

                        // Upsert nested field logs.{startTime} = logRow
                        dailyLogDoc
                            .update("logs.$startStr", logRow)
                            .addOnSuccessListener {
                                // Finally, update the state doc with stop details
                                val endPayload = mapOf(
                                    "running" to false,
                                    "usedLitres" to used,
                                    "remainingLitres" to remaining,
                                    "stoppedAtEpochMs" to nowMs
                                )
                                stateRef.set(endPayload, SetOptions.merge())
                                    .addOnSuccessListener { callback(Result.success(Unit)) }
                                    .addOnFailureListener { e -> callback(Result.failure(e)) }
                            }
                            .addOnFailureListener { e -> callback(Result.failure(e)) }
                    }
                    .addOnFailureListener { e -> callback(Result.failure(e)) }
            }
            .addOnFailureListener { e -> callback(Result.failure(e)) }
    }

    // --------------------------------------------------------------------------
    // DAILY LOG RETRIEVAL
    // --------------------------------------------------------------------------

    /**
     * Retrieves **all** DailyLogs from Firestore.
     * @return List<DailyLog> representing each date document under [LOG_COLLECTION].
     */
    suspend fun getAllDailyLogs(): List<DailyLog> = suspendCoroutine { continuation ->
        db.collection(LOG_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val logs = result.map { doc ->
                    val data = doc.data
                    val logDate = data["date"] as? String ?: ""
                    val timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now()
                    val rawLogs = data["logs"] as? Map<String, Map<String, Any>> ?: emptyMap()
                    val logsMap = rawLogs.mapValues { (_, logEntry) ->
                        IrrigationLog(
                            zone = logEntry["zone"] as? List<String> ?: emptyList(),
                            scheduled = logEntry["scheduled"] as? Boolean ?: false,
                            litres = (logEntry["litres"] as? Long)?.toInt() ?: 0,
                            startTime = logEntry["startTime"] as? String ?: "",
                            endTime = logEntry["endTime"] as? String ?: ""
                        )
                    }
                    DailyLog(logDate, timestamp, logsMap)
                }
                continuation.resume(logs)
            }
            .addOnFailureListener { e -> continuation.resumeWithException(e) }
    }

    /**
     * Retrieves a single DailyLog document by date (non-suspending).
     * Invokes [onResult] with the Firestore data map (or null if not found).
     */
    fun getDailyLog(date: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection(LOG_COLLECTION).document(date)
            .get()
            .addOnSuccessListener { doc -> onResult(doc.takeIf { it.exists() }?.data) }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to get daily log for $date", e)
                onResult(null)
            }
    }

    /**
     * Retrieves all DailyLogs within a given date range (inclusive).
     * Prints results to Logcat for inspection.
     */
    fun getRangeDailyLog(fromDate: String, toDate: String) {
        db.collection(LOG_COLLECTION)
            .whereGreaterThanOrEqualTo("date", fromDate)
            .whereLessThanOrEqualTo("date", toDate)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(TAG, "No logs found between $fromDate and $toDate")
                } else {
                    result.forEach { doc ->
                        Log.d(TAG, "${doc.id} => ${doc.data}")
                    }
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error getting range logs", e) }
    }

    // --------------------------------------------------------------------------
    // IRRIGATION SCHEDULING (Weekly Templates)
    // --------------------------------------------------------------------------

    /**
     * Inserts or updates a single [ScheduleEntry] period for a given [Day].
     * Uses `.set(..., merge())` to avoid overwriting other periods.
     */
    fun upsertSchedulePeriod(
        day: Day,
        period: Period,
        entry: ScheduleEntry,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val docId = day.name  // e.g. "MONDAY"
        val fieldName = period.toFieldName() // "morning", "midday", "afternoon"

        val data = mapOf(
            "day" to day,
            fieldName to entry,
            "updatedAt" to Timestamp.now()
        )

        db.collection(SCHEDULE_COLLECTION)
            .document(docId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Deletes one period (e.g. "morning") from a [ScheduleDay] document.
     * Other existing periods remain intact.
     */
    fun deleteSchedulePeriod(
        day: Day,
        period: Period,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val docId = day.name
        val fieldName = period.toFieldName()

        val updates = hashMapOf<String, Any?>(
            fieldName to null,
            "updatedAt" to Timestamp.now()
        )

        db.collection(SCHEDULE_COLLECTION)
            .document(docId)
            .update(updates)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Retrieves a single [ScheduleDay] document (by enum day name).
     * @param day e.g. Day.MONDAY
     */
    fun getScheduleDay(
        day: Day,
        onComplete: (Result<ScheduleDay?>) -> Unit
    ) {
        db.collection(SCHEDULE_COLLECTION)
            .document(day.name)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    onComplete(Result.success(null))
                } else {
                    val obj = snap.toObject<ScheduleDay>()
                    onComplete(Result.success(obj))
                }
            }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Upserts multiple periods (morning/midday/afternoon) for one day in a single call.
     * Automatically merges existing data and updates `updatedAt` timestamp.
     */
    fun upsertDay(
        day: Day,
        morning: ScheduleEntry? = null,
        midday: ScheduleEntry? = null,
        afternoon: ScheduleEntry? = null,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val docId = day.name
        val data = mutableMapOf<String, Any>(
            "day" to day,
            "updatedAt" to Timestamp.now()
        )
        morning?.let { data["morning"] = it }
        midday?.let { data["midday"] = it }
        afternoon?.let { data["afternoon"] = it }

        db.collection(SCHEDULE_COLLECTION)
            .document(docId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "upsert successful")
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener {
                Log.w(TAG, "Upsert failure")
                onComplete(Result.failure(it))
            }
    }

    // --------------------------------------------------------------------------
    // REAL-TIME UPDATES
    // --------------------------------------------------------------------------

    /**
     * Subscribes to ALL schedule-day documents in [SCHEDULE_COLLECTION].
     * @param onUpdate Emits a sorted list of [ScheduleDay] on every change.
     */
    fun getAllSchedules(
        onUpdate: (Result<List<ScheduleDay>>) -> Unit
    ): ListenerRegistration {
        return db.collection(SCHEDULE_COLLECTION)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(Result.failure(err))
                    return@addSnapshotListener
                }
                if (snap == null) {
                    onUpdate(Result.success(emptyList()))
                    return@addSnapshotListener
                }
                try {
                    val items = snap.documents
                        .mapNotNull { it.toObject<ScheduleDay>() }
                        .sortedBy { it.day.ordinal }
                    onUpdate(Result.success(items))
                } catch (t: Throwable) {
                    onUpdate(Result.failure(t))
                }
            }
    }

    // --------------------------------------------------------------------------
    // INTERNAL UTILITIES
    // --------------------------------------------------------------------------

    /** Maps enum [Period] to its Firestore field name key. */
    private fun Period.toFieldName(): String = when (this) {
        Period.MORNING -> "morning"
        Period.MIDDAY -> "midday"
        Period.AFTERNOON -> "afternoon"
    }
}
