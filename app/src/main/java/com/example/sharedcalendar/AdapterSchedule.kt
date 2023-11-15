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

class AdapterSchedule : RecyclerView.Adapter<AdapterSchedule.ScheduleView>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var itemClickListener: OnItemClickListener? = null

    inner class ScheduleView(val layout: View): RecyclerView.ViewHolder(layout) {
        val textTitle = layout.findViewById<TextView>(R.id.textTitle)
        val textDuration = layout.findViewById<TextView>(R.id.textDuration)

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
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
        for (schedule in scheduleList) {
            if (schedule.start_date == day || schedule.end_date == day) {

            }
        }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}
