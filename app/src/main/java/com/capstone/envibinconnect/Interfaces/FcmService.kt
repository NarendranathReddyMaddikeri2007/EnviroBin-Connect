package com.capstone.envibinconnect.Interfaces

import com.capstone.envibinconnect.DataClasses.FcmNotificationRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=/*GIVE AUTHORIZATION KEY HERE*/"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notificationRequest: FcmNotificationRequest): Call<ResponseBody>
}
