package com.example.sharedcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate // import selectedDate
import com.example.sharedcalendar.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Date
import com.github.usingsky.calendar.KoreanLunarCalendar
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var dayList: ArrayList<Date>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 초기화
        selectedDate = Calendar.getInstance()

        // 화면 설정
        setMonthView(true)

        // 이전달 버튼 이벤트리스너
        binding.preBtn.setOnClickListener {
            selectedDate.add(Calendar.MONTH, -1) // (현재 달) - 1
            checkDayOfMonth()
            setMonthView(true)
        }

        // 다음달 버튼 이벤트리스너
        binding.nextBtn.setOnClickListener {
            selectedDate.add(Calendar.MONTH, 1) // (현재 달) + 1
            checkDayOfMonth()
            setMonthView(true)
        }
    }

    // 월이 변경될 때 오늘 날짜라면 해당 날짜로 설정하고 아니라면 1일로 설정하는 코드
    private fun checkDayOfMonth() {
        if (selectedDate[Calendar.MONTH] == CalendarUtil.today[Calendar.MONTH]) {
            selectedDate[Calendar.DAY_OF_MONTH] = CalendarUtil.today[Calendar.DAY_OF_MONTH]
        } else {
            selectedDate[Calendar.DAY_OF_MONTH] = 1
        }
    }

    // 날짜 화면에 보여주기
    private fun setMonthView(reset: Boolean) {
        binding.monthYearTextView.text = monthYearFromDate(selectedDate)

        // 날짜 생성해서 리스트에 담기(달이 변경되었을 때만)
        if (reset) {
            dayList = dayInMonthArray()
        }

        // 어댑터 초기화
        val adapter = CalendarAdapter(dayList)

        // 레이아웃 설정(7개의 열)
        var manager: RecyclerView.LayoutManager = GridLayoutManager(this, 7)

        // 레이아웃 적용
        binding.recyclerView.layoutManager = manager

        // 어댑터 적용
        binding.recyclerView.adapter = adapter

        // 하단의 선택된 날짜에 대한 정보를 보여주는 뷰의 기준 날짜 및 내용 변경
        val dayOfWeekOfSelectedDate: String = when (selectedDate[Calendar.DAY_OF_WEEK]) {
            1 -> "일"
            2 -> "월"
            3 -> "화"
            4 -> "수"
            5 -> "목"
            6 -> "금"
            else -> "토"
        }
        binding.selectedDayTextView.text = "${selectedDate[Calendar.DAY_OF_MONTH]}. ${dayOfWeekOfSelectedDate}"

        // 음력 날짜 출력 위한 코드
        val lunarCalendar = KoreanLunarCalendar.getInstance()
        lunarCalendar.setSolarDate(selectedDate[Calendar.YEAR], selectedDate[Calendar.MONTH]+1, selectedDate[Calendar.DAY_OF_MONTH])
        // 입력 문자열을 날짜로 포맷
        val inputDateString: String = lunarCalendar.lunarIsoFormat // 변환될 문자열 : "2023-10-19"의 형태를 가지고 있음
        val inputFormat = SimpleDateFormat("yyyy-MM-dd") // 포맷 형태 설정
        val formattedInputDate: Date = inputFormat.parse(inputDateString)

        // 원하는 출력 형태로 재포맷
        val outputFormat = SimpleDateFormat("MM.dd")
        val formattedOutputDate = outputFormat.format(formattedInputDate) // 원하는 형태인 "09.05" 형태로 포맷됨

        // 포맷된 문자열을 음력 텍스트뷰에 설정
        binding.selectedDayTextViewToLunar.text = "음력 $formattedOutputDate"

        // 선택되는 날짜에 대한 이벤트리스너
        adapter.itemClickListener = object : CalendarAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val item = dayList[position]
                val forGetMonthOfItem: Calendar = Calendar.getInstance()
                forGetMonthOfItem.time = item
                if (selectedDate[Calendar.MONTH] == forGetMonthOfItem[Calendar.MONTH]) {
                    selectedDate.time = item
                    setMonthView(false) // 선택한 날짜가 이번달일 경우 달을 변경하지 않음
                } else {
                    selectedDate.time = item
                    setMonthView(true) // 선택한 날짜가 이번달이 아닐 경우 달을 변경함
                }
            }
        }
    }

    // 날짜 타입 설정
    private fun monthYearFromDate(calendar: Calendar) : String {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1

        val formattedMonth = if (month < 10) "0$month" else "$month"

        return "$year.$formattedMonth"
    }

    // 날짜 생성
    private fun dayInMonthArray() : ArrayList<Date> {
        var dayList = ArrayList<Date>()
        var monthCalendar = selectedDate.clone() as Calendar

        // 1일로 설정
        monthCalendar[Calendar.DAY_OF_MONTH] = 1

        //해당 달의 1일의 요일[1:일요일, 2: 월요일.... 7일: 토요일]
        val firstDayOfMonth = monthCalendar[Calendar.DAY_OF_WEEK]-1

        //요일 숫자만큼 이전 날짜로 설정
        //예: 6월1일이 수요일이면 3만큼 이전날짜 셋팅
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        while(dayList.size < 42){ // 1주(7일) * 6 = 42

            dayList.add(monthCalendar.time)

            //1일씩 늘린다. 1일 -> 2일 -> 3일
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dayList
    }
}