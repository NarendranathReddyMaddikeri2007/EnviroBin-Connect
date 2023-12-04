package com.capstone.envibinconnect.Interfaces

interface LocationCallback {
    fun onLocationResult(locationPair: Pair<Double, Double>?)
}