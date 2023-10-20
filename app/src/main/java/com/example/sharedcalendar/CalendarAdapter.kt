package com.example.sharedcalendar

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import com.example.sharedcalendar.CalendarUtil.Companion.today
import java.util.Calendar
import java.util.Date

class CalendarAdapter(private val dayList: ArrayList<Date>) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    // 화면 설정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dayList.size
    }

    // 데이터 설정
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 날짜 변수에 담기
        val monthDate = dayList[position]

        // 초기화
        var dateCalendar = Calendar.getInstance()

        // 날짜 캘린더에 담기
        dateCalendar.time = monthDate

        // 전달받은 날짜
        var receivedYear = dateCalendar.get(Calendar.YEAR)
        var receivedMonth = dateCalendar.get(Calendar.MONTH) + 1
        var receivedDay = dateCalendar.get(Calendar.DAY_OF_MONTH)

        // 매달 1일 default 값 설정
        holder.dayText.text = receivedDay.toString()
        if (receivedDay == selectedDate[Calendar.DAY_OF_MONTH] && receivedMonth == selectedDate[Calendar.MONTH] + 1) {
            holder.dayText.background = ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.round_calendar_cell_selected
            )
        }

        // 현재 시스템 날짜
        var selectYear = selectedDate.get(Calendar.YEAR)
        var selectMonth = selectedDate.get(Calendar.MONTH) + 1

        //현재 날짜 비교해서 같다면 배경색상 변경
        if (receivedYear == today.get(Calendar.YEAR) && receivedMonth == today.get(Calendar.MONTH)+1 && receivedDay == today.get(Calendar.DAY_OF_MONTH)) {
            holder.dayText.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.round_calendar_cell)
        }

        // 전달받은 날짜와 현재 시스템 날짜 비교하여 색상 변경
        if (receivedYear == selectYear && receivedMonth == selectMonth) {
            holder.dayText.setTextColor(Color.parseColor("#000000"))

            //텍스트 색상 지정(토,일)
            if ((position + 1) % 7 == 0) { //토요일은 파랑
                holder.dayText.setTextColor(Color.BLUE)

            } else if (position == 0 || position % 7 == 0) { //일요일은 빨강
                holder.dayText.setTextColor(Color.RED)
            }
        } else { //다르다면 연한 색상
            holder.dayText.setTextColor(Color.parseColor("#B4B4B4"))

            //텍스트 색상 지정(토,일)
            if ((position + 1) % 7 == 0) { //토요일은 파랑
                holder.dayText.setTextColor(Color.parseColor("#B4FFFF"))

            } else if (position == 0 || position % 7 == 0) { //일요일은 빨강
                holder.dayText.setTextColor(Color.parseColor("#FFB4B4"))
            }
        }
    }
}