package com.oic.myapplication.ui.screens.scheduling

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Shared state that survives navigation between screens. */
object IrrigationRepo {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun start() { _isRunning.value = true }
    fun stop()  { _isRunning.value = false }
}
