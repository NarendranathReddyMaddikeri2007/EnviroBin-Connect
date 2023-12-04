package com.capstone.envibinconnect.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.capstone.envibinconnect.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedpreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        sharedpreferences  = this@SplashScreenActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        firebaseAuth = Firebase.auth

        if(intent!=null && intent.hasExtra("key1") && intent.extras!=null){
            for(key : String in intent.extras?.keySet()!!){
                if(key!=null){
                    println("\n\n------------FIREBASE CLOUD MESSAGING -> INTENT EXTRA IS ${intent.extras!!.getString(key)} -----------\n\n")
                }
            }
        }
            lifecycleScope.launch {
                delay(3000)
                decideNavigation()
            }
    }

    private fun decideNavigation() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val type = sharedpreferences.getString("user","")
            val email = sharedpreferences.getString("EMAIL","")
            if(type.isNullOrEmpty()==false && email.isNullOrEmpty()==false){
                if(type.equals("client") && isGmail(email)){
                    val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if(type.equals("manager") && isOutlook(email)){
                    val intent = Intent(this@SplashScreenActivity, ManagerActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        } else {
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun isGmail(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@gmail.com")
    }

    fun isOutlook(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@outlook.com")
    }
}