package com.example.messengeryoutube.messages

import com.google.firebase.database.DataSnapshot

interface OnGetDataListener {
    fun onSuccess(dataSnapshot: DataSnapshot)
    fun onStart()
    fun onFailure()
}