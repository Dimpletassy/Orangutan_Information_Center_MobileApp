package com.oic.myapplication.services.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.Day
import com.oic.myapplication.services.database.models.IrrigationLog
import com.oic.myapplication.services.database.models.Period
import com.oic.myapplication.services.database.models.ScheduleDay
import com.oic.myapplication.services.database.models.ScheduleEntry
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


private const val LOG_COLLECTION = "IrrigationLogs"
private const val SCHEDULE_COLLECTION = "IrrigationSchedules"
// ---- Add near the top with your other consts ----
private const val STATE_COLLECTION = "IrrigationState"
private const val STATE_DOC       = "current"


class DatabaseController {
    companion object {
        private const val TAG = "Firestore"
    }

    private val db = FirebaseFirestore.getInstance()

    /**
     * Creates a daily log if it doesn't exist already.
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
     * Adds a new irrigation log entry to a daily log.
     */
    fun addIrrigationLog(dailyLog: DailyLog, irrigationLog: IrrigationLog) {
        db.collection(LOG_COLLECTION).document(dailyLog.date)
            .update("logs.${irrigationLog.startTime}", irrigationLog)
            .addOnSuccessListener { Log.d(TAG, "Log added successfully!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error adding log", e) }
    }

    // ---- Add anywhere inside DatabaseController ----

    /**
     * Start a MANUAL irrigation run.
     * - Writes a state doc so UI can reflect manual running.
     * - Creates today's DailyLog (if needed) and adds an IrrigationLog entry keyed by startTime.
     *
     * @return onComplete: success(Unit) or failure(Throwable)
     */
    fun beginManualRun(
        litres: Int,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val now = com.google.firebase.Timestamp.now()
        val zoneList = emptyList<String>() // no zones for now
        val startStr = java.time.LocalTime.now().let {
            java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(it)
        }

        // derive expected runtime using same dummy flow model as UI
        val minutes = kotlin.math.max(5, kotlin.math.ceil(litres / 2.0).toInt())
        val endStr = java.time.LocalTime.now().plusMinutes(minutes.toLong()).let {
            java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(it)
        }

        // 1) Write current state
        val stateData = hashMapOf(
            "running"       to true,
            "mode"          to "manual",
            "litres"        to litres,
            "startedAt"     to now,
            "expectedEndIn" to minutes,
            "startTimeKey"  to startStr // used to update endTime on stop
        )

        db.collection(STATE_COLLECTION).document(STATE_DOC)
            .set(stateData, SetOptions.merge())
            .addOnFailureListener { onComplete(Result.failure(it)) }
            .addOnSuccessListener {
                // 2) Also append a DailyLog row
                val today = java.time.LocalDate.now().toString()
                val dailyLog = com.oic.myapplication.services.database.models.DailyLog(
                    date = today,
                    timestamp = now,
                    logs = emptyMap()
                )
                createDailyLog(dailyLog) // creates if missing (safe no-op if exists)

                val irrigationLog = com.oic.myapplication.services.database.models.IrrigationLog(
                    startTime = startStr,
                    endTime   = endStr,            // predicted end; we’ll correct on stop
                    scheduled = false,
                    litres    = litres,
                    zone      = zoneList
                )
                addIrrigationLog(dailyLog, irrigationLog)
                onComplete(Result.success(Unit))
            }
    }

    /**
     * Stop a MANUAL irrigation run.
     * - Sets running=false in the state doc.
     * - Updates today's DailyLog {logs.<startTimeKey>.endTime} to NOW (if state contains that key).
     */
    fun endManualRun(
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val stateRef = db.collection(STATE_COLLECTION).document(STATE_DOC)
        stateRef.get()
            .addOnFailureListener { onComplete(Result.failure(it)) }
            .addOnSuccessListener { snap ->
                val startKey = snap.getString("startTimeKey")
                val updates = hashMapOf<String, Any?>(
                    "running" to false,
                    "stoppedAt" to com.google.firebase.Timestamp.now()
                )
                stateRef.set(updates, SetOptions.merge())

                if (startKey.isNullOrBlank()) {
                    onComplete(Result.success(Unit))
                    return@addOnSuccessListener
                }

                // Update today’s DailyLog endTime to the real stop time
                val today = java.time.LocalDate.now().toString()
                val endNow = java.time.LocalTime.now().let {
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(it)
                }
                db.collection(LOG_COLLECTION).document(today)
                    .update("logs.$startKey.endTime", endNow)
                    .addOnSuccessListener { onComplete(Result.success(Unit)) }
                    .addOnFailureListener { onComplete(Result.failure(it)) }
            }
    }


    /**
     * Retrieves all daily logs as a list.
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
     * Retrieves a daily log for a specific date.
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
     * Retrieves daily logs in a given date range.
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

    /** Upsert a single period on a day doc (document id = day.name). */
    fun upsertSchedulePeriod(
        day: Day,
        period: Period,
        entry: ScheduleEntry,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        val docId = day.name  // "MONDAY", "TUESDAY", ...
        val fieldName = period.toFieldName() // "morning" / "midday" / "afternoon"

        val data = mapOf(
            "day" to day,
            fieldName to entry,
            "updatedAt" to Timestamp.now()
        )

        db.collection(SCHEDULE_COLLECTION)
            .document(docId)
            .set(data, SetOptions.merge())   // merge so other periods aren't lost
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /** Remove one period map from a day (keeps the others). */
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

    /** Get a whole day doc (with morning/midday/afternoon entries if present). */
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

    /** Convenience: save multiple period entries in one go (merge). */
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

    /** Map enum to the field key we store in Firestore. */
    private fun Period.toFieldName(): String = when (this) {
        Period.MORNING -> "morning"
        Period.MIDDAY -> "midday"
        Period.AFTERNOON -> "afternoon"
    }

    /** Realtime updates for all schedule-day docs (optional helper). */
    fun getAllSchedules(
        onUpdate: (Result<List<ScheduleDay>>) -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection(SCHEDULE_COLLECTION)
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

}


