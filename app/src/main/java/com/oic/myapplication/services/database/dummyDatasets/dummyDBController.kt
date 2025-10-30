package com.oic.myapplication.services.database.dummyDatasets

import android.util.Log
import com.google.gson.Gson
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.DailyLog
import com.oic.myapplication.services.database.models.IrrigationLog

fun populateDatabaseFromAssets() {
    val gson = Gson()

    try {

        val dbController = DatabaseController()

        // --- Push each log to Firestore ---
        val irrigationLog = IrrigationLog(
            startTime = "10:15:00",
            endTime = "11:15:00",
            scheduled = false,
            litres = 20,
            zone = listOf("0")
        )

        val irrigationLog2 = IrrigationLog(
            startTime = "12:00:00",
            endTime = "13:00:00",
            scheduled = false,
            litres = 20,
            zone = listOf("0")
        )

        val irrigationLog3 = IrrigationLog(
            startTime = "06:30:00",
            endTime = "07:30:00",
            scheduled = false,
            litres = 20,
            zone = listOf("0")
        )

        val logList: Map<String, IrrigationLog> = mapOf(
            irrigationLog.startTime to irrigationLog,
            irrigationLog2.startTime to irrigationLog2
        )

        val logList2: Map<String, IrrigationLog> = mapOf(
            irrigationLog2.startTime to irrigationLog2,
            irrigationLog3.startTime to irrigationLog3
        )

        val logList3: Map<String, IrrigationLog> = mapOf(
            irrigationLog.startTime to irrigationLog
        )

        val logList4: Map<String, IrrigationLog> = mapOf(
            irrigationLog.startTime to irrigationLog
        )

        val dailyLog = DailyLog(
            date = "2025-09-11",
            logs = logList
        )

        val dailyLog2 = DailyLog(
            date = "2025-09-12",
            logs = logList2
        )

        val dailyLog3 = DailyLog(
            date = "2025-09-13",
            logs = logList3
        )

        val dailyLog4 = DailyLog(
            date = "2025-09-14",
            logs = logList4
        )

        dbController.createDailyLog(dailyLog)
        dbController.createDailyLog(dailyLog2)
        dbController.createDailyLog(dailyLog3)
        dbController.createDailyLog(dailyLog4)

        Log.d("Firestore", "✅ Database population complete (4 daily logs)")

    } catch (e: Exception) {
        Log.e("Firestore", "❌ Error reading or parsing file: ${e.message}", e)
    }
}
