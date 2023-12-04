package com.capstone.envibinconnect.DataClasses

data class ClientRequestData(
    val title: String,
    val body: String,
    val email: String,
    val token : String,
    val longitude: Double,
    val latitude : Double
)
