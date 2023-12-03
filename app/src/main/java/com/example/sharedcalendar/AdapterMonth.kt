package com.example.sharedcalendar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import com.example.sharedcalendar.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Date

class AdapterMonth(val binding: ActivityMainBinding): RecyclerView.Adapter<AdapterMonth.MonthView>() {
    val center = Int.MAX_VALUE / 2
    private var calendar = Calendar.getInstance()

    inner class MonthView(val layout: View): RecyclerView.ViewHolder(layout) {
        val item_month_text = layout.findViewById<TextView>(R.id.item_month_text)
        val item_month_day_list = layout.findViewById<RecyclerView>(R.id.item_month_day_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_month, parent, false)
        return MonthView(view)
    }

    override fun onBindViewHolder(holder: MonthView, position: Int) {
        calendar.time = Date() // calendar의 time을 현재 날짜로 초기화

        calendar.clear(Calendar.HOUR_OF_DAY)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)

        calendar.set(Calendar.DAY_OF_MONTH, 1) // 현재 월의 1일로 이동
        calendar.add(Calendar.MONTH, position - center) // 좌로 이동시 position - center = -1, 우로 이동시 position - center = 1

        holder.item_month_text.text = String.format("${calendar.get(Calendar.YEAR)}년 ${calendar.get(Calendar.MONTH) + 1}월") // 현재 년월 텍스트 설정
        val tempMonth = calendar.get(Calendar.MONTH) // 현재 월을 저장

        var dayList: MutableList<Date> = MutableList(6 * 7) { Date() }
        for(i in 0..5) { // 6주
            for(k in 0..6) { // 7일
                calendar.add(Calendar.DAY_OF_MONTH, (1-calendar.get(Calendar.DAY_OF_WEEK)) + k)
                dayList[i * 7 + k] = calendar.time
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        }

        val dayListManager = GridLayoutManager(holder.layout.context, 7)
        val dayListAdapter = AdapterDay(binding, tempMonth, dayList)

        holder.item_month_day_list.apply {
            layoutManager = dayListManager
            adapter = dayListAdapter
        }
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}