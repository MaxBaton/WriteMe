package com.example.messengeryoutube.registration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val id: String, var userName: String, var imageUrl: String): Parcelable {
    constructor(): this("","","")
}