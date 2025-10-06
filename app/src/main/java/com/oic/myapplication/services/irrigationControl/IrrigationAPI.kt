package com.oic.myapplication.services.irrigationControl

/* REFERRING TO API DOCUMENTATION UNDER THE PROJECTS DOCUMENTATION FOLDER */
val API_KEY = ""

val local_machine = "http://192.168.1.133"
val API_BASE = "${local_machine}:3000/api/v1/"


fun runZoneAPI(zone_id: Int): String {
    return("${API_BASE}/action=run&zone_id=${zone_id}")
}

fun runAllZonesAPI(): String {
    return("${API_BASE}/?action=runall")
}

fun stopZoneAPI(zone_id: Int): String {
    return("${API_BASE}/action=stop&zone_id=${zone_id}")
}

fun stopAllZonesAPI(): String {
    return("${API_BASE}/action=stopall")
}

