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
import androidx.appcompat.app.AppCompatActivity
import com.capstone.envibinconnect.Interfaces.TokenCallback
import com.capstone.envibinconnect.Others.FCMToken
import com.capstone.envibinconnect.Others.GiveAlert
import com.capstone.envibinconnect.R
import com.capstone.envibinconnect.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity(), TokenCallback {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDatabse : FirebaseFirestore
    private lateinit var collectionReference : CollectionReference
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sharedpreferences :SharedPreferences
    val fcmToken = FCMToken()
    private lateinit var alert : GiveAlert
    companion object{
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        alert = GiveAlert(context = this@RegisterActivity)
        sharedpreferences = this@RegisterActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        setContentView(binding.root)
        setFirebase()
        onButtonClicks()
    }

    private fun onButtonClicks() {
        binding.btnSignUp.setOnClickListener{
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            if(checkAllFields()){
                if(isUserAlreadyRegistered(email,password)){
                    alert.sendSnackbar(message = "Already registered", view = findViewById(R.id.linearlayout_snackbar_layout))
                }
                else{
                    val id : Int = binding.groupradioRegister.checkedRadioButtonId
                    var radioButton : RadioButton? = null
                    if (id!=-1 && id!=null){
                        radioButton = findViewById(id)
                    }
                    val user = radioButton?.text
                    var result = ""
                    if(user!=null && user?.equals("House Holder") == true && isGmail(email)==true){
                        alert.sendSnackbar(
                            message = "Give client data",
                            view = binding.root
                        )
                        result = "client"
                    }
                    else if(user!=null && user?.equals("Municipal Officer") == true && isOutlook(email)==true){
                        alert.sendSnackbar(
                            message = "Give manager data",
                            view = binding.root
                        )
                        result = "manager"
                    }
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                        if(it.isSuccessful){
                            saveEmail(email,password)
                            getAndInsertTokenIntoFirestore(email)
                            if(result.equals("client")==true){
                                saveUserType(result)
                                alert.sendSnackbar(
                                    message = "Client Account created successfully!!",
                                    view = binding.root
                                )
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else if(result.equals("manager")==true){
                                saveUserType(result)
                                alert.sendSnackbar(
                                    message = "Manager Account created successfully!!",
                                    view = binding.root
                                )
                                val intent = Intent(this@RegisterActivity, ManagerActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        else{
                            Log.e(TAG, it.exception.toString())
                        }
                    }
                }
            }
        }
    }

    fun isGmail(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@gmail.com")
    }

    private fun saveUserType(type: String) {
        val edit : SharedPreferences.Editor = sharedpreferences.edit()
        edit.putString("user",type)
        edit.apply()
    }

    fun isOutlook(mail: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(mail).matches() && mail.endsWith("@outlook.com")
    }

    private fun saveEmail(email: String, password: String) {
        val editor = sharedpreferences.edit()
        editor.putString("EMAIL", email)
        editor.putString("PASSWORD",password)
        editor.apply()
    }

    private fun getAndInsertTokenIntoFirestore(email: String) {
        fcmToken.getClientToken(object : TokenCallback {
            override fun onTokenReceived(token: String) {
                // Handle the token
                Log.d(TAG,"Received token: ${token}")
                val TOKEN = hashMapOf(
                    "token" to "${token}",
                    "timestamp" to FieldValue.serverTimestamp(),
                )
                //------------------
                Log.d(TAG,"BEFORE INSERTING INTO FIRESTORE -> FIREBASE CLOUD MESSAGING PUSH_TOKEN ->")
                val documentReference = collectionReference.document("${email}")
                documentReference.set(TOKEN)
                    .addOnSuccessListener {
                        Log.d(TAG,"TOKEN INSERTED INTO FIRESTORE DATABASE SUCCESSFULLY")
                    }
                    .addOnFailureListener {
                        Log.d(TAG,"TOKEN INSERTING INTO FIRESTORE DATABASE FAILED")
                    }
            }
            override fun onTokenFailed() {
                // Handle token retrieval failure
                Log.d(TAG,"Token retrieval failed & data inserting into firestore failed")
            }
        })
    }


    private fun isUserAlreadyRegistered(email: String, password: String): Boolean {
        val documentReference = collectionReference.document("${email}")
        var result = false
        documentReference.get().addOnCompleteListener{task->
            if (task.isSuccessful) {
                val document = task.result
                if(document != null) {
                    if (document.exists()) {
                        true
                    } else {
                        result = false
                    }
                }
            } else {
                false
            }
        }
        return result
    }


    private fun checkAllFields(): Boolean{
        val email = binding.etEmailRegister.text.toString()
        val password = binding.etPasswordRegister.text.toString()
        val cpassword = binding.etConfirmPasswordRegister.text.toString()
        if(email == ""){
            binding.etEmailLayoutRegister.error = "Email is required field"
            return false
        }
        if(password == ""){
            binding.etPasswordLayoutRegister.error = "Password is required field"
            binding.etPasswordLayoutRegister.errorIconDrawable = null
            return false
        }
        if(cpassword == ""){
            binding.etConfirmPasswordLayoutRegister.error = "Confirm password is required field"
            binding.etConfirmPasswordLayoutRegister.errorIconDrawable = null
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLayoutRegister.error = "Check email format"
            return false
        }
        if(binding.etPasswordRegister.length() < 8){
            binding.etPasswordLayoutRegister.error = "Password should be at least 8 characters long"
            binding.etPasswordLayoutRegister.errorIconDrawable = null
            return false
        }
        if(password != cpassword){
            binding.etConfirmPasswordLayoutRegister.error = "Password does not match"
            return false
        }
        return true
    }

    private fun setFirebase() {
        firebaseAuth = Firebase.auth
        firestoreDatabse = Firebase.firestore
        collectionReference = firestoreDatabse.collection("users")
    }

    fun gotoRLogin(view: View) {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onTokenReceived(token: String) {
        //
    }

    override fun onTokenFailed() {
        //
    }
}
