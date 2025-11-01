package com.oic.myapplication.services.database.models

data class ManualRunDoc(
    val running: Boolean = false,
    val startAtEpochMs: Long = 0L,      // when manual run started
    val totalLitres: Int = 0,           // user-selected litres at start
    val usedLitres: Int? = null,        // set on stop (early or auto)
    val remainingLitres: Int? = null,   // set on stop (early or auto)
    val stoppedAtEpochMs: Long? = null  // set on stop (early or auto)
)
