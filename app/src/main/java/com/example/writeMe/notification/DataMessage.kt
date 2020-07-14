package com.example.writeMe.notification

data class DataMessage(var title: String,var body: String,var currentUser: String,var interlocutorUser: String) {
    constructor() : this("","","","")
}