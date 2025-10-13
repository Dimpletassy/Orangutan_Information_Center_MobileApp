package com.oic.myapplication.services.database.models

// todo: create enum zone
data class IrrigationLog(
    val startTime: String,
    val endTime: String,
    val zone: List<String>, // 0, 1
    val litres: Int,
    val scheduled: Boolean // if manual then false
)

//"logs": {
//        "03:54:26": {
//        "zone": ["0", "1"],
//        "scheduled": false,
//        "durationMins": 10,
//        "startTime": "03:54:26",
//        "endTime": "03:54:26"
//    }