package com.capstone.envibinconnect.Interfaces

interface TokenCallback {
    fun onTokenReceived(token : String)
    fun onTokenFailed()
}