package com.example.writeMe.messages.latestMessages

import android.content.Intent
import com.example.writeMe.messages.chatLog.ChatLogActivity
import com.example.writeMe.messages.newMessages.NewMessageActivity
import com.example.writeMe.registration.User

object GoOverInChat {
    fun go(latestMessagesActivity: LatestMessagesActivity,
           currentUser: User,
           interlocutorUserInGroupAdapter: LatestMessagesActivity.LatestMessageItem) {
        val intent = Intent(latestMessagesActivity, ChatLogActivity::class.java)
        intent.putExtra(NewMessageActivity.INTERLOCUTOR_USER,interlocutorUserInGroupAdapter.interlocutorUser)
        intent.putExtra(LatestMessagesActivity.CURRENT_USER_KEY,currentUser)
        latestMessagesActivity.startActivity(intent)
    }
}