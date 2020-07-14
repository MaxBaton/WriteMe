package com.example.writeMe.registration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val id: String, var userName: String, var imageUrl: String,var status: String = "offline"): Parcelable {
    constructor(): this("","","","")
}