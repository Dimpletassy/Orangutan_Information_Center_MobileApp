package com.oic.myapplication.services.database.models

import com.google.firebase.Timestamp

data class DailyLog(
    val date: String,
    val timestamp: Timestamp = Timestamp.now(),
    val logs: Map<String, IrrigationLog>
)

/* example json log */

//{
//    "2025-09-28": {
//    "date": "2025-09-28",
//    "logs": {
//        "03:54:26": {
//        "zone": ["0", "1"],
//        "scheduled": false,
//        "durationMins": 10,
//        "startTime": "03:54:26",
//        "endTime": "03:54:26"
//    }
//    },
//    "timestamp": {
//        "seconds": 1758995666,
//        "nanoseconds": 898000000
//    }
//}
//}
