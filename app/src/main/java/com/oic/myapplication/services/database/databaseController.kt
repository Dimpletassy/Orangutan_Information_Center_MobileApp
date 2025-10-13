package com.oic.myapplication.services.database

import android.content.ContentValues
import android.util.Log
import androidx.core.os.registerForAllProfilingResults
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.oic.myapplication.helper.timestampToDate
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.IrrigationLog
import com.oic.myapplication.services.database.models.Schedule
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


val TAG = "Firestore"

// time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
// date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())


const val logHistoryCollectionPath = "IrrigationLogs"
const val scheduleCollectionPath = "IrrigationSchedules"

class databaseController {
    val db = Firebase.firestore

    fun createDailyLog(dailyLog: DailyLog){
        /* CREATES A DAILY LOG TO THE DB FOR EACH IRRIGATION ACTION OF THE DAY TO BE ADDED TO */
        db.collection(logHistoryCollectionPath)
            .document(dailyLog.date)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() ){
                    Log.d(TAG, "Irrigation log ${dailyLog.date} already exists")
                } else {
                    db.collection(logHistoryCollectionPath).document(dailyLog.date)
                        .set(dailyLog)
                        .addOnSuccessListener { Log.d(TAG, "Log for ${dailyLog.date} written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing log", e) }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "GET failed with ", exception)
            }
    }


    fun addIrrigationLog(dailyLog: DailyLog, irrigationLog: IrrigationLog) {
        db.collection(logHistoryCollectionPath).document(dailyLog.date)
            .update(
                mapOf(
                    "logs.${irrigationLog.startTime}" to irrigationLog
                )
            )
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "Log added successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding log", e)
            }
    }

    suspend fun getAllDailyLogs(): List<DailyLog> = suspendCoroutine { continuation ->
        db.collection(logHistoryCollectionPath)
            .get()
            .addOnSuccessListener { result ->
                val logs = result.map { document ->
                    val data = document.data
                    val logDate = data["date"] as String
                    val timestamp = data["timestamp"] as Timestamp
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
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    fun getDailyLog(date: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection(logHistoryCollectionPath).document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(document.data)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "get failed with ", exception)
                onResult(null)
            }
    }

    fun getRangeDailyLog(fromDate: String, toDate: String){
        db.collection(logHistoryCollectionPath)
            .whereGreaterThanOrEqualTo("date", fromDate)
            .whereLessThanOrEqualTo("date", toDate)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty ){
                    Log.d(TAG, "GET RANGE returns empty")
                } else{
                    for (document in result){
                        Log.d(TAG, "from date = ${fromDate}, to date = ${toDate}")
                        Log.d(TAG, "${document.id} => ${document.data}")

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting range logs ", exception)
            }
    }

    fun addSchedule(schedule: Schedule){
        /* USED FOR CREATING AND UPDATING SCHEDULES */
        db.collection(scheduleCollectionPath)
            .document(schedule.day.name)
            .set(schedule)
            .addOnSuccessListener { Log.d(TAG, "Log for ${schedule.day.name} written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing log", e) }
    }



}