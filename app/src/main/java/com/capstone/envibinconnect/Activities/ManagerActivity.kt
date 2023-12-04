package com.capstone.envibinconnect.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.envibinconnect.Adapters.ManagerAdapter
import com.capstone.envibinconnect.DataClasses.ClientRequestData
import com.capstone.envibinconnect.DataClasses.NotificationItem
import com.capstone.envibinconnect.Interfaces.NotificationListener
import com.capstone.envibinconnect.Interfaces.onRequestAccept
import com.capstone.envibinconnect.Others.GiveAlert
import com.capstone.envibinconnect.R
import com.capstone.envibinconnect.databinding.ActivityManagerBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagerActivity : AppCompatActivity(), NotificationListener, onRequestAccept{
    private lateinit var binding: ActivityManagerBinding
    private lateinit var adapter: ManagerAdapter
    private lateinit var notificationList: MutableList<NotificationItem>
    private lateinit var database: FirebaseDatabase
    private lateinit var reference : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var alerts : GiveAlert

    companion object{
        const val TAG = "ManagerActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        binding = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("manager")
        firebaseAuth = Firebase.auth
        notificationList = mutableListOf()
        alerts = GiveAlert(this@ManagerActivity)


        binding.recyclerViewActivityManager.layoutManager = LinearLayoutManager(this@ManagerActivity)

        val adapter = ManagerAdapter(this@ManagerActivity, notificationList, this)
        binding.recyclerViewActivityManager.adapter = adapter
        trackDataChanges(adapter)
    }

    private fun trackDataChanges(adapter: ManagerAdapter) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val managerList = mutableListOf<NotificationItem>()
                for (childSnapshot in snapshot.children){
                    val gmail = childSnapshot.child("email").value.toString()
                    val latitude = childSnapshot.child("latitude").value.toString().toDouble()
                    val longitude= childSnapshot.child("longitude").value.toString().toDouble()
                    managerList.add(NotificationItem(email = gmail, latitude = latitude, longitude = longitude))
                }
                adapter.updateData(managerList)
            }

            override fun onCancelled(e: DatabaseError) {
                //Handle Errors
                Log.d(TAG,"ERROR ManagerActivity.kt/trackDataChanges()/onCancelled()")
                print(e)
                print("\n/--------------------/\n")
                e.details
                print("\n/--------------------/\n")
                e.message
                print("\n/--------------------/\n")
                e.toString()
                print("\n\n/---------------------ERROR---------------------/\n\n")
            }

        })
    }

    override fun onNotificationReceived(item: ClientRequestData) {
            val location = hashMapOf("latitude" to item.latitude, "longitude" to item.longitude)
            reference.child(item.email).setValue(location).addOnSuccessListener {it->
                Log.d(TAG,"ManagerActivity/onNotificationReceived()/addOnSuccessListener/-> $it")
            }
            .addOnFailureListener {it->
                Log.d(TAG,"ManagerActivity/onNotificationReceived()/addOnFailureListener/-> ${it.message}")
                it.printStackTrace()
            }
    }

    override fun onAcceptRequest(position: Int) {
        if (position in 0 until notificationList.size) {
            notificationList.removeAt(position)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manager_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.logout_menu_item -> logout()
            R.id.mmaps_menu_item -> gotoMaps()
            R.id.users_map_menu_item -> gotoUsersMap()
            R.id.cprofile_menu_item -> firebaseAuth.currentUser?.email?.let { openProfile(it) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoUsersMap() {
        val intent = Intent(this@ManagerActivity, UsersMapActivity::class.java)
        startActivity(intent)
    }

    fun openProfile(username : String){
        alerts.sendAlertDialog(
            title = "Profile",
            message = "Your username is ${username}",
            icon = R.drawable.baseline_person_24
        )
    }

    private fun gotoMaps() {
        val intent = Intent(this@ManagerActivity, MapActivity::class.java)
        startActivity(intent)
    }

    fun logout(){
        if(firebaseAuth.currentUser!=null){
            firebaseAuth.signOut()
            val intent = Intent(this@ManagerActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}