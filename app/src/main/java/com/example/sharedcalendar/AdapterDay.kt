package com.example.sharedcalendar

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.scheduleList
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import com.example.sharedcalendar.CalendarUtil.Companion.today
import com.example.sharedcalendar.databinding.ActivityMainBinding
import com.github.usingsky.calendar.KoreanLunarCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdapterDay(private val binding: ActivityMainBinding, private val tempMonth: Int, private val dayList: MutableList<Date>): RecyclerView.Adapter<AdapterDay.DayView>() {
    // 이전에 선택된 날짜의 뷰를 저장하기 위한 임시 홀더
    private var oldHolder: DayView? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var itemClickListener: OnItemClickListener? = null

    inner class DayView(val layout: View): RecyclerView.ViewHolder(layout) {
        val item_day_layout = layout.findViewById<LinearLayout>(R.id.item_day_layout)
        val item_day_text = layout.findViewById<TextView>(R.id.item_day_text)
        val item_schedule_dot = layout.findViewById<RecyclerView>(R.id.item_schedule_dot)

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_day, parent, false)
        return DayView(view)
    }

    override fun onBindViewHolder(holder: DayView, position: Int) {
        holder.item_day_text.text = SimpleDateFormat("d", Locale.getDefault()).format(dayList[position])

        holder.item_day_text.setTextColor(when(position % 7) {
            0 -> Color.RED // 7로 나누어 떨어지면 일요일이므로 빨간색
            6 -> Color.BLUE // 7로 나눈 나머지가 1이면 토요일이므로 파랑색
            else -> Color.BLACK // 그 외는 평일이므로 검은색
        })

        // 선택된 날짜 배경 색상 설정
        if(dayList[position] == selectedDate.time) {
            //선택한 날짜가 오늘일 경우에는 배경 색상 변경 안함
            if(dayList[position] != today.time){
                //선택한 날짜의 배경 색상 변경
                holder.item_day_text.setBackgroundResource(R.drawable.round_calendar_cell_selected)
                oldHolder = holder
            }
            changeSelectedDateView()
        }

        // 오늘 날짜 배경 색상 설정
        if(dayList[position] == today.time) {
            holder.item_day_text.setBackgroundResource(R.drawable.round_calendar_cell)
        }

        if(tempMonth != SimpleDateFormat("MM", Locale.getDefault()).format(dayList[position]).toInt()-1) {
            holder.item_day_text.alpha = 0.4f
        }

        // 선택된 날짜에 등록된 일정이 있을 경우 점을 찍기 위한 코드 부분
        val tempSchedule: MutableList<ScheduleData> = mutableListOf() // 임시 배열
        val day = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(dayList[position])
        for (schedule in scheduleList) {
            // 현재 날짜가 일정의 시작일과 종료일 사이에 있다면 해당 날짜의 일정을 임시 배열에 추가
            if (schedule.start_date <= day && day <= schedule.end_date) {
                tempSchedule.add(schedule)
            }
        }
        if (tempSchedule.isNotEmpty()) {
            val size = if (tempSchedule.size <= 5) tempSchedule.size else 5
            val dotListManager = GridLayoutManager(holder.layout.context, size)
            val dotListAdapter = AdapterDot(tempSchedule, tempMonth)
            holder.item_schedule_dot.apply {
                layoutManager = dotListManager
                adapter = dotListAdapter
            }
        }

        // 날짜 선택 시 호출되는 리스너
        holder.item_day_layout.setOnClickListener {
            // 이전에 선택한 날짜의 배경 색상을 원래대로 돌리기
            oldHolder?.item_day_text?.background = null

            // 선택한 날짜가 오늘일 경우에는 배경 색상 변경 안함
            if(dayList[position] != today.time) {
                // 선택한 날짜의 배경 색상 변경
                holder.item_day_text.setBackgroundResource(R.drawable.round_calendar_cell_selected)

                oldHolder = holder
            }

            selectedDate.time = dayList[position]

            changeSelectedDateView()
        }
    }

    override fun getItemCount(): Int {
        return 6 * 7
    }

    private fun changeSelectedDateView() {
        // 하단의 선택된 날짜에 대한 정보를 보여주는 뷰의 기준 날짜 및 내용 변경
        val dayOfWeekOfSelectedDate: String = when (selectedDate[Calendar.DAY_OF_WEEK]) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
        binding.selectedDayTextView.text = String.format("%s. %s", selectedDate[Calendar.DAY_OF_MONTH], dayOfWeekOfSelectedDate)

        // 음력 날짜 출력 위한 코드
        val lunarCalendar = KoreanLunarCalendar.getInstance()
        lunarCalendar.setSolarDate(selectedDate[Calendar.YEAR], selectedDate[Calendar.MONTH]+1, selectedDate[Calendar.DAY_OF_MONTH])
        // 입력 문자열을 날짜로 포맷
        val inputDateString: String = lunarCalendar.lunarIsoFormat // 변환될 문자열 : "2023-10-19"의 형태를 가지고 있음
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 포맷 형태 설정
        val formattedInputDate: Date? = inputFormat.parse(inputDateString)
        // 원하는 출력 형태로 재포맷
        val outputFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
        val formattedOutputDate = outputFormat.format(formattedInputDate!!) // 원하는 형태인 "09.05" 형태로 포맷됨
        // 포맷된 문자열을 음력 텍스트뷰에 설정
        binding.selectedDayTextViewToLunar.text = String.format("음력 %s", formattedOutputDate)


        // 등록된 일정 보여주기
        val tempSchedule: MutableList<ScheduleData> = mutableListOf() // 임시 배열
        val day = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(selectedDate.time)
        for (schedule in scheduleList) {
            // 선택한 날짜가 일정의 시작일과 종료일 사이에 있다면 해당 날짜의 일정을 임시 배열에 추가
            if (schedule.start_date <= day && day <= schedule.end_date) {
                tempSchedule.add(schedule)
            }
        }
        // 선택한 날짜의 일정이 담긴 스케줄을 어댑터의 인수로 전달
        if (tempSchedule.isNotEmpty()) {
            binding.showDiaryView.visibility = View.VISIBLE
            binding.noDiaryTv.visibility = View.GONE
            binding.showDiaryView.adapter = AdapterSchedule(tempSchedule)
        } else {
            // 해당 날짜에 등록된 일정이 없을 경우 '등록된 일정이 없습니다.' 텍스트 출력
            binding.showDiaryView.visibility = View.GONE
            binding.noDiaryTv.visibility = View.VISIBLE
        }
    }
}