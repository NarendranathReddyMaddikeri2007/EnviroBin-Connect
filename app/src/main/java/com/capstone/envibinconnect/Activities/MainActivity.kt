package com.capstone.envibinconnect.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.capstone.envibinconnect.DataClasses.FcmNotification
import com.capstone.envibinconnect.DataClasses.FcmNotificationRequest
import com.capstone.envibinconnect.Interfaces.FcmService
import com.capstone.envibinconnect.Interfaces.LocationCallback
import com.capstone.envibinconnect.Others.GiveAlert
import com.capstone.envibinconnect.R
import com.capstone.envibinconnect.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alerts : GiveAlert

    companion object {
        const val NOTIFICATION_ID = 1
        const val REQUEST_CODE_LOCATION_PREMISSION = 3
        const val TAG = "MainActivity"
    }
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var firebaseAuth: FirebaseAuth
    private var EMAIL : String? = null
    private lateinit var SERVER_KEY : String
    private lateinit var firestoreDatabse : FirebaseFirestore
    private lateinit var collectionReference : CollectionReference

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.FOREGROUND_SERVICE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alerts = GiveAlert(this@MainActivity)
        //notify
        notificationManager = NotificationManagerCompat.from(this@MainActivity)

        SERVER_KEY = resources.getString(R.string.SERVER_KEY)
        firebaseAuth = Firebase.auth
        firestoreDatabse = Firebase.firestore
        collectionReference = firestoreDatabse.collection("users")
        EMAIL  = firebaseAuth.currentUser?.email
        onButtonClicks()


        if(intent!=null && intent.hasExtra("key1") && intent.extras!=null){
            for(key : String in intent.extras?.keySet()!!){
                 if(key!=null){
                     Log.d(TAG,"FIREBASE CLOUD MESSAGING -> INTENT EXTRA IS ${intent.extras!!.getString(key)}")
                 }
            }
        }

    }

    private fun onButtonClicks() {


        binding.sendRequestActivityMain.setOnClickListener {
            Log.d(TAG, "MainActivity.kt : trackATruckActivityMain button clicked")
            getLocation(object : LocationCallback {
                override fun onLocationResult(location: Pair<Double, Double>?) {
                    if (location != null) {

                        Log.d("Location", "Latitude: ${location.first}, Longitude: ${location.second}")
                        Log.d(TAG, "MainActivity.kt : latitude=${location?.first} longitude=${location?.second}")
                        if(location!=null && EMAIL!=null){
                            Log.d(TAG, "MainActivity.kt : location is RETRIEVED")
                            Log.d(TAG, "MainActivity.kt : latitude=${location.first} longitude=${location.second}")
                            val latitude = location.first
                            val longitude = location.second
                            collectionReference.document("narendranath@outlook.com").get().addOnSuccessListener { result->
                                val token = result.get("token")?.toString()
                                Log.d(TAG, "MainActivity.kt : FCM Token is retrieved from Firestore")
                                if(token!=null && !token.equals("token_") && token!="" && token!=" "){
                                    println("\n\n----------------GOING TO SEND MESSAGE----------------\n\n")
                                    val result : Boolean = sendNotification(
                                        title = "New request",
                                        body = "Received request from ${getUser(EMAIL!!)}",
                                        token = token,
                                        email = EMAIL!!,
                                        latitude = latitude,
                                        longitude = longitude
                                    )
                                }
                            }
                        }
                    } else {
                        // Handle the case where location is null
                        Log.d("Location", "Location is null")
                        alerts.sendSnackbar(message = "Location invalid", view = binding.root)
                    }
                }
            })
        }
    }


    fun getLocation(callback: LocationCallback) {
        if (isLocationEnabled(this)) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                callback.onLocationResult(null)
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val locationPair = Pair(it.latitude, it.longitude)
                    callback.onLocationResult(locationPair)
                } ?: run {
                    callback.onLocationResult(null)
                }
            }
        } else {
            alerts.sendSnackbar("Location not enabled",binding.root)
            callback.onLocationResult(null)
        }
    }

    fun openProfile(username : String){
        alerts.sendAlertDialog(
            title = "Profile",
            message = "Your username is ${username}",
            icon = R.drawable.baseline_person_24
        )
    }

    fun getUser(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex != -1) {
            email.substring(0, atIndex)
        }
        else  email
    }

    private fun isLocationEnabled(context: Context?): Boolean {
        val locManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun logout() {
        if(firebaseAuth.currentUser!=null){
            firebaseAuth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_LOCATION_PREMISSION) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {

            }
            else{
                alerts.sendSnackbar("Permissions Denied",binding.root)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun sendNotification(title: String, body: String, token: String, email: String, latitude : Double, longitude : Double): Boolean {
        Log.d(TAG, "MainActivity.kt : sendNotification() is STARTED")
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val fcmService = retrofit.create(FcmService::class.java)

            val notificationRequest = FcmNotificationRequest(
                to = token,
                notification = FcmNotification(title, body),
                data = mapOf("email" to email, "token" to token, "latitude" to "${latitude}", "longitude" to "${longitude}")
            )

            val call = fcmService.sendNotification(notificationRequest)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d(TAG,"Notification sent successfully to ${email}")
                        Log.d(TAG, "MainActivity.kt : sendNotification() : message sent SUCCESSFUL")
                    } else {
                        Log.d(TAG, "MainActivity.kt : sendNotification() : message sent FAILED")
                        Log.d(TAG,"Failed to send notification to $email. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG,"Error while sending notification: ${t.message}")
                    Log.d(TAG, "MainActivity.kt : sendNotification() : message sent FAILED : ${t.message}")

                }
            })
            return true
        } catch (e: Exception) {
            Log.d(TAG,"Error while sending notification: ${e.message}")
            Log.d(TAG, "MainActivity.kt : sendNotification() : Catch : message sent FAILED")
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.client_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.logout_menu_item -> logout()
            R.id.maps_menu_item -> gotoMaps()
            R.id.cprofile_menu_item -> firebaseAuth.currentUser?.email?.let { openProfile(it) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoMaps() {
        val intent = Intent(this@MainActivity, MapActivity::class.java)
        startActivity(intent)
    }
}