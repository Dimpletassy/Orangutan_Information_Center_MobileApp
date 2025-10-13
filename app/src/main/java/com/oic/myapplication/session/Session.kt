
package com.example.myapplication.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Session {
    var isLoggedIn by mutableStateOf(false)
    fun login() { isLoggedIn = true }
    fun logout() { isLoggedIn = false }
}
