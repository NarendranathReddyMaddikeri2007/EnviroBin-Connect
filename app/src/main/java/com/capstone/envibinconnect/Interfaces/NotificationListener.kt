package com.capstone.envibinconnect.Interfaces

import com.capstone.envibinconnect.DataClasses.ClientRequestData
import com.capstone.envibinconnect.DataClasses.NotificationItem

interface NotificationListener {
    fun onNotificationReceived(item: ClientRequestData)
}