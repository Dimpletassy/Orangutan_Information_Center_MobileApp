package com.oic.myapplication.services.database

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.oic.myapplication.helper.getcurDate
import com.oic.myapplication.helper.getcurTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun databasePractice (){
    val db = Firebase.firestore
    val dbController = databaseController()

    val date = getcurDate()
    val time = getcurTime()
    val timestamp = Timestamp.now()
    val zones = listOf("0", "1")
    val irrigationLog = IrrigationLog(startTime = time, endTime = time, zone = zones, durationMins = 10, scheduled = false)
    val logList: Map<String, IrrigationLog> = mapOf(
        irrigationLog.startTime to irrigationLog)

    val dailyLog = DailyLog(date=date, timestamp=timestamp, logs=logList)
    dbController.createDailyLog(dailyLog)


    // Calculate 2 days ago
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -2) // subtract 2 days

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date2DaysAgo = dateFormat.format(cal.time)
    val timestamp2DaysAgo = Timestamp(cal.time)

    // Create irrigation log
    val time2DaysAgo = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(cal.time)
    val zones2 = listOf("0", "1")
    val irrigationLog2 = IrrigationLog(
        startTime = time2DaysAgo,
        endTime = time2DaysAgo,
        zone = zones2,
        durationMins = 10,
        scheduled = false
    )

    // Put the log in a map keyed by startTime
    val logList2: Map<String, IrrigationLog> = mapOf(
        irrigationLog.startTime to irrigationLog
    )

    // Create DailyLog for 2 days ago
    val dailyLog2DaysAgo = DailyLog(
        date = date2DaysAgo,
        timestamp = timestamp2DaysAgo,
        logs = logList2
    )
    dbController.createDailyLog(dailyLog2DaysAgo)

    //check get range
    // Calculate 2 days ago
    val cal2 = Calendar.getInstance()
    cal2.add(Calendar.DAY_OF_YEAR, -1) // subtract 2 days

    val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date4DaysAgo = dateFormat2.format(cal2.time)
    dbController.getRangeDailyLog(date4DaysAgo, date)

    // Usage:
    dbController.getDailyLog("2025-09-30") { data ->
        if (data != null) {
            Log.d("firestore", "DailyLog: $data")
        } else {
            Log.d("firestore", "No document found")
        }
    }
}