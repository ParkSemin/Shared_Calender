package com.example.sharedcalendar

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private val myRef = database.database.getReference("users")
    private val myScheduleRef = database.database.getReference("schedules").child(MyApplication.email_revised.toString())

    private val scheduleList: MutableList<ScheduleData> = mutableListOf()
    lateinit var dayList: ArrayList<Date>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // 뒤로가기 버튼을 누르면 앱이 종료되기 위해 버튼을 누른 시간을 저장
    private var backPressedTime: Long = 0
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(applicationContext, "한번 더 누르면 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
            } else {
                finishAffinity()
                System.runFinalization()
                exitProcess(0)
            }
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 뒤로가기 콜백 추가
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Toolbar 설정
        val toolbar = binding.toolbar // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_density_medium_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = binding.drawerLayout

        // 네비게이션 드로어 내에있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        // 네비게이션 뷰의 헤더를 가져와서 그 안의 TextView에 접근
        val headerView = navigationView.getHeaderView(0)
        val tvName: TextView = headerView.findViewById(R.id.tv_name)
        val tvEmail: TextView = headerView.findViewById(R.id.tv_email)
        tvName.text = MySharedPreferences.getUserName(this)
        tvEmail.text = MySharedPreferences.getUserId(this)

        // FAB (Floating Action Button)를 찾아서 변수에 저장합니다.
        val fab = binding.fab

        // FAB 버튼에 클릭 리스너를 설정합니다.
        fab.setOnClickListener { view ->
            // 버튼을 클릭하면 showPopupMenu 함수를 호출합니다.
            showPopupMenu(view)
        }

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

    // AddEventActivity에서 일정 추가하고 다시 돌아오면 변경 사항을 반영해야 함
    // 따라서 onCreate()에서 분리하여 작성하였음
    override fun onResume() {
        super.onResume()

        // 초기화
        selectedDate = Calendar.getInstance()

        myScheduleRef.addValueEventListener(object: ValueEventListener {
            // DB 일정 데이터를 성공적으로 가져온 경우
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children) {
                    val scheduleData = snapshot.getValue(ScheduleData::class.java)
                    scheduleData?.let {
                        // 읽어온 데이터를 리스트에 추가
                        scheduleList.add(it)
                    }
                }

                // 리스트에 저장된 일정 데이터 활용
                for (schedule in scheduleList) {
                    Log.d("parksemin", "일정명: ${schedule.title}, 시작일: ${schedule.start_date}, 시작시간: ${schedule.start_time}, 종료일: ${schedule.end_date}, 종료시간: ${schedule.ent_time}")
                }
            }

            // DB 일정 데이터를 가져오지 못한 경우
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "DB 데이터 읽기 실패", Toast.LENGTH_LONG).show()
            }

        })

        // 화면 설정
        setMonthView(true)
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
        // 최상단에 "2023.11" 형태로 텍스트 설정하는 부분
        binding.monthYearTextView.text = monthYearFromDate(selectedDate)

        // 날짜 생성해서 리스트에 담기(달이 변경되었을 때만)
        if (reset) {
            dayList = dayInMonthArray()
        }

        // 어댑터 초기화
        val adapter = CalendarAdapter(dayList)

        // 레이아웃 설정(7개의 열)
        val manager: RecyclerView.LayoutManager = GridLayoutManager(this, 7)

        // 레이아웃 적용
        binding.recyclerView.layoutManager = manager

        // 어댑터 적용
        binding.recyclerView.adapter = adapter

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

        // 선택되는 날짜에 대한 이벤트리스너
        adapter.itemClickListener = object : CalendarAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val item = dayList[position]
                val forGetMonthOfItem: Calendar = Calendar.getInstance()
                forGetMonthOfItem.time = item

                // 선택한 날짜가 이번 달이면 달력 변경 안함(False). 다른 달이면 변경함(True).
                if (selectedDate[Calendar.MONTH] == forGetMonthOfItem[Calendar.MONTH]) {
                    selectedDate.time = item
                    setMonthView(false)
                } else {
                    selectedDate.time = item
                    setMonthView(true)
                }
            }
        }
    }

    // 날짜 타입 설정
    private fun monthYearFromDate(calendar: Calendar) : String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        val formattedMonth = if (month < 10) "0$month" else "$month"

        return "$year.$formattedMonth"
    }

    // 날짜 생성
    private fun dayInMonthArray() : ArrayList<Date> {
        val dayList = ArrayList<Date>()
        val monthCalendar = selectedDate.clone() as Calendar

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
                R.id.add_event -> {
                    val intent = Intent(this, AddEventActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate) // selectedDate를 "selectedDate"라는 키로 넘겨줌
                    startActivity(intent)
                    true
                }
                R.id.add_timetable -> {
                    false
                }
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
            R.id.menu_logout-> {
                MySharedPreferences.clearUser(this)
                MyApplication.auth.signOut()

                val intent: Intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                return true
            }
            R.id.menu_delete-> {
                MySharedPreferences.clearUser(this)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("경고")
                    .setMessage("정말 탈퇴하시겠습니까?")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { _, _ ->
                            myRef.child(MyApplication.email_revised.toString()).removeValue()
                            MyApplication.auth.currentUser?.delete()
                            MotionToast.darkColorToast(
                                this,
                                "탈퇴 완료",
                                "회원 탈퇴가 성공적으로 처리되었습니다",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                            )
                            val intent: Intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        })
                    .setNegativeButton("취소", null)
                builder.show()

                return true
            }
        }
        return false
    }

}
