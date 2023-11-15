package com.example.sharedcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.scheduleList
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterSchedule(val tempSchedule: MutableList<ScheduleData>) : RecyclerView.Adapter<AdapterSchedule.ScheduleView>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleView(val layout: View): RecyclerView.ViewHolder(layout) {
        val item_schedule_layout = layout.findViewById<LinearLayout>(R.id.item_schedule_layout)
        val item_schedule_title = layout.findViewById<TextView>(R.id.item_schedule_title)
        val item_schedule_duration = layout.findViewById<TextView>(R.id.item_schedule_duration)

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleView {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_schedule, parent, false)
        return ScheduleView(view)
    }

    override fun onBindViewHolder(holder: ScheduleView, position: Int) {
        holder.item_schedule_title.text = tempSchedule[position].title
        holder.item_schedule_duration.text = String.format("${tempSchedule[position].start_date} ~ ${tempSchedule[position].end_date}")


        // 해당 일정 선택시 호출되는 리스너
        holder.item_schedule_layout.setOnClickListener {
            // 일정 정보 띄워주는 화면 표시하도록 구현할 것
        }
    }

    override fun getItemCount(): Int {
        // 선택 날짜의 일정 개수 반환
        return tempSchedule.size
    }
}
