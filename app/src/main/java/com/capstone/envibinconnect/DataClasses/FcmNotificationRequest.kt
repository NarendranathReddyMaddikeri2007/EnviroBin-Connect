package com.capstone.envibinconnect.DataClasses

import com.capstone.envibinconnect.DataClasses.FcmNotification

data class FcmNotificationRequest(
    val to: String,
    val notification: FcmNotification,
    val data: Map<String, String>
)