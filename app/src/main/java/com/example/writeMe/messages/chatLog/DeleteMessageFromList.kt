package com.example.writeMe.messages.chatLog

object DeleteMessageFromList {
    fun delete(listOfMessages: MutableList<ChatMessage>, chatItemId: String) {
        var deleteIndex = 0
        listOfMessages.forEachIndexed { index, chatMessage ->
            if (chatMessage.id == chatItemId) {
                deleteIndex = index
            }
        }
        listOfMessages.removeAt(deleteIndex)
    }
}