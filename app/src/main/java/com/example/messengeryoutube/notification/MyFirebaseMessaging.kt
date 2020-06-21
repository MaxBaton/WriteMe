package com.example.messengeryoutube.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.example.messengeryoutube.messages.NewMessageActivity
import com.example.messengeryoutube.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessaging: FirebaseMessagingService() {
//    private var currentUser: User? = null
//    private var interlocutorUser: User? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && remoteMessage.data.isNotEmpty()) { //remoteMessage.data.isNorEmpty return FALSE!!!!!
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val message = if (remoteMessage.data["body"] != null) remoteMessage.data["body"] else "null"
        val title = if (remoteMessage.data["title"] != null) remoteMessage.data["title"] else "null"
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //val notificationMessage = NotificationMessage(currentUser = toUser!!,toUser = currentUser!!)
        val myNotification = NotificationMessage.createNotification(this, title!!,message!!)
        val channel = NotificationMessage.createNotificationChannel(this)
        if (channel != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                noti.createNotificationChannel(channel)
            }
            noti.notify(NotificationMessage.NOTIFICATION_ID,myNotification)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            updateToken(p0)
        }
    }

    private fun updateToken(refreshToken: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().getReference("/tokens")
        val token = Token(refreshToken!!)
        reference.child(currentUser!!.uid).setValue(token)
    }
}