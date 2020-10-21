package com.example.writeMe.messages.chatLog

import android.util.Log
import com.example.writeMe.notification.DataMessage
import com.example.writeMe.notification.MyResponse
import com.example.writeMe.notification.Sender
import com.example.writeMe.notification.Token
import com.example.writeMe.registration.User
import com.example.writeMe.toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SendNotification {
    fun send(
        chatLogActivity: ChatLogActivity,
        apiService: APIService,
        currentUser: User,
        interlocutorUser: User,
        receiver: String,
        userName: String,
        message: String
    ) {
        val tokens = FirebaseDatabase.getInstance().getReference("/tokens")
        val query = tokens.orderByKey().equalTo(receiver)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapShot in dataSnapshot.children) {
                    //convert users to json
                    val moshi = Moshi.Builder().build()
                    val jsonAdapter = moshi.adapter(User::class.java)
                    val jsonCurrentUser = jsonAdapter.toJson(currentUser)
                    val jsonInterlocutorUser = jsonAdapter.toJson(interlocutorUser)

                    val token = snapShot.getValue(Token::class.java)
                    val dataMessage = DataMessage(userName,message,jsonCurrentUser,jsonInterlocutorUser)
                    val sender = Sender(dataMessage,token!!.token)

                    apiService.sendNotification(sender).enqueue(object: Callback<MyResponse?> {
                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                            TODO()
                        }

                        override fun onResponse(
                            call: Call<MyResponse?>,
                            response: Response<MyResponse?>
                        ) {
                            if (response.code() == 200) {
                                if (response.body()!!.success != 1) {
                                    chatLogActivity.toast("failed with notification")
                                }
                            }
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("ChatLogActivity","users canceled")
            }
        })
    }
}