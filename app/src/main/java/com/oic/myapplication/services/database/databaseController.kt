package com.oic.myapplication.services.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.IrrigationLog
import com.oic.myapplication.services.database.models.Schedule
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


private const val LOG_COLLECTION = "IrrigationLogs"
private const val SCHEDULE_COLLECTION = "IrrigationSchedules"

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

    /**
     * Adds or updates an irrigation schedule.
     */
    fun addSchedule(schedule: Schedule) {
        db.collection(SCHEDULE_COLLECTION).document(schedule.day.name)
            .set(schedule)
            .addOnSuccessListener { Log.d(TAG, "Schedule for ${schedule.day.name} written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing schedule", e) }
    }
}
