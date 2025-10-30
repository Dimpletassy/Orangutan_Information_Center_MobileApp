package com.oic.myapplication.services.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthService {

    companion object {
        private const val TAG = "FirebaseAuth"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Signs up a new user with email and password.
     */
    fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onResult: (success: Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    onResult(true)
                    // TODO: Add UI feedback
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    onResult(false)
                    // TODO: Add error UI feedback
                }
            }
    }

    /**
     * Logs in a registered user with email and password.
     */
    fun login(
        email: String,
        password: String,
        onResult: (success: Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "loginUserWithEmail:success")
                    onResult(true)
                } else {
                    Log.w(TAG, "loginUserWithEmail:failure", task.exception)
                    onResult(false)
                }
            }
    }
}
