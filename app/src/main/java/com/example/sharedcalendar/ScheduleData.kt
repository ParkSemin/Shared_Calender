package com.example.sharedcalendar

import java.io.Serializable

data class ScheduleData(
    val key: String = "",
    val firstTimeRegistrant: String = "",
    val firstTimeRegistrantAccount: String = "",
    val finalReviser: String = "",
    val finalReviserAccount: String = "",
    val title: String = "",
    val start_date: String = "",
    val start_time: String = "",
    val end_date: String = "",
    val end_time: String = "",
    val color: Int = 0,
    val notificationTime: Int = 0
) : Serializable {
    // 데이터 변경 확인 메서드
    fun isDataChanged(other: ScheduleData): Boolean {
        return this.start_date != other.start_date ||
                this.start_time != other.start_time ||
                this.end_date != other.end_date ||
                this.end_time != other.end_time ||
                this.notificationTime != other.notificationTime
    }
}