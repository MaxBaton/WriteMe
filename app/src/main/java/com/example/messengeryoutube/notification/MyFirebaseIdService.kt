//package com.example.messengeryoutube.notification
//
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.iid.FirebaseInstanceId
//import com.google.firebase.messaging.FirebaseMessagingService
//
//class MyFirebaseIdService: FirebaseMessagingService() {
//    override fun onNewToken(p0: String) {
//        super.onNewToken(p0)
//
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val refreshToken = FirebaseInstanceId.getInstance().token
//
//        if (currentUser != null) {
//            updateToken(refreshToken)
//        }
//    }
//
//    private fun updateToken(refreshToken: String?) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//
//        val reference = FirebaseDatabase.getInstance().getReference("/tokens")
//        val token = Token(refreshToken!!)
//        reference.child(currentUser!!.uid).setValue(token)
//    }
//}