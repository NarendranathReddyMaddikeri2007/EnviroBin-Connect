package com.capstone.envibinconnect.Others

import android.util.Log
import com.capstone.envibinconnect.Interfaces.TokenCallback
import com.google.firebase.messaging.FirebaseMessaging

class FCMToken {

    companion object{
        const val TAG = "FCMToken"
    }

    fun getClientToken(callback: TokenCallback) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result ?: ""
                    callback.onTokenReceived(token)
                    println("FIREBASE CLOUD MESSAGING PUSH_TOKEN ->$token")
                    Log.i(TAG, "pushToken: $token")
                } else {
                    callback.onTokenFailed()
                    println("FIREBASE CLOUD MESSAGING PUSH_TOKEN CREATION FAILED")
                    Log.i(TAG, "TOKEN CREATION FAILED")
                }
            }
    }
}