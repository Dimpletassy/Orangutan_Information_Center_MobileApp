package com.oic.myapplication.services.database

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


val TAG = "Database Controller"

// time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
// date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
data class IrrigationLog(
    val startTime: String,
    val zone: List<String>,
    val duration: String
)

data class DailyLog(
    val date: String ,
    val logs: Map<String, IrrigationLog>
)
const val logHistoryCollectionPath = "logHistory"

class databaseController {
    val db = Firebase.firestore

    fun createDailyLog(dailyLog: DailyLog){
        /* CREATES A DAILY LOG TO THE DB FOR EACH IRRIGATION ACTION OF THE DAY TO BE ADDED TO */
        db.collection(logHistoryCollectionPath).document(dailyLog.date)
            .set(dailyLog)
            .addOnSuccessListener { Log.d(TAG, "Log for ${dailyLog.date} written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing log", e) }
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

    fun getAllDailyLog(){
        db.collection(logHistoryCollectionPath)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDailyLog(date: String){
        /* will return in this format
            data: {date=26-09-2025, logs={
            00:00:00={duration=1 min, zone=[0, 1], startTime=00:00:00},
            14:55:56={duration=10 mins, zone=[0, 1], startTime=14:55:56}}}*/
        db.collection(logHistoryCollectionPath).document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun getRangeDailyLog(fromDate: String, toDate:String){
        //todo
    }

}