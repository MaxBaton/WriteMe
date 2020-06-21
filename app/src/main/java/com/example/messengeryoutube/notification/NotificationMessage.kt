package com.example.messengeryoutube.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.messengeryoutube.messages.ChatLogActivity
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.example.messengeryoutube.messages.NewMessageActivity
import com.example.messengeryoutube.registration.User

class NotificationMessage(val currentUser: User,val toUser: User) {
    companion object{
        const val CHANNEL_ID = "channel notification message (id)"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_NAME = "пока такое имя"
        const val CHANNEL_DESCRIPTION = "пока такое описание"
        const val PENDING_INTENT_ID = 1

        fun createNotification(context: Context,userName: String,message: String): Notification {
            val intent = Intent(context,LatestMessagesActivity::class.java)
            //intent.putExtra(NewMessageActivity.INTERLOCUTOR_USER,toUser)
            //intent.putExtra(LatestMessagesActivity.CURRENT_USER_KEY,currentUser)
            val pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT)
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setContentTitle(userName)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()!!
        }

        fun createNotificationChannel(context: Context): NotificationChannel? {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name =
                    CHANNEL_NAME
                val descriptionText =
                    CHANNEL_DESCRIPTION
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                return channel
            }
            return null
        }
    }
}