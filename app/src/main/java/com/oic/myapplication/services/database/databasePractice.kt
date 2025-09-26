package com.oic.myapplication.services.database

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun databasePractice (){
    val db = Firebase.firestore
    val dbController = databaseController()

    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val time2 = ("00:00:00")

    val zone = listOf<String>("0", "1")

    val irrigationLog = IrrigationLog(startTime = time, zone = zone, duration = "10 mins")
    val irrigationLog2 = IrrigationLog(startTime = time2, zone = zone, duration = "1 min")

    val logList: Map<String, IrrigationLog> = mapOf(
        irrigationLog.startTime to irrigationLog
    )
    val dailyLog = DailyLog(date = date, logList)

    //todo: add daily log with irrigation log, then add a new log
    dbController.getDailyLog(date)
}