package com.example.messengeryoutube.notification

data class DataMessage(var title: String,var body: String) {
    constructor() : this("","")
}