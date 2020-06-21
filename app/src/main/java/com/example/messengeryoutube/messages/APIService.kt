package com.example.messengeryoutube.messages

import com.example.messengeryoutube.notification.MyResponse
import com.example.messengeryoutube.notification.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface APIService {
    @Headers(
            "Content-Type:application/json",
            "Authorization:key=AAAATYoGFgk:APA91bFgqJNer0rWzFzZfI-oQC-prA60BELbYgJtPRaMoFAovVf5ucptM9WNUR4h4sxBjMT1GP7jSZRJN_HjQS8qw3AXQ8K9UV62lEYFFNpsoSfwi6CrGMoVhcgdy1Cg2w9ylMoDwjKX"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>
}