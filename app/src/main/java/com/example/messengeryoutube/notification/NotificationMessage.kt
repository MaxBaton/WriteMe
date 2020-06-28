package com.example.messengeryoutube.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.messengeryoutube.R
import com.example.messengeryoutube.messages.ChatLogActivity
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.example.messengeryoutube.messages.NewMessageActivity
import com.example.messengeryoutube.registration.User

class NotificationMessage(val currentUser: User,val interlocutorUser: User) {
    companion object{
        const val CHANNEL_ID = "channel id"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_NAME = "уведомление"
        const val CHANNEL_DESCRIPTION = "оповещение о новом сообщении"
        const val PENDING_INTENT_ID = 1
    }

        fun createNotification(context: Context,userName: String,message: String): Notification {
            val intent = Intent(context,ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.INTERLOCUTOR_USER,interlocutorUser)
            intent.putExtra(LatestMessagesActivity.CURRENT_USER_KEY,currentUser)
            val taskStackBuilder = TaskStackBuilder.create(context)
                                    .addParentStack(ChatLogActivity::class.java)
                                    .addNextIntent(intent)
            val pendingIntent = taskStackBuilder.getPendingIntent(PENDING_INTENT_ID,PendingIntent.FLAG_UPDATE_CURRENT)
            return NotificationCompat.Builder(context, CHANNEL_ID)
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
                val name = CHANNEL_NAME
                val descriptionText = CHANNEL_DESCRIPTION
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