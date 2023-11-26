package com.example.sharedcalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // FCM 메시지 수신 시 호출됩니다.
        Log.d("FCM", "메시지 수신: ${remoteMessage.notification?.body}")

        // 알림 채널 생성 및 설정 (한 번만 수행)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_channel_id",
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성 및 표시
        val notification = NotificationCompat.Builder(this, "my_channel_id")
            .setContentTitle("제목")
            .setContentText("내용")
            .setSmallIcon(R.drawable.ic_check)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        // 새 FCM 토큰이 생성될 때 호출됩니다.
        Log.d("FCM", "새 토큰: $token")
    }

    override fun onMessageSent(messageId: String) {
        // FCM 메시지가 전송된 후 호출됩니다.
        Log.d("FCM", "메시지 전송됨: $messageId")
    }

    override fun onSendError(messageId: String, exception: Exception) {
        // FCM 메시지 전송 오류 발생 시 호출됩니다.
        Log.e("FCM", "메시지 전송 오류: $messageId, $exception")
    }
}
