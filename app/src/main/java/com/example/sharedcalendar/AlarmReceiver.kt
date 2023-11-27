package com.example.sharedcalendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") // 인텐트에서 알람 ID 가져오기
        val sharedPrefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

        val title = sharedPrefs.getString("EXTRA_TITLE_$alarmId", "기본 제목")
        val notificationTime = sharedPrefs.getInt("EXTRA_NOTIFICATION_TIME_$alarmId", 0)
        Log.d("MyApp", "Received in onReceive, notificationTime: $notificationTime")
        val message = when {
            notificationTime == 60 -> "일정: ${title}의 시작 1시간 전"
            notificationTime == 2 -> "일정: ${title}의 시작"
            else -> "일정: ${title}의 시작 ${notificationTime}분 전"
        }

        // 알림 생성 및 표시
        createNotificationChannel(context)
        if (title != null) {
            showNotification(context, title, message)
        }
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
            .setSmallIcon(R.drawable.ic_launcher) // 알림 아이콘 설정
            .setContentTitle(title) // 알림 제목
            .setContentText(message) // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(vibrationPattern) // 진동 추가
            .setContentIntent(pendingIntent) // 앱 열기 위한 PendingIntent 설정

        notificationManager.notify(1, notificationBuilder.build())
    }
}
