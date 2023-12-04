package com.example.sharedcalendar

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.sharedcalendar.CalendarUtil.Companion.scheduleList
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import com.example.sharedcalendar.CalendarUtil.Companion.today
import com.example.sharedcalendar.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private val myRef = database.database.getReference("users")
    private val myScheduleRef = database.database.getReference("schedules")
    // 이미 설정된 알람을 추적하기 위한 맵
    private val existingAlarms = hashMapOf<String, ScheduleData>()

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
        registerPushToken()
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

        syncDatabase() // 알림 설정, 일정 리스트

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

    override fun onStart() {
        super.onStart()
        // 처음에 전체 데이터를 불러오는 ValueEventListener
        myScheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scheduleList.clear()
                for(snapshot in dataSnapshot.children) {
                    val scheduleData = snapshot.getValue(ScheduleData::class.java)
                    scheduleData?.let {
                        scheduleList.add(it)
                        existingAlarms[it.key] = it
                    }
                }
                updateCalendarUI()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })
    }

    private fun syncDatabase() {
        // 이후의 변경을 추적하는 ChildEventListener
        myScheduleRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val newSchedule = dataSnapshot.getValue(ScheduleData::class.java)
                newSchedule?.let {
                    if (!scheduleList.any { schedule -> schedule.key == it.key }) {
                        scheduleList.add(it)
                        setAlarm(it)
                        existingAlarms[it.key] = it
                    }
                }
                updateCalendarUI()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val updatedSchedule = dataSnapshot.getValue(ScheduleData::class.java)
                updatedSchedule?.let { updatedItem ->
                    val index = scheduleList.indexOfFirst { it.key == updatedItem.key }
                    if (index >= 0) {
                        val oldItem = scheduleList[index]

                        // 중요한 데이터가 변경되었는지 확인
                        if (oldItem.isDataChanged(updatedItem)) {
                            scheduleList[index] = updatedItem
                            updateAlarm(updatedItem)
                        }
                    } else {
                        // 리스트에 항목이 없는 경우, 새로 추가합니다.
                        scheduleList.add(updatedItem)
                        setAlarm(updatedItem)
                    }
                    existingAlarms[updatedItem.key] = updatedItem
                }
                updateCalendarUI()
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedSchedule = dataSnapshot.getValue(ScheduleData::class.java)
                removedSchedule?.let {
                    scheduleList.removeAll { it.key == removedSchedule.key }
                    cancelAlarm(removedSchedule)
                    existingAlarms.remove(removedSchedule.key)
                }
                updateCalendarUI()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // 필요한 경우 여기에 로직 추가
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })
    }


    private fun updateCalendarUI() {
        val monthListManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val monthListAdapter = AdapterMonth(binding)
        binding.calendarCustom.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(Int.MAX_VALUE/2 + (selectedDate[Calendar.YEAR] - today[Calendar.YEAR]) * 12 + (selectedDate[Calendar.MONTH] - today[Calendar.MONTH]))
        }
    }




    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setAlarm(scheduleData: ScheduleData) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", scheduleData.key) // 알람 ID를 인텐트에 추가
        }
        val requestCode = scheduleData.key.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmTime = calculateAlarmTime(scheduleData.start_date, scheduleData.start_time, scheduleData.notificationTime)

        // Android 12 이상에서 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            try {
                if (System.currentTimeMillis() < alarmTime) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                    Log.d("parktest","setalarm")
                } else {
                }
            } catch (e: SecurityException) {
                // 예외 처리, 사용자에게 알림 가능
            }
        } else {
            // 사용자를 시스템 설정으로 안내하여 정확한 알람 권한을 활성화
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }

        val sharedPrefs = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("EXTRA_TITLE_${scheduleData.key}", scheduleData.title)
            putInt("EXTRA_NOTIFICATION_TIME_${scheduleData.key}", scheduleData.notificationTime)
            apply()
        }

    }



    private fun calculateAlarmTime(startDate: String, startTime: String, notificationTime: Int): Long {
        return when (notificationTime) {
            0 -> 0 // 알람을 설정하지 않음
            2 -> {
                // 당일 정해진 시간에 알람 설정
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                format.parse("$startDate $startTime")?.time ?: 0
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
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", scheduleData.key)
        }
        val requestCode = scheduleData.key.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // 알람 취소
        alarmManager.cancel(pendingIntent)
        Log.d("parktest","deletalarm")


        // SharedPreferences에서 데이터 제거
        val sharedPrefs = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove("EXTRA_TITLE_${scheduleData.key}")
            remove("EXTRA_NOTIFICATION_TIME_${scheduleData.key}")
            apply()
        }

        // existingAlarms 맵에서 해당 키 삭제
        existingAlarms.remove(scheduleData.key)
        // 제거된 데이터 확인을 위한 로그
    }


    override fun onStop() {
        super.onStop()
        FcmPush.instance.sendMessage("Fcc6hQxKXTezbJiPh3OsMBQMLKw1","asd","asd")
    }
    private fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result

                if (token != null) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val database = FirebaseDatabase.getInstance()
                        val ref = database.getReference("pushtokens").child(uid)

                        // 푸시 토큰을 Firebase 실시간 데이터베이스에 저장
                        ref.child("pushToken").setValue(token)
                    }
                }
            } else {
                // 푸시 토큰 가져오기 실패 시 처리
            }
        }
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
        val currentUser = MyApplication.auth.currentUser
        val uid = currentUser?.uid
        when(item.itemId){
            R.id.menu_logout-> {
                MySharedPreferences.clearUser(this)
                MyApplication.auth.signOut()
                if (uid != null) {
                    val database = FirebaseDatabase.getInstance()
                    val ref = database.getReference("pushtokens").child(uid)

                    // Firebase 실시간 데이터베이스에서 해당 사용자의 푸시 토큰 삭제
                    ref.removeValue().addOnSuccessListener {
                        // 성공적으로 삭제되었을 때의 처리
                    }.addOnFailureListener {
                        // 삭제 실패 시 처리
                    }
                }

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
                            if (uid != null) {
                                val database = FirebaseDatabase.getInstance()
                                val tokenRef = database.getReference("pushtokens").child(uid)
                                // 푸시 토큰 데이터 삭제
                                tokenRef.removeValue().addOnSuccessListener {
                                    // 푸시 토큰 데이터 삭제 성공 처리
                                }.addOnFailureListener {
                                    // 푸시 토큰 데이터 삭제 실패 처리
                                }
                            }

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
