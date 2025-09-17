package com.oic.myapplication.services.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

val auth = FirebaseAuth.getInstance()

fun firebaseSignup(firstName: String,
                   lastName: String,
                   email: String,
                   password: String,
                   onSuccess: (status: Boolean) -> Unit){

    /* CREATES USERS TO FIREBASE CLOUD AUTHENTICATION SERVICE USING EMAIL AND PASSWORD ONLY */
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseAuth", "createUserWithEmail:success")
                onSuccess(true)
                //todo: add ui
            } else {
                Log.w("FirebaseAuth", "createUserWithEmail:failure", task.exception)
                onSuccess(false)
                //todo: add error ui
            }
        }

}

fun firebaseLogin(email: String,
                  password: String,
                  onSuccess: (success: Boolean) -> Unit){
    /* LOGS IN REGISTERED USERS FROM FIREBASE */
    val status: String
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d("FirebaseAuth", "loginUserWithEmail: success")
                onSuccess(true)
            } else {
                Log.d("FirebaseAuth", "loginUserWithEmail: failure")
                onSuccess(false)
            }
        }
}