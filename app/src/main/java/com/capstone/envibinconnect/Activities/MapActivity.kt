package com.capstone.envibinconnect.Activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.capstone.envibinconnect.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var gMap : GoogleMap
    lateinit var API_KEY : String
    private lateinit var databaseReference: DatabaseReference
    companion object{
        const val TAG = "MapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        API_KEY = resources.getString(R.string.API_KEY)
        databaseReference = FirebaseDatabase.getInstance().reference.child("locations")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment_activity_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, this.API_KEY)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        addMarkers()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gMap.clear()
                addMarkers()
            }
            override fun onCancelled(e: DatabaseError) {
                // Handle database error
                Log.d(TAG,"MapActivity/onMapReady()/databaseReference.addValueEventListener()/onCancelled()")
                Log.d(TAG,"error is ${e}")
                Log.d(TAG,"error details are ${e.details}")
                Log.d(TAG,"error message is ${e.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun addMarkers() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val builder = LatLngBounds.Builder()
                for (placeSnapshot in snapshot.children) {
                    val latitude = placeSnapshot.child("latitude").value as Double
                    val longitude = placeSnapshot.child("longitude").value as Double
                    val location = LatLng(latitude, longitude)
                    gMap.addMarker(MarkerOptions().position(location).title(placeSnapshot.key))
                        ?.setIcon(bitmapFromVector(this@MapActivity,
                            R.drawable.baseline_directions_bus_24
                        ))
                }
                // Use builder to set padding for markers
                for (placeSnapshot in snapshot.children) {
                    val latitude = placeSnapshot.child("latitude").value as Double
                    val longitude = placeSnapshot.child("longitude").value as Double
                    builder.include(LatLng(latitude, longitude))
                }
                val bounds = builder.build()
                val padding = 200
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                gMap.moveCamera(cameraUpdate)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    fun bitmapFromVector(context : Context, vectorId : Int) : BitmapDescriptor? {
        val vectorDrawable : Drawable? = ContextCompat.getDrawable(context,vectorId)
        vectorDrawable?.setBounds(0,0,vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap : Bitmap? = vectorDrawable?.intrinsicHeight?.let {
            vectorDrawable?.intrinsicWidth?.let { it1 ->
                Bitmap.createBitmap(
                    it1,
                    it, Bitmap.Config.ARGB_8888)
            }
        }
        val convas : Canvas? = bitmap?.let { Canvas(it) }
        if (convas != null) {
            vectorDrawable.draw(convas)
        }
        return bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
    }
}