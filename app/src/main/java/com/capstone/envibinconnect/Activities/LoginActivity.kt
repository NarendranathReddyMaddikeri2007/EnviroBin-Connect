package com.capstone.envibinconnect.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.envibinconnect.Interfaces.TokenCallback
import com.capstone.envibinconnect.Others.FCMToken
import com.capstone.envibinconnect.Others.GiveAlert
import com.capstone.envibinconnect.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var firestoreDatabse : FirebaseFirestore
    private lateinit var collectionReference : CollectionReference
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedpreferences : SharedPreferences
    private lateinit var alerts : GiveAlert
    val fcmToken = FCMToken()


    companion object{
        const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        alerts = GiveAlert(this@LoginActivity)
        sharedpreferences = this@LoginActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        setFirebase()
        onButtonClicks()
    }

    private fun onButtonClicks() {
        binding.btnSignIn.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(checkAllFields(email,password)){
                val id : Int = binding.groupradio.checkedRadioButtonId
                var radioButton : RadioButton? = null
                if (id!=-1 && id!=null){
                    radioButton = findViewById(id)
                }
                val user = radioButton?.text
                var result = ""
                if(user!=null && user?.equals("House Holder") == true && isGmail(email)==true){
                    result = "client"
                }
                else if(user!=null && user?.equals("Municipal Officer") == true && isOutlook(email)==true){
                    result = "manager"
                }
                if(!result.isNullOrEmpty()){
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                        if(it.isSuccessful){
                            fcmToken.getClientToken(object : TokenCallback {
                                override fun onTokenReceived(token: String) {
                                    val TOKEN = hashMapOf("token" to "${token}","timestamp" to FieldValue.serverTimestamp())
                                    val documentReference = collectionReference.document("${email}")
                                    documentReference.update(TOKEN)
                                        .addOnSuccessListener {
                                            Log.d(TAG,"TOKEN UPDATED INTO FIRESTORE DATABASE SUCCESSFULLY")
                                        }
                                        .addOnFailureListener {
                                            Log.d(TAG,"TOKEN UPDATING INTO FIRESTORE DATABASE FAILED")
                                        }
                                }
                                override fun onTokenFailed() {
                                    Log.d(TAG,"Token retrieval failed & data inserting into firestore failed")
                                }
                            })
                            //----------------------------------------------------------------------
                            alerts.sendSnackbar("Successfully Sign-In!!",binding.root)
                            saveEmail(email = email, password = password)
                            if(result.equals("client")==true){
                                saveUserType(result)
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else if(result.equals("manager")){
                                saveUserType(result)
                                val intent = Intent(this, ManagerActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        else{
                            Log.e("error: ", it.exception.toString())
                            Log.d(TAG,"LOGIN FAILED DURING AUTHORIZATION1")
                        }
                    }
                }
                else{
                    alerts.sendSnackbar("Selected wrong input",binding.root)
                }
            }
            else{
                Log.d(TAG,"LOGIN FAILED DURING AUTHORIZATION1")
            }
        }
    }

    private fun saveEmail(email: String, password: String) {
        val editor = sharedpreferences.edit()
        editor.putString("EMAIL", email)
        editor.putString("PASSWORD",password)
        editor.apply()
    }

    private fun saveUserType(type: String) {
        val edit : SharedPreferences.Editor = sharedpreferences.edit()
        edit.putString("user",type)
        edit.apply()
    }

    fun isGmail(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@gmail.com")
    }

    fun isOutlook(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@outlook.com")
    }

    private fun checkAllFields(email : String, password: String): Boolean{
        if(email == ""){
            binding.etEmailLayout.error = "Email is required field"
            return false
        }
        if(password == ""){
            binding.etPasswordLayout.error = "Password is required field"
            binding.etPasswordLayout.errorIconDrawable = null
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLayout.error = "Check email format"
            return false
        }
        if(binding.etPassword.length() < 8){
            binding.etPasswordLayout.error = "Password should be at least 8 characters long"
            binding.etPasswordLayout.errorIconDrawable = null
            return false
        }
        return true
    }

    fun gotoRegister(view: View) {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setFirebase() {
        firebaseAuth = Firebase.auth
        firestoreDatabse = FirebaseFirestore.getInstance()
        collectionReference = firestoreDatabse.collection("users")
    }
}