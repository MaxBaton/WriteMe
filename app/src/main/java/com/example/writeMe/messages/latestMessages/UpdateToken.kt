package com.example.writeMe.messages.latestMessages

import com.example.writeMe.notification.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UpdateToken {
    fun updateToken(refreshToken: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().getReference("/tokens")
        val token = Token(refreshToken!!)
        reference.child(currentUser!!.uid).setValue(token)
    }
}