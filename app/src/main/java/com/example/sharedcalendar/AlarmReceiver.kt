package com.example.sharedcalendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "알람"
        val notificationTime = intent.getIntExtra("EXTRA_NOTIFICATION_TIME", 0)
        val message = when {
            notificationTime % 60 == 0 -> {
                val hours = notificationTime / 60
                "일정:$title 의 시작 ${hours}시간 ${notificationTime % 60}분 후"
            }
            notificationTime == 0 -> "일정:$title 의 시작"
            notificationTime == 100 -> "일정:$title 의 10초 후"
            else -> "일정:$title 의 시작 ${notificationTime}분 전"
        }

        // 알림 생성 및 표시
        createNotificationChannel(context)
        showNotification(context, title, message)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AlarmChannel"
            val descriptionText = "Alarm Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ALARM_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림이 탭될 때 앱을 열기 위한 PendingIntent 생성
        val intent = Intent(context, MainActivity::class.java) // 여기서 YourMainActivity를 귀하의 앱의 메인 액티비티로 바꿔주세요.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 진동 패턴 생성
        val vibrationPattern = longArrayOf(1000) //1초 진동

        val notificationBuilder = NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_alarm) // 알림 아이콘 설정
            .setContentTitle(title) // 알림 제목
            .setContentText(message) // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(vibrationPattern) // 진동 추가
            .setContentIntent(pendingIntent) // 앱 열기 위한 PendingIntent 설정

        notificationManager.notify(1, notificationBuilder.build())
    }
}
