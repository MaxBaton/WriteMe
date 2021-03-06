package com.example.writeMe

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu

fun Context.createPopupMenu(menuResource: Int,anchor: View): PopupMenu {
    val popupMenu = PopupMenu(this,anchor)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        popupMenu.gravity = Gravity.RIGHT
    }
    popupMenu.menuInflater.inflate(menuResource,popupMenu.menu)
    return popupMenu
}