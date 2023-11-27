package com.example.sharedcalendar

import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColor
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.sharedcalendar.CalendarUtil.Companion.scheduleList
import com.example.sharedcalendar.databinding.ActivityMainBinding
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate // import selectedDate
import com.example.sharedcalendar.CalendarUtil.Companion.today // import today
import com.google.android.gms.tasks.OnCompleteListener
import java.util.Calendar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private val myRef = database.database.getReference("users")
    private val myScheduleRef = database.database.getReference("schedules")
    private val REQUEST_PERMISSION_CODE = 123 // 권한 요청 코드 (임의 설정)

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

        window.apply {
            //상태바
            statusBarColor = Color.WHITE
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        syncDatabase() // 알림설정

        selectedDate.clear(Calendar.HOUR_OF_DAY)
        selectedDate.clear(Calendar.MINUTE)
        selectedDate.clear(Calendar.SECOND)
        selectedDate.clear(Calendar.MILLISECOND)

        today.clear(Calendar.HOUR_OF_DAY)
        today.clear(Calendar.MINUTE)
        today.clear(Calendar.SECOND)
        today.clear(Calendar.MILLISECOND)

        val monthListManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val monthListAdapter = AdapterMonth(binding)
        binding.calendarCustom.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(Int.MAX_VALUE/2 + (selectedDate[Calendar.YEAR] - today[Calendar.YEAR]) * 12 + (selectedDate[Calendar.MONTH] - today[Calendar.MONTH]))
        }
        val snap = PagerSnapHelper()
        snap.attachToRecyclerView(binding.calendarCustom)
        binding.showDiaryView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // AddEventActivity에서 일정 추가하고 다시 돌아오면 변경 사항을 반영해야 함
        myScheduleRef.addValueEventListener(object: ValueEventListener {
            // DB 일정 데이터를 성공적으로 가져온 경우
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scheduleList.clear()
                CoroutineScope(Dispatchers.IO).launch {
                    runBlocking {
                        for(snapshot in dataSnapshot.children) {
                            val scheduleData = snapshot.getValue(ScheduleData::class.java)
                            scheduleData?.let {
                                // 읽어온 데이터를 리스트에 추가
                                scheduleList.add(it)
                            }
                        }
                    }
                }
                binding.calendarCustom.apply {
                    layoutManager = monthListManager
                    adapter = monthListAdapter
                    scrollToPosition(Int.MAX_VALUE/2 + (selectedDate[Calendar.YEAR] - today[Calendar.YEAR]) * 12 + (selectedDate[Calendar.MONTH] - today[Calendar.MONTH]))
                }
            }

            // DB 일정 데이터를 가져오지 못한 경우
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "DB 데이터 읽기 실패", Toast.LENGTH_LONG).show()
            }
        })

        // 뒤로가기 콜백 추가
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Toolbar 설정
        val toolbar = binding.toolbar // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용
        toolbar.setBackgroundColor(Color.WHITE)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // 홈버튼 이미지 변경
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
        tvEmail.text = MyApplication.email

        // FAB (Floating Action Button)를 찾아서 변수에 저장합니다.
        val fab = binding.fab

        // FAB 버튼에 클릭 리스너를 설정합니다.
        fab.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            intent.putExtra("selectedDate", selectedDate) // selectedDate를 "selectedDate"라는 키로 넘겨줌
            startActivity(intent)
            true
        }
    }
    private fun syncDatabase() {
        myScheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                handleDatabaseChanges(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })
    }
    fun handleDatabaseChanges(dataSnapshot: DataSnapshot) {
        // 이미 설정된 알람을 추적하기 위한 맵
        val existingAlarms = hashMapOf<String, ScheduleData>()

        dataSnapshot.children.forEach { scheduleSnapshot ->
            val schedule = scheduleSnapshot.getValue(ScheduleData::class.java)
            schedule?.let {
                if (existingAlarms.containsKey(it.key)) {
                    updateAlarm(it) // 알람 수정
                } else {
                    setAlarm(it) // 새 알람 설정
                }
                existingAlarms[it.key] = it
            }
        }

        // 데이터베이스에서 삭제된 알람 확인 및 제거
        existingAlarms.keys.forEach { key ->
            if (!dataSnapshot.hasChild(key)) {
                cancelAlarm(existingAlarms[key]!!)
                existingAlarms.remove(key)
            }
        }
    }
    private fun setAlarm(scheduleData: ScheduleData) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", scheduleData.title) // 일정명 추가
            putExtra("EXTRA_NOTIFICATION_TIME", scheduleData.notificationTime) // notificationTime 추가
        }
        val pendingIntent = PendingIntent.getBroadcast(this, scheduleData.key.hashCode(), alarmIntent, PendingIntent.FLAG_IMMUTABLE)

        val alarmTime = calculateAlarmTime(scheduleData.start_date, scheduleData.start_time, scheduleData.notificationTime)
        if (System.currentTimeMillis() < alarmTime) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        } else {
            // 현재 시간이 알람 시간보다 이후이면, 알람 설정 안 함
            // 필요하다면 여기에 사용자에게 알림 시간이 지났음을 알리는 로직을 추가할 수 있습니다.
        }
        Log.d("AlarmManager", "Alarm set for: $alarmTime")
    }
    private fun calculateAlarmTime(startDate: String, startTime: String, notificationTime: Int): Long {
        return when (notificationTime) {
            0 -> 0 // 알람을 설정하지 않음
            1 -> {
                // 당일 정해진 시간에 알람 설정
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                format.parse("$startDate $startTime")?.time ?: 0
            }
            100 -> {
                // 현재 시간으로부터 10초 후
                System.currentTimeMillis() + 10_000
            }
            else -> {
                // 기본 로직: notificationTime만큼 빼기
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dateTime = format.parse("$startDate $startTime") ?: return 0
                val calendar = Calendar.getInstance().apply {
                    time = dateTime
                    add(Calendar.MINUTE, -notificationTime)
                }
                calendar.timeInMillis
            }
        }
    }

    private fun updateAlarm(scheduleData: ScheduleData) {
        cancelAlarm(scheduleData) // 먼저 기존 알람을 취소
        setAlarm(scheduleData) // 그리고 새 알람을 설정
    }

    private fun cancelAlarm(scheduleData: ScheduleData) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, scheduleData.key.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager.cancel(alarmIntent)
    }


    // 메뉴 옵션 생성
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        return when(item.itemId){
            android.R.id.home->{
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
                            // 1. DB에 등록된 사용자 계정 삭제
                            myRef.child(MyApplication.email_revised.toString()).removeValue()
                            // 2. 탈퇴하려는 사용자가 최초 등록자인 일정 찾아서 삭제
                            myScheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (eventSnapshot in dataSnapshot.children) {
                                        val event = eventSnapshot.getValue(ScheduleData::class.java)

                                        // firstTimeRegistrantAccount와 탈퇴하는 회원의 이메일 비교
                                        if (event?.firstTimeRegistrantAccount == MyApplication.email) {
                                            // 해당 사용자가 등록한 일정이므로 삭제
                                            eventSnapshot.ref.removeValue() // 이벤트 삭제
                                        }
                                    }
                                }
                                // DB 일정 데이터를 가져오지 못한 경우
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(applicationContext, "DB 데이터 읽기 실패", Toast.LENGTH_LONG).show()
                                }
                            })
                            // 3. Authentication에 등록된 사용자 계정 삭제
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
