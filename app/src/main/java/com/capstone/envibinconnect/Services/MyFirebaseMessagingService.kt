package com.capstone.envibinconnect.Services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.capstone.envibinconnect.Activities.MainActivity
import com.capstone.envibinconnect.DataClasses.ClientRequestData
import com.capstone.envibinconnect.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = "StatusNotificationChannel"
        const val NOTIFICATION_ID = 1
        const val TAG = "MyFirebaseMessageService"
    }
    private lateinit var database: FirebaseDatabase
    private lateinit var reference : DatabaseReference
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //Log.d(TAG,"onNewToken() : called")
        insertTokenIntoFirestore(token = token)
    }
    fun insertTokenIntoFirestore(token : String){
        val  firebaseAuth : FirebaseAuth = Firebase.auth
        if(firebaseAuth.currentUser!=null){
            val email : String? = firebaseAuth.currentUser?.email
            if(email!=null){
                val deviceToken = hashMapOf("token" to token,"timestamp" to FieldValue.serverTimestamp())
                Firebase.firestore.collection("users").document("${email}").update(deviceToken)
                    .addOnCompleteListener { task->
                    if(task.isSuccessful) println("\n\n\n----NEW FCM TOKEN IS UPDATED SUCCESSFULLY----\n\n\n")
                    else println("\n\n\n----NEW FCM TOKEN UPDATION IS FAILED----\n\n\n")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //Log.d(TAG, "onMessageReceived() : called")
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("manager")
        //Log.d(TAG, "onMessageReceived() : Firebase objects are initialized")
        //Log.d(TAG, "onMessageReceived() : Message received from : ${message.from}")
        val notification = message.notification
        val data = message.data

        if (notification != null && data != null) {
            //Log.d(TAG, "onMessageReceived() : message.data : $data")
            val title = notification.title
            val body = notification.body
            //Log.d(TAG, "onMessageReceived() : Title is $title and body is $body")
            val token = data["token"]
            val latitude: Double? = data["latitude"]?.toDoubleOrNull()
            val longitude: Double? = data["longitude"]?.toDoubleOrNull()
            val email = data["email"]
            //Log.d(TAG, "onMessageReceived() : user=$${token}, latitude=${data["latitude"]}, longitude=${data["longitude"]} & email=$${decodeEmail("${email}")}")
            if (title != null && body != null && token != null && latitude != null && longitude != null && email != null) {
                //Log.d(TAG, "onMessageReceived() : All Perfect")
                //Log.d(TAG, "onMessageReceived() : user=${token}, latitude=$latitude, longitude=$longitude & email=$email")
                insertIntoDatabase(ClientRequestData(
                    title = title,
                    body = body,
                    email = email,
                    latitude = latitude,
                    longitude = longitude,
                    token = token
                ))

            }
            //Log.d(TAG, "onMessageReceived() : showNotification() is going to call")
            showNotification(title = title, body = body)
        }
    }

    fun insertIntoDatabase(item: ClientRequestData) {
        val location = hashMapOf("email" to item.email,"latitude" to item.latitude, "longitude" to item.longitude)
        val user = UUID.randomUUID().toString().split("-")[0]
        reference.child(user).setValue(location).addOnSuccessListener {
            Log.d(TAG, "onMessageReceived() : insertIntoDatabase() is SUCCESSFUL")
        }
        .addOnFailureListener {
            Log.d(TAG, "onMessageReceived() : insertIntoDatabase() is FAILED")
        }
    }


    override fun onDeletedMessages() {
        super.onDeletedMessages()
        //Log.d(TAG,"onDeleteMessage() : called")
    }

    private fun showNotification(title: String?, body: String?) {
        createNotificationChannel()
        Log.d(TAG,"onMessageReceived() : createNotificationChannel() is called")
        sendStatusNotification(title,body)
        Log.d(TAG,"onMessageReceived() : sendStatusNotification() is called")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Status Notification Channel"
            val descriptionText = "Channel for sending status notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendStatusNotification(title: String?, body: String?) {
        // Create an explicit intent for the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        //Log.d(TAG,"onMessageReceived() : sendStatusNotification() : PendingIntent is created")
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
        //Log.d(TAG,"onMessageReceived() : sendStatusNotification() : Notification builder is created")
        with(NotificationManagerCompat.from(this@MyFirebaseMessagingService)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            Log.d(TAG,"onMessageReceived() : sendStatusNotification() : notify() is called")
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }

}
