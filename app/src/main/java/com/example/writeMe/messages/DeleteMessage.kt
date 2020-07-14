package com.example.writeMe.messages

import com.example.writeMe.registration.User
import com.google.firebase.database.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class DeleteMessageFromMe {
    fun deleteMessageFromMe(
        currentUser: User,
        interlocutorUser: User,
        listOfMessages: MutableList<ChatMessage>,
        chatMessageItem: Item<GroupieViewHolder>,
        isInterlocutorClass: Boolean
    ) {
        val referenceCurrentUserMessage = FirebaseDatabase.getInstance()
            .getReference("/users_messages/${currentUser.id}/${interlocutorUser.id}")
        val referenceLatestMessagesCurrentUser = FirebaseDatabase.getInstance()
            .getReference("/latest_messages/${currentUser.id}/${interlocutorUser.id}")

        val messageId = if (isInterlocutorClass) {
            (chatMessageItem as ChatLogActivity.InterlocutorChatItem).id
        } else (chatMessageItem as ChatLogActivity.MyChatItem).id
        referenceLatestMessagesCurrentUser.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.getValue(ChatMessage::class.java)
                val latestMessageId = message!!.id
                if (messageId == latestMessageId) {
                    removeMessage(referenceCurrentUserMessage, messageId, isInterlocutorClass)
                    val penultimateMessage = listOfMessages[listOfMessages.size - 2]
                    referenceLatestMessagesCurrentUser.setValue(penultimateMessage)
                } else {
                    removeMessage(referenceCurrentUserMessage, messageId, isInterlocutorClass)
                }
            }
        })
    }

    private fun removeMessage(
        referenceCurrentUserMessage: DatabaseReference,
        messageId: String,
        isInterlocutorClass: Boolean
    ) {
        if (!isInterlocutorClass) {
            referenceCurrentUserMessage.child(messageId).removeValue()
        } else {
            referenceCurrentUserMessage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEachIndexed { index, element ->
                        val chatMessage = element.getValue(ChatMessage::class.java)
                        if (chatMessage!!.id == messageId) {
                            referenceCurrentUserMessage.child(element.key!!).removeValue()
                            return@forEachIndexed
                        }
                    }
                }
            })
        }
    }
}

class DeleteMessageFromBoth {
    fun deleteFromBoth(
        currentUser: User,
        interlocutorUser: User,
        listOfMessages: MutableList<ChatMessage>,
        chatMessageItem: Item<GroupieViewHolder>
    ) {
        val referenceCurrentUserMessage = FirebaseDatabase.getInstance()
            .getReference("/users_messages/${currentUser.id}/${interlocutorUser.id}")
        val referenceInterlocutorUserMessage = FirebaseDatabase.getInstance()
            .getReference("/users_messages/${interlocutorUser.id}/${currentUser.id}")

        val referenceLatestMessagesCurrentUser = FirebaseDatabase.getInstance()
            .getReference("/latest_messages/${currentUser.id}/${interlocutorUser.id}")
        val referenceLatestMessagesInterlocutorUser = FirebaseDatabase.getInstance()
            .getReference("/latest_messages/${interlocutorUser.id}/${currentUser.id}")

        val messageId = (chatMessageItem as ChatLogActivity.MyChatItem).id
        referenceLatestMessagesCurrentUser.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.getValue(ChatMessage::class.java)
                val latestMessageId = message!!.id
                if (messageId == latestMessageId) {
                    removeMessage(referenceCurrentUserMessage, referenceInterlocutorUserMessage, messageId)
                    val penultimateMessage = listOfMessages[listOfMessages.size - 2]
                    referenceLatestMessagesCurrentUser.setValue(penultimateMessage)
                    val listOfInterlocutorMessages = mutableListOf<ChatMessage>()
                    referenceInterlocutorUserMessage.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach {
                                val _message = it.getValue(ChatMessage::class.java)
                                listOfInterlocutorMessages.add(_message!!)
                            }
                            if (listOfInterlocutorMessages[listOfInterlocutorMessages.size - 1].id ==  messageId) {
                                referenceLatestMessagesInterlocutorUser.setValue(penultimateMessage)
                            }
                        }
                    })
                } else {
                    removeMessage(referenceCurrentUserMessage, referenceInterlocutorUserMessage,messageId)
                }
            }
        })
    }

    private fun removeMessage(
        referenceCurrentUserMessage: DatabaseReference,
        referenceInterlocutorUserMessage: DatabaseReference,
        messageId: String
    ) {
        referenceCurrentUserMessage.child(messageId).removeValue()
        referenceInterlocutorUserMessage.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEachIndexed { index, element ->
                        val chatMessage = element.getValue(ChatMessage::class.java)
                        if (chatMessage!!.id == messageId) {
                            referenceInterlocutorUserMessage.child(element.key!!).removeValue()
                            return@forEachIndexed
                        }
                    }
                }
            })
        }
}