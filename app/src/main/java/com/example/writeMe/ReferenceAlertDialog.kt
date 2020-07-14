package com.example.writeMe

import android.app.AlertDialog
import android.os.Build
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


fun AppCompatActivity.createReferenceAlertDialog(): AlertDialog {
    return with(AlertDialog.Builder(this)) {
        setTitle("Справка")
        val view = this@createReferenceAlertDialog.layoutInflater.inflate(R.layout.reference,null)
        val textViewReference = view.findViewById<TextView>(R.id.tv_menu_reference)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textViewReference.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }
        setView(view)
        setPositiveButton("Ок") {dialog,_ -> dialog.cancel()}
        create()
    }
}