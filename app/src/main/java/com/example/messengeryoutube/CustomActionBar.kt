package com.example.messengeryoutube

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity

class CustomActionBar {
    companion object{
        fun editProfileActionBar(activity: AppCompatActivity){
            activity.supportActionBar?.title = "Профиль"
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(activity.window.statusBarColor))
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        fun latestMessagesActivityActionBar(activity: AppCompatActivity, menu: Menu?) {
            activity.supportActionBar?.title = "Диалоги"
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(activity.window.statusBarColor))
        }
    }
}