package com.example.sharedcalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private val myRef = database.database.getReference("users")
    private val myScheduleRef = database.database.getReference("schedules")

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

        // 앱 시작 시 알림 채널을 생성합니다.
        createNotificationChannel()

        window.apply {
            //상태바
            statusBarColor = Color.WHITE
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }
        // 앱의 시작 지점에서 Firebase 초기화
        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM 토큰 얻기 실패", task.exception)
                    return@addOnCompleteListener
                }

                // FCM 토큰을 얻은 경우
                val token = task.result
                Log.d(TAG, "FCM 토큰: $token")

                // 여기서 얻은 토큰을 원하는 곳에 저장하거나 사용할 수 있습니다.
            }

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
    private fun createNotificationChannel() {
        // Android Oreo(API 26+) 이상에서는 알림 채널을 생성해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = "My Channel Description"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
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
                            myScheduleRef.removeValue()
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
