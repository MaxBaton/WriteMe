package com.example.writeMe.messages.latestMessages

import android.app.AlertDialog
import com.example.writeMe.registration.User
import com.google.firebase.database.FirebaseDatabase

object CreateAlertDialogConfirmDelete {
    fun create(latestMessagesActivity: LatestMessagesActivity,
               currentUser: User,
               interlocutorUserInGroupAdapter: LatestMessagesActivity.LatestMessageItem): AlertDialog? {
        return with(AlertDialog.Builder(latestMessagesActivity)) {
            setTitle("Подтверждение удаления")
            setPositiveButton("Подтвердить") { currentDialog, _ ->
                deleteCorrespondence(currentUser,interlocutorUserInGroupAdapter)
                currentDialog.cancel()
            }
            setNegativeButton("Отмена") {currentDialog, _ ->
                currentDialog.cancel()
            }
            create()
        }
    }

    private fun deleteCorrespondence(currentUser: User,interlocutorUserInGroupAdapter: LatestMessagesActivity.LatestMessageItem) {
        val referenceLatestMessages = FirebaseDatabase.getInstance().getReference("/latest_messages/${currentUser.id}")
        val referenceUsersMessages = FirebaseDatabase.getInstance().getReference("/users_messages/${currentUser.id}")

        referenceLatestMessages.child(interlocutorUserInGroupAdapter.interlocutorUser!!.id).removeValue()
        referenceUsersMessages.child(interlocutorUserInGroupAdapter.interlocutorUser!!.id).removeValue()
    }
}