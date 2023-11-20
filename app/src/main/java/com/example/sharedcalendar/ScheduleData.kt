package com.example.sharedcalendar

import java.io.Serializable

data class ScheduleData(
    val key: String = "",
    val name: String = "",
    val title: String = "",
    val start_date: String = "",
    val start_time: String = "",
    val end_date: String = "",
    val end_time: String = "",
    val color: Int = 0
) : Serializable