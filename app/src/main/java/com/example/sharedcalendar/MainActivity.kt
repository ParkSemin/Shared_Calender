package com.example.sharedcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate // import selectedDate
import com.example.sharedcalendar.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Date
import com.github.usingsky.calendar.KoreanLunarCalendar
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var dayList: ArrayList<Date>
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Toolbar 설정
        val toolbar = binding.toolbar // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_density_medium_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.drawer_layout)

        // FAB (Floating Action Button)를 찾아서 변수에 저장합니다.
        val fab = binding.fab

        // FAB 버튼에 클릭 리스너를 설정합니다.
        fab.setOnClickListener { view ->
            // 버튼을 클릭하면 showPopupMenu 함수를 호출합니다.
            showPopupMenu(view)
        }

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
    // 팝업 메뉴를 표시하는 함수
    private fun showPopupMenu(view: View) {
        // PopupMenu 객체를 생성합니다.
        val popup = PopupMenu(this, view)
        // 팝업 메뉴의 아이템을 정의한 XML 파일을 로드합니다.
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search_schedule -> {
                    Toast.makeText(this, "Item clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        // 팝업 메뉴를 표시합니다.
        popup.show()
    }
    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item.itemId){
            android.R.id.home->{
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)

    }
    // 드로어 내 아이템 클릭 이벤트 처리하는 함수
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_item1-> Toast.makeText(this,"menu_item1 실행",Toast.LENGTH_SHORT).show()
            R.id.menu_item2-> Toast.makeText(this,"menu_item2 실행",Toast.LENGTH_SHORT).show()
            R.id.menu_item3-> Toast.makeText(this,"menu_item3 실행",Toast.LENGTH_SHORT).show()
        }
        return false
    }

}
