package com.example.sharedcalendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var notificationListView: ListView
    private lateinit var selectedNotificationText: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var notificationTextOptions: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        // Initialize UI components
        notificationListView = findViewById(R.id.notificationListView)
        selectedNotificationText = findViewById(R.id.selectedNotificationText)

        // Define notification text options and corresponding variables
        val notificationOptionsWithVariables = mapOf(
            "알림 없음" to 0,
            "당일 알림" to 1,
            "5분 전" to 5,
            "10분 전" to 10,
            "15분 전" to 15,
            "1시간 전" to 60,
            "test 10초 뒤" to 100
        )

        // Initialize adapter
        notificationTextOptions = ArrayList(notificationOptionsWithVariables.keys)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notificationTextOptions)
        notificationListView.adapter = adapter

        // Set item click listener to display selected text and start 'addevent' activity
        notificationListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                // 선택된 알림 텍스트 가져오기
                val selectedText = notificationTextOptions[position]

                // 선택된 텍스트에 해당하는 변수 (분) 가져오기
                val minutesBefore = notificationOptionsWithVariables[selectedText]

                // 선택된 시간을 결과로 반환하고 액티비티 종료
                returnResult(minutesBefore ?: 0)
            }
    }
    private fun returnResult(minutesBefore: Int) {
        val intent = Intent()
        intent.putExtra("minutesBefore", minutesBefore)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
