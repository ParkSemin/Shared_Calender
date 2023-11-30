package com.example.sharedcalendar


import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
class FcmPush {

    var JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    companion object {
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        // Firebase Realtime Database의 'pushtokens' 경로에서 푸시 토큰을 가져오기
        val databaseReference = FirebaseDatabase.getInstance().getReference("pushtokens").child(destinationUid)
        Log.d("sendBroadcastMessage", "sendMessage: uid: $destinationUid : $title, Message: $message")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val token = dataSnapshot.child("pushToken").value.toString()
                    Log.d("sendBroadcastMessage", "token: uid: $token : $title, Message: $message")

                    val pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification.title = title
                    pushDTO.notification.body = message

                    val body = RequestBody.create(JSON, gson!!.toJson(pushDTO))
                    val url = "https://fcm.googleapis.com/fcm/send"
                    val serverKey = "AAAAGGh8Wa4:APA91bG0z_QBqCyOdi2-hLUq_qyuOOx3-UndWXGpEsxuUphJrP1JLoehrEFYNO53ExeykzP4ufLRKf6BkIn9E97etOa8eDEhsjT6N85EWqeS6QFepGxOlnr3f1y6uL8b4eIIRj3H51S8" // Firebase Console에서 얻은 서버 키

                    val request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=$serverKey")
                        .url(url)
                        .post(body)
                        .build()

                    okHttpClient?.newCall(request)?.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            // 실패 처리
                            Log.d("sendBroadcastMessage", "onFailure: onFailure")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            println(response.body?.string())
                            Log.d("sendBroadcastMessage", "onResponse: $call ")
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("sendBroadcastMessage", "DatabaseError")
                // 취소 처리
            }
        })
    }
}