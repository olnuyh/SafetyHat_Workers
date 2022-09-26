package com.example.workers.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage

class FCMMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("onNewToken", "${token}")
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.takeIf { it.data.isNotEmpty() }?.apply {

        }
    }
}