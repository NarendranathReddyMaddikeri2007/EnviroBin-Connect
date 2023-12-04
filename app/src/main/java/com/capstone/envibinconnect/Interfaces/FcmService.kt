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
        "Authorization: key=AAAA33_jrnA:APA91bGVfCm5-MmhOaLjQnVlrsNaRK5hxeu7_LyWaYvgxTGm8lKi7KXx3MKz4H1DlWKhX3MB1j0uaYvyXRksiAUnVYAYt_TQYukU7ObVNWG_UBKGu-ChRABVrA27y3c0HYDSpv_KvEKp"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notificationRequest: FcmNotificationRequest): Call<ResponseBody>
}