package com.example.messengeryoutube.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.*
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.example.messengeryoutube.messages.NewMessageActivity
import com.example.messengeryoutube.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi


class MyFirebaseMessaging: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && remoteMessage.data.isNotEmpty()) {
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val message = remoteMessage.data["body"]
        val title   = remoteMessage.data["title"]
        val currentUserJson = remoteMessage.data["currentUser"]
        val interlocutorUserJson = remoteMessage.data["interlocutorUser"]
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //convert users from JSON
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(User::class.java)
        val currentUser = jsonAdapter.fromJson(currentUserJson!!)
        val interlocutorUser = jsonAdapter.fromJson(interlocutorUserJson!!)

        val notificationMessage = NotificationMessage(currentUser = interlocutorUser!!,interlocutorUser = currentUser!!)
        val myNotification = notificationMessage.createNotification(this, title!!,message!!)
        val channel = notificationMessage.createNotificationChannel(this)
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