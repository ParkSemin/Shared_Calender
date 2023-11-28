package com.example.sharedcalendar

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterDot(private val tempSchedule: MutableList<ScheduleData>, private val tempMonth: Int) : RecyclerView.Adapter<AdapterDot.DotView>() {
    inner class DotView(private val layout: View): RecyclerView.ViewHolder(layout) {
        val item_dot = layout.findViewById<TextView>(R.id.item_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DotView {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_dot, parent, false)
        return DotView(view)
    }

    override fun onBindViewHolder(holder: DotView, position: Int) {
        // 일정이 있을 경우에만 점 찍음
        if (tempSchedule.isNotEmpty()) {
            holder.item_dot.backgroundTintList = ColorStateList.valueOf(tempSchedule[position].color)
            // 해당 일정의 이전달 또는 다음달에서는 점의 색상이 희미하도록 설정함
            if(tempMonth+1 != tempSchedule[position].start_date.split("-")[1].toInt() && tempMonth+1 != tempSchedule[position].end_date.split("-")[1].toInt()) {
                holder.item_dot.alpha = 0.4f
            }
        }
    }

    override fun getItemCount(): Int {
        // 현재 날짜의 일정 개수 반환
        return tempSchedule.size
    }
}