package com.example.writeMe

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.writeMe.messages.chatLog.ChatMessage
import com.example.writeMe.messages.chatLog.DeleteMessageFromBoth
import com.example.writeMe.messages.chatLog.DeleteMessageFromMe
import com.example.writeMe.messages.chatLog.EditMessage
import com.example.writeMe.messages.editProfile.DeleteAccount
import com.example.writeMe.messages.editProfile.EditProfileActivity
import com.example.writeMe.registration.MainActivity
import com.example.writeMe.registration.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


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

fun AppCompatActivity.createDeleteMessageFromMeAlertDialog(currentUser: User,
                                                           interlocutorUser: User,
                                                           listOfMessages: MutableList<ChatMessage>,
                                                           chatMessageItem: Item<GroupieViewHolder>,
                                                           isInterlocutorClass: Boolean): AlertDialog {
    return with(AlertDialog.Builder(this)) {
        setTitle("Удалить сообщение?")
        setPositiveButton("Да") { _, _ ->
            DeleteMessageFromMe().deleteMessageFromMe(currentUser = currentUser,interlocutorUser = interlocutorUser,
                listOfMessages = listOfMessages,chatMessageItem = chatMessageItem, isInterlocutorClass = isInterlocutorClass)
        }
        setNegativeButton("Отмена") {dialog, _ -> dialog.cancel() }
        create()
    }
}

fun AppCompatActivity.createDeleteMessageFromBothAlertDialog(currentUser: User,
                                                             interlocutorUser: User,
                                                             listOfMessages: MutableList<ChatMessage>,
                                                             chatMessageItem: Item<GroupieViewHolder>): AlertDialog {
    return with(AlertDialog.Builder(this)) {
        setTitle("Удалить сообщение?")
        setPositiveButton("Да") { _, _ ->
            DeleteMessageFromBoth().deleteFromBoth(currentUser = currentUser, interlocutorUser = interlocutorUser,
                listOfMessages = listOfMessages, chatMessageItem = chatMessageItem)
        }
        setNegativeButton("Отмена") {dialog, _ -> dialog.cancel() }
        create()
    }
}

fun AppCompatActivity.createEditMessageAlertDialog(view: View, currentUser: User,
                                                   interlocutorUser: User,
                                                   editTextEditMessage: EditText,
                                                   chatMessageItem: Item<GroupieViewHolder>): AlertDialog {
    return with(AlertDialog.Builder(this)) {
        setTitle("Редактирование сообщения")
        setView(view)
        setCancelable(false)
        setPositiveButton("Изменить") {_,_ ->
            val message = editTextEditMessage.text.toString()
            EditMessage().editMessage(message = message,currentUser = currentUser!!, interlocutorUser = interlocutorUser!!,
                chatMessageItem = chatMessageItem)
        }
        setNegativeButton("Отмена") {_,_ -> }
        create()
    }
}

fun AppCompatActivity.createConfirmDeleteAccountAlertDialog(editProfileActivity: EditProfileActivity, currentUser: User): AlertDialog? {
    return with(AlertDialog.Builder(editProfileActivity)) {
        setTitle("Удалить аккаунт?")
        setPositiveButton("Удалить") { currentDialog, _ ->
            currentDialog.cancel()

            DeleteAccount().delete(currentUser = currentUser)

            val intent = Intent(editProfileActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            editProfileActivity.startActivity(intent)
            editProfileActivity.finish()
        }
        setNegativeButton("Отмена") {currentDialog, _ ->
            currentDialog.cancel()
        }
        create()
    }
}

fun AppCompatActivity.createConfirmEditAccountAlertDialog(
    editProfileActivity: EditProfileActivity,
    currentUserAuth: FirebaseUser,
    dialog: AlertDialog,
    email: String,
    password: String,
    changeEmailOrAndPassword: Int
): AlertDialog? {
    return with(AlertDialog.Builder(editProfileActivity)) {
        setTitle("Подтверждение изменения")
        val view = editProfileActivity.layoutInflater.inflate(R.layout.confirm_edit_email_password_alert_dialog,null)
        setView(view)
        setCancelable(false)
        setPositiveButton("Подтвердить") { currentDialog, _ ->
            currentUserAuth?.let {
                val currentEmail = currentUserAuth!!.email!!
                val editTextPassword = view.findViewById<EditText>(R.id.edit_text_confirm_password)
                val currentPassword = editTextPassword.text.toString().trim()
                val credential = EmailAuthProvider.getCredential(currentEmail,currentPassword)

                it.reauthenticate(credential)
                    .addOnCompleteListener {
                        currentDialog.cancel()
                        when(changeEmailOrAndPassword) {
                            1 -> {
                                currentUserAuth!!.updateEmail(email)
                                    .addOnCompleteListener {
                                        editProfileActivity.toast("complete updating email")
                                        editProfileActivity.runOnUiThread {
                                            dialog?.cancel()
                                            editProfileActivity.finish()
                                        }
                                    }
                            }
                            2 -> {
                                currentUserAuth!!.updatePassword(password)
                                    .addOnCompleteListener {
                                        editProfileActivity.toast("complete updating password")
                                        editProfileActivity.runOnUiThread {
                                            dialog?.cancel()
                                            editProfileActivity.finish()
                                        }
                                    }
                            }
                            3 -> {
                                currentUserAuth!!.updateEmail(email)
                                    .addOnCompleteListener {
                                        editProfileActivity.toast("complete updating email")
                                        currentUserAuth!!.updatePassword(password)
                                            .addOnCompleteListener {
                                                editProfileActivity.toast("complete updating password")
                                                editProfileActivity.runOnUiThread {
                                                    dialog?.cancel()
                                                    editProfileActivity.finish()
                                                }
                                            }
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { editProfileActivity.toast(it.message!!) }
            }
        }
        setNegativeButton("Отмена"){ _, _ ->
            editProfileActivity.runOnUiThread {
                dialog?.cancel()
                editProfileActivity.finish()
            }
        }
        create()
    }
}

fun AppCompatActivity.createUpdateOrDeleteAccountAlertDialog(editProfileActivity: EditProfileActivity, title: String): AlertDialog? {
    return with(AlertDialog.Builder(editProfileActivity)) {
        setTitle(title)
        setView(editProfileActivity.layoutInflater.inflate(R.layout.registration_wait_alert_dialog,null))
        setCancelable(true)
        create()
    }
}

