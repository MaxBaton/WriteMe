package com.example.writeMe

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity

class CustomActionBar {
    companion object{
        fun customActionBar(activity: AppCompatActivity,title: String,isHomeButtonInlcude: Boolean){
            activity.supportActionBar?.title = title
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(activity.window.statusBarColor))
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(isHomeButtonInlcude)
        }
    }
}