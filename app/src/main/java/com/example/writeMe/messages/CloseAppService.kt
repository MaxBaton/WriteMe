package com.example.writeMe.messages

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CloseAppService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val reference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser!!.uid}")
        reference.child("status").setValue("offline")
        Thread.sleep(100) // service doesn't have time to work
    }
}