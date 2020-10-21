package com.example.writeMe.messages.latestMessages

import android.content.Intent
import com.example.writeMe.registration.MainActivity
import com.google.firebase.auth.FirebaseAuth

object VerificationExistingUser {
    fun verify(latestMessagesActivity: LatestMessagesActivity): Intent? {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(latestMessagesActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }else return null
    }
}