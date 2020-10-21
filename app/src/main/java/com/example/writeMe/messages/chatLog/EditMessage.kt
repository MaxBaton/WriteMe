package com.example.writeMe.messages.chatLog

import com.example.writeMe.registration.User
import com.google.firebase.database.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class EditMessage {
    fun editMessage(
        message: String,
        currentUser: User,
        interlocutorUser: User,
        chatMessageItem: Item<GroupieViewHolder>
    ) {
        val messageId = (chatMessageItem as ChatLogActivity.MyChatItem).id
        val referenceCurrentUserMessage = FirebaseDatabase.getInstance()
            .getReference("/users_messages/${currentUser.id}/${interlocutorUser.id}")
        val referenceInterlocutorUserMessage = FirebaseDatabase.getInstance()
            .getReference("/users_messages/${interlocutorUser.id}/${currentUser.id}")

        val referenceLatestMessagesCurrentUser = FirebaseDatabase.getInstance()
            .getReference("/latest_messages/${currentUser.id}/${interlocutorUser.id}")
        val referenceLatestMessagesInterlocutorUser = FirebaseDatabase.getInstance()
            .getReference("/latest_messages/${interlocutorUser.id}/${currentUser.id}")
        //referenceCurrentUserMessage.child(messageId).child("text").setValue(message)

        referenceLatestMessagesCurrentUser.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val _message = snapshot.getValue(ChatMessage::class.java)
                val latestMessageId = _message!!.id
                if (messageId == latestMessageId) {
                    editMessageFinal(message = message,referenceCurrentUserMessage = referenceCurrentUserMessage,
                        referenceInterlocutorUserMessage = referenceInterlocutorUserMessage,messageId = messageId)
                    referenceLatestMessagesCurrentUser.child("text").setValue(message)
                    referenceLatestMessagesInterlocutorUser.child("text").setValue(message)
                } else {
                    editMessageFinal(message = message,referenceCurrentUserMessage = referenceCurrentUserMessage,
                        referenceInterlocutorUserMessage = referenceInterlocutorUserMessage,messageId = messageId)
                }
            }
        })
    }

    private fun editMessageFinal(
        message: String,
        referenceCurrentUserMessage: DatabaseReference,
        referenceInterlocutorUserMessage: DatabaseReference,
        messageId: String
    ) {
        referenceCurrentUserMessage.child(messageId).child("text").setValue(message)
        referenceInterlocutorUserMessage.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val _message = it.getValue(ChatMessage::class.java)
                    if (messageId == _message!!.id){
                        val key = it.key!!
                        referenceInterlocutorUserMessage.child(key).child("text").setValue(message)
                        return
                    }
                }
            }
        })
    }
}