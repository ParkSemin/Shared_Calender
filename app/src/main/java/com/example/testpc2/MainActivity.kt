package com.example.testpc2

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.*
import androidx.appcompat.widget.Toolbar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // 변수 선언
    private var selectedYear = 0
    private var selectedMonth = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var titleTextView: TextView
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_density_medium_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.drawer_layout)

        // 네비게이션 드로어 내에있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        val username = intent.getStringExtra("username")

        // FAB (Floating Action Button)을 찾아서 변수에 저장합니다.
        val fab: FloatingActionButton = findViewById(R.id.fab)

        // 현재 연도와 월을 가져옴
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH) + 1 // 0-based index

        // View를 초기화함
        titleTextView = findViewById(R.id.titleTextView)
        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("calendar", Context.MODE_PRIVATE)

        // 이전 버튼과 다음 버튼에 대한 참조를 가져옴
        val prevButton: Button = findViewById(R.id.prevButton)
        val nextButton: Button = findViewById(R.id.nextButton)


        // FAB 버튼에 클릭 리스너를 설정합니다.
        fab.setOnClickListener { view ->
            // 버튼을 클릭하면 showPopupMenu 함수를 호출합니다.
            showPopupMenu(view)
        }

        // 이전 버튼 클릭 이벤트
        prevButton.setOnClickListener {
            if (selectedMonth == 1) {
                selectedMonth = 12
                selectedYear--
            } else {
                selectedMonth--
            }
            updateCalendar()
        }

        // 다음 버튼 클릭 이벤트
        nextButton.setOnClickListener {
            if (selectedMonth == 12) {
                selectedMonth = 1
                selectedYear++
            } else {
                selectedMonth++
            }
            updateCalendar()
        }

        // 초기 캘린더 뷰 업데이트
        updateCalendar()
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item!!.itemId){
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
            R.id.menu_item1-> {
                // 첫 번째 아이템을 클릭했을 때의 동작
                val intent = Intent(this, LoggedInActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item2-> {
                logout()
                true
            }
            R.id.menu_item3-> Toast.makeText(this,"menu_item3 실행",Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun logout() {
        // 사용자 정보 및 세션 정보 삭제
        sharedPreferences.edit().apply {
            remove("isLoggedIn")
            // 필요하다면 추가적인 사용자 정보도 삭제
        }.apply()

        // 로그인 화면으로 이동 (예: LoginActivity)
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    // 캘린더 뷰 업데이트 메소드
    private fun updateCalendar() {
        // 제목 설정
        titleTextView.text = "$selectedYear-$selectedMonth"

        // 기존 뷰를 모두 제거
        val layout = findViewById<LinearLayout>(R.id.calendarLayout)
        layout.removeAllViews()

        // 캘린더 설정
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // 해당 월의 최대 일수와 첫 번째 요일을 가져옴
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-based index

        // 주와 일자를 그리는 루프
        for (i in 0 until 6) {
            // 주 레이아웃 생성 및 설정
            val weekDaysLayout = LinearLayout(this)
            weekDaysLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            weekDaysLayout.orientation = LinearLayout.HORIZONTAL

            // 첫 주의 경우 시작 요일을 설정
            val startDay = if (i == 0) firstDayOfWeek else 0
            val endDay = 7

            var dayNumber = i * 7 + 1 - startDay

            // 일자 뷰 생성
            for (j in 0 until 7) {
                val dayView = TextView(this)
                dayView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                dayView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                // 해당 일자에 일정이 있는 경우 출력
                val finalDayNumber = dayNumber
                if (finalDayNumber in 1..maxDay) {
                    val dateKey = "$selectedYear-$selectedMonth-$finalDayNumber"
                    val schedule = sharedPreferences.getString(dateKey, "")
                    dayView.text = "$finalDayNumber\n$schedule"

                    // 일자를 클릭했을 때의 동작
                    dayView.setOnClickListener {
                        showScheduleDialog(selectedYear, selectedMonth, finalDayNumber)
                    }
                }

                // 주 레이아웃에 일자 뷰 추가
                weekDaysLayout.addView(dayView)
                dayNumber++
            }

            // 캘린더 레이아웃에 주 레이아웃 추가
            layout.addView(weekDaysLayout)
        }
    }

    // 일정 추가/수정 다이얼로그 표시 메소드
    private fun showScheduleDialog(year: Int, month: Int, day: Int) {
        // 일정 키 생성
        val dateKey = "$year-$month-$day"
        // 이전 일정 가져오기
        val prevSchedule = sharedPreferences.getString(dateKey, "") ?: ""
        // 일정 입력을 위한 EditText 생성
        val editText = EditText(this)
        editText.setText(prevSchedule)

        // 다이얼로그 생성 및 표시
        AlertDialog.Builder(this)
            .setTitle("Schedule for $year-$month-$day")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                // 새 일정 저장
                val newSchedule = editText.text.toString()
                sharedPreferences.edit().putString(dateKey, newSchedule).apply()
                updateCalendar()
            }
            .setNegativeButton("Delete") { _, _ ->
                // 일정 삭제
                sharedPreferences.edit().remove(dateKey).apply()
                updateCalendar()
            }
            .setNeutralButton("Cancel", null)
            .show()
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
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        // 팝업 메뉴를 표시합니다.
        popup.show()
    }
}
