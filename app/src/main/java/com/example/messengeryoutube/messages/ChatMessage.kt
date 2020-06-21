package com.example.messengeryoutube.messages

data class ChatMessage(val id: String,val text: String,val fromUserId: String,val toUserId: String,val timestamp: Long) {
    constructor(): this("","","","",-1)
}