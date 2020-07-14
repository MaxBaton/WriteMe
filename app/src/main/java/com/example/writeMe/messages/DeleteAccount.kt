package com.example.writeMe.messages

import com.example.writeMe.registration.MainActivity
import com.example.writeMe.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class DeleteAccount {
    fun delete(currentUser: User) {
        val referenceLatestMessages = FirebaseDatabase.getInstance().getReference("/latest_messages")
        val referenceUsersMessages = FirebaseDatabase.getInstance().getReference("/users_messages")
        val referenceTokens = FirebaseDatabase.getInstance().getReference("/tokens")
        val referenceUserInChat = FirebaseDatabase.getInstance().getReference("/user_in_chat")
        val referenceUsers = FirebaseDatabase.getInstance().getReference("/users")

        val listOfInterlocutorsId = mutableListOf<String>()
        referenceLatestMessages.child(currentUser.id).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val key = it.key!!
                    listOfInterlocutorsId.add(key)
                }
                listOfInterlocutorsId.forEach {
                    referenceLatestMessages.child(it).child(currentUser.id).removeValue()
                    referenceUsersMessages.child(it).child(currentUser.id).removeValue()
                    referenceUserInChat.child(it).child(currentUser.id).removeValue()
              }
                referenceLatestMessages.child(currentUser.id).removeValue()
                referenceUsersMessages.child(currentUser.id).removeValue()
                referenceTokens.child(currentUser.id).removeValue()
                referenceUserInChat.child(currentUser.id).removeValue()
                referenceUsers.child(currentUser.id).removeValue()
                if (currentUser.imageUrl != MainActivity.ANONYMOUS_AVATAR_URL) {
                    FirebaseStorage.getInstance().getReference("/avatars/${FirebaseAuth.getInstance().currentUser!!.email}").delete()
                }
                FirebaseAuth.getInstance().currentUser!!.delete()
            }

        })
    }
}