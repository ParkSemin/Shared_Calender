package com.example.sharedcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val scheduleList: List<ScheduleData>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val startDateTextView: TextView = itemView.findViewById(R.id.startDateTextView)
        val startTimeTextView: TextView = itemView.findViewById(R.id.startTimeTextView)
        val endDateTextView: TextView = itemView.findViewById(R.id.endDateTextView)
        val endTimeTextView: TextView = itemView.findViewById(R.id.endTimeTextView)

        fun bind(schedule: ScheduleData) {
            titleTextView.text = schedule.title
            startDateTextView.text = schedule.start_date
            startTimeTextView.text = schedule.start_time
            endDateTextView.text = schedule.end_date
            endTimeTextView.text = schedule.end_time
            // 다른 필드에 대한 바인딩도 추가 가능
        }
    }
}
