package com.example.sharedcalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 인텐트에서 알람 ID 가져오기
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return

        // SharedPreferences에서 일정 명과 notificationTime 가져오기
        val sharedPrefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        val title = sharedPrefs.getString("EXTRA_TITLE_$alarmId", "기본 제목") ?: "기본 제목"
        val notificationTime = sharedPrefs.getInt("EXTRA_NOTIFICATION_TIME_$alarmId", 0)

        // 로그 출력
        Log.d("MyApp", "Received in onReceive, notificationTime: $notificationTime")

        // 알림 메시지 설정
        val message = when (notificationTime) {
            120 -> "일정: ${title}의 시작 2시간 전"
            60 -> "일정: ${title}의 시작 1시간 전"
            2 -> "일정: ${title}의 시작"
            else -> "일정: ${title} 시작 ${notificationTime}분 전"
        }

        // 알림 생성 및 표시
        createNotificationChannel(context)
        showNotification(context, title, message)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AlarmChannel"
            val descriptionText = "Alarm Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH // 여기에 추가
            val channel = NotificationChannel("ALARM_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 기본 알림 소리를 가져옵니다.
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림이 탭될 때 앱을 열기 위한 PendingIntent 생성
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 진동 패턴 생성
        val vibrationPattern = longArrayOf(2000) // 2초 진동

        val notificationBuilder = NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher) // 알림 아이콘 설정
            .setContentTitle(title) // 알림 제목
            .setContentText(message) // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(vibrationPattern) // 진동 추가
            .setSound(defaultSoundUri) // 기본 알림 소리 추가
            .setContentIntent(pendingIntent) // 앱 열기 위한 PendingIntent 설정

        notificationManager.notify(1, notificationBuilder.build())
    }
}
