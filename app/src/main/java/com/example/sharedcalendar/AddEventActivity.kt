package com.example.sharedcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.sharedcalendar.CalendarUtil.Companion.selectedDate
import com.example.sharedcalendar.databinding.ActivityAddEventBinding
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.Calendar

class AddEventActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddEventBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    // private var myRef = database.database.getReference("schedules").child(MyApplication.email_revised.toString())
    private val myRef = database.database.getReference("schedules")

    // MainActivity로부터 넘겨 받은 년, 월, 일, 요일 정보 저장할 year, month, day, dayOfWeekString 멤버 변수 선언
    private var year = 0
    private var month  = 0
    private var day = 0
    private var dayOfWeekString = ""

    // 시간(기본 값 : 00시)과 분(기본 값 00분)을 저장할 멤버 변수 hour, minute 선언
    private var hour = 0
    private var minute = 0

    // 시작 일과 종료 일을 클래스의 멤버 변수로 선언
    private var startYear = 0
    private var startMonth = 0
    private var startDay = 0
    private var startDayOfWeekString = ""
    private var endYear = 0
    private var endMonth = 0
    private var endDay = 0
    private var endDayOfWeekString = ""

    // 시작 시간과 종료 시간 또한 멤버 변수로 선언
    private var startHour = 0
    private var startMinute = 0
    private var endHour = 0
    private var endMinute = 0

    private var minutesBefore = 0

    // AdapterSchedule에서 넘겨받은 tempSchedule을 클래스 전체에서 사용하기 위해 멤버 변수로 선언
    var schedule: ScheduleData? = null

    // 일정 색상을 사용하기 위해 멤버 변수로 선언
    var scheduleColor: Int = 0 // 일단 0으로 하고 밑에서 바로 초기화 진행함

    // 다이얼로그 객체 선언
    private lateinit var dialog: AlertDialog

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // 기본 색상 설정
        scheduleColor = ContextCompat.getColor(applicationContext, R.color.default_color_schedule)

        // AdapterSchedule에서 넘겨받은 tempSchedule 설정(API 33 이후부터는 if 문의 '참'부분, 33 미만은 '거짓'부분)
        schedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("tempSchedule", ScheduleData::class.java)
        } else {
            intent.getSerializableExtra("tempSchedule") as ScheduleData
        }

        val minutesBeforeTextView = findViewById<TextView>(R.id.minutesBeforeTextView)
        minutesBeforeTextView.text = "알림 없음"

        // 일정을 추가하는 경우 schedule == null이고 수정하는 경우에는 해당 일정 정보가 들어감
        if (schedule == null) {
            // 일정을 추가하는 경우 최초 등록한 사람은 본인, 최종 수정한 사람은 없으므로 공백
            binding.firstTimeRegistrant.text = MySharedPreferences.getUserName(this)
            binding.finalReviser.text = ""

            // 일정 삭제 버튼 안보이게 함
            binding.btnDeleteSchedule.isVisible = false

            // 선택된 날짜인 selectedDate를 통해 year, month, day 변수 초기화
            year = selectedDate.get(Calendar.YEAR)
            month = selectedDate.get(Calendar.MONTH)
            day = selectedDate.get(Calendar.DAY_OF_MONTH)
            dayOfWeekString = when (selectedDate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }

            // 추출한 year, month, day를 멤버 변수로 초기화
            startYear = year
            startMonth = month
            startDay = day
            startDayOfWeekString = dayOfWeekString
            endYear = year
            endMonth = month
            endDay = day
            endDayOfWeekString = dayOfWeekString

            // 등록하려는 일정의 날짜가 오늘이라면 현재 시간을 올림한 시간으로 시작 시간 및 종료 시간 설정
            hour = selectedDate[Calendar.HOUR_OF_DAY] + 1
            // hour, minute의 초깃값으로 시작 시간, 종료 시간 설정
            startHour = hour
            startMinute = minute
            endHour = hour + 1
            endMinute = minute
        }
        else /* --- 일정 수정인 경우 --- */
        {
            // 일정을 수정하는 경우 최초 등록한 사람은 등록된 일정의 정보를 바탕으로, 최종 수정하는 사람은 본인으로 설정
            binding.firstTimeRegistrant.text = schedule!!.firstTimeRegistrant
            binding.finalReviser.text = MySharedPreferences.getUserName(this)

            // 일정 색상 설정
            scheduleColor = schedule!!.color

            // 일정 삭제 버튼 보이게 하고 리스너 등록
            binding.btnDeleteSchedule.isVisible = true
            binding.btnDeleteSchedule.setOnClickListener {
                // 일정을 등록한 사람만 삭제할 수 있도록 보안 설정
                if (schedule!!.firstTimeRegistrantAccount == MyApplication.email) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("경고")
                        .setMessage("일정을 삭제하시겠습니까?")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { _, _ ->
                                myRef.child(schedule!!.key).removeValue()
                                MotionToast.darkColorToast(
                                    this,
                                    "삭제 완료",
                                    "일정이 삭제되었습니다",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                                )
                                sendBroadcastMessage("일정 알림", "${binding.title.text} 일정이 삭제되었습니다.")
                                val intent: Intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            })
                        .setNegativeButton("취소", null)
                    builder.show()
                } else {
                    // 다른 사용자들은 일정을 삭제할 수 없음
                    MotionToast.darkColorToast(
                        this,
                        "일정 삭제 실패",
                        "최초 등록자(${schedule!!.firstTimeRegistrant})만 삭제할 수 있습니다",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(
                            this,
                            www.sanju.motiontoast.R.font.helvetica_regular
                        )
                    )
                }

            }

            // 1. 일정 제목 설정
            binding.title.setText(schedule!!.title)

            // 2. 시작일 설정
            val startDateParts = schedule!!.start_date.split('-')
            startYear = startDateParts[0].toInt()
            startMonth = startDateParts[1].toInt() - 1
            startDay = startDateParts[2].toInt()

            // 3. 종료일 설정
            val endDateParts = schedule!!.end_date.split('-')
            endYear = endDateParts[0].toInt()
            endMonth = endDateParts[1].toInt() - 1
            endDay = endDateParts[2].toInt()

            // 4. 시작 시간 설정
            val startTimeParts = schedule!!.start_time.split(':')
            startHour = startTimeParts[0].toInt()
            startMinute = startTimeParts[1].toInt()

            // 5. 종료 시간 설정
            val endTimeParts = schedule!!.end_time.split(':')
            endHour = endTimeParts[0].toInt()
            endMinute = endTimeParts[1].toInt()

            // 6. 시작 요일 설정
            val tempCal: Calendar = Calendar.getInstance()
            tempCal.set(Calendar.YEAR, startYear)
            tempCal.set(Calendar.MONTH, startMonth)
            tempCal.set(Calendar.DAY_OF_MONTH, startDay)
            startDayOfWeekString = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }

            // 7. 종료 요일 설정(tempCal의 날짜만 초기화해서 재사용)
            tempCal.set(Calendar.YEAR, endYear)
            tempCal.set(Calendar.MONTH, endMonth)
            tempCal.set(Calendar.DAY_OF_MONTH, endDay)
            endDayOfWeekString = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }

            // 7. 알림 설정
            minutesBefore = schedule!!.notificationTime
            minutesBeforeTextView.text = when (minutesBefore) {
                0 -> "알림 없음"
                2 -> "정시 알림"
                60 -> "알림 1시간 전"
                120 -> "알림 2시간 전"
                else -> "알림 ${minutesBefore}분 전"
            }
        }



        // 설정한 날짜, 시간을 버튼 텍스트에 설정
        updateTimeButtonText()
        updateDateButtonText()


        // DatePickerDialog 설정
        binding.startDateButton.setOnClickListener {
            showDatePickerDialogForButton(
                binding.startDateButton,
                startYear,
                startMonth,
                startDay
            )
        }
        binding.endDateButton.setOnClickListener {
            showDatePickerDialogForButton(binding.endDateButton, endYear, endMonth, endDay)
        }

        // TimePickerDialog 설정
        binding.startTimeButton.setOnClickListener {
            showTimePickerDialogForButton(binding.startTimeButton, startHour, startMinute)
        }
        binding.endTimeButton.setOnClickListener {
            showTimePickerDialogForButton(binding.endTimeButton, endHour, endMinute)
        }

        // Toolbar 설정
        val toolbar = binding.toolbar // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cancel)
            setDisplayShowTitleEnabled(false) // 툴바에 타이틀이 나타나지 않도록 설정
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 툴바, 색상 미리보기, 상태바 색상 변경
        toolbar.setBackgroundColor(scheduleColor)
        binding.previewColor.backgroundTintList = ColorStateList.valueOf(scheduleColor)
        window.statusBarColor = scheduleColor

        // Color Picker Dialog 설정
        binding.colorPicker.setOnClickListener {
            // Kotlin Code
            MaterialColorPickerDialog
                .Builder(this)
                .setTitle("색상 선택")
                .setColorShape(ColorShape.CIRCLE)
                .setDefaultColor(R.color.default_color_schedule)
                .setColorRes(resources.getIntArray(R.array.colors))
                .setColorListener { color, _ ->
                    if (color == -16383835) { // 아무것도 선택 안하고 확인 누르면 이 색상이 들어가는데 라이브러리 수정하려고 봤지만 모르겠어서 해당 색상일 경우에 대한 조건문을 설정하였음
                        scheduleColor = ContextCompat.getColor(applicationContext, R.color.default_color_schedule)
                    } else {
                        scheduleColor = color
                    }
                    toolbar.setBackgroundColor(scheduleColor)
                    binding.previewColor.backgroundTintList = ColorStateList.valueOf(scheduleColor)
                    window.statusBarColor = scheduleColor
                }
                .show()
        }
        binding.notificationButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("알림 설정")
            val options = arrayOf(
                "알림 없음",
                "정시 알림",
                "5분 전",
                "10분 전",
                "15분 전",
                "30분 전",
                "1시간 전",
                "2시간 전"
            )

            val notificationOptionsWithVariables = mapOf(
                "알림 없음" to 0,
                "정시 알림" to 2,
                "5분 전" to 5,
                "10분 전" to 10,
                "15분 전" to 15,
                "30분 전" to 30,
                "1시간 전" to 60,
                "2시간 전" to 120,
            )

            builder.setItems(options) { _, which ->
                val selectedOption = options[which]
                minutesBefore = notificationOptionsWithVariables[selectedOption] ?: 0

                // 선택된 옵션을 사용하여 UI 업데이트 또는 데이터 처리
                minutesBeforeTextView.text = when (minutesBefore) {
                    0 -> "알림 없음"
                    2 -> "정시 알림"
                    60 -> "알림 1시간 전"
                    120 -> "알림 2시간 전"
                    else -> "알림 ${minutesBefore}분 전"
                }
            }

            builder.show()
        }

    }



    // 날짜 선택 함수
    private fun showDatePickerDialogForButton(button: Button, year: Int, month: Int, day: Int) {
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // 선택된 년, 월, 일을 year, month, day로 갱신
            this.year = selectedYear
            this.month = selectedMonth
            this.day = selectedDay

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

            val dayOfWeekString = when (selectedCalendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }

            // 갱신된 년, 월, 일로 부터 멤버 변수를 갱신
            if (button == binding.startDateButton) {
                startYear = selectedYear
                startMonth = selectedMonth
                startDay = selectedDay
                startDayOfWeekString = dayOfWeekString

                // 갱신된 시작 일이 종료 일보다 더 미래인 경우, 종료 일 갱신
                if (startYear > endYear) {
                    endYear = startYear
                    endMonth = startMonth
                    endDay = startDay
                    endDayOfWeekString = startDayOfWeekString
                } else if (startMonth > endMonth) {
                    endMonth = startMonth
                    endDay = startDay
                    endDayOfWeekString = startDayOfWeekString
                } else if (startDay > endDay) {
                    endDay = startDay
                    endDayOfWeekString = startDayOfWeekString
                }
            } else if (button == binding.endDateButton) {
                endYear = selectedYear
                endMonth = selectedMonth
                endDay = selectedDay
                endDayOfWeekString = dayOfWeekString

                // 갱신된 종료 일이 시작 일보다 더 과거인 경우, 시작 일 갱신
                if (startYear > endYear) {
                    startYear = endYear
                    startMonth = endMonth
                    startDay = endDay
                    startDayOfWeekString = endDayOfWeekString
                } else if (startMonth > endMonth) {
                    startMonth = endMonth
                    startDay = endDay
                    startDayOfWeekString = endDayOfWeekString
                } else if (startDay > endDay) {
                    startDay = endDay
                    startDayOfWeekString = endDayOfWeekString
                }
            }

            // 선택된 날짜로 버튼의 텍스트 갱신
            updateDateButtonText()
        }, year, month, day)

        datePickerDialog.show()
    }

    // 날짜 선택 후 버튼의 텍스트도 갱신시키는 함수
    private fun updateDateButtonText() {
        binding.startDateButton.text = String.format("%s월 %s일 (%s)", startMonth + 1, startDay, startDayOfWeekString)
        binding.endDateButton.text = String.format("%s월 %s일 (%s)", endMonth + 1, endDay, endDayOfWeekString)
    }

    private fun sendBroadcastMessage(title: String, message: String) {
        val ref = database.database.getReference("pushtokens")

        // 모든 사용자의 UID를 가져와서 알림을 보냅니다.
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val uid = childSnapshot.key
                    uid?.let {
                        FcmPush.instance.sendMessage(uid, title, message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
                Log.e("sendBroadcastMessage", "Database error: ${error.message}")
            }
        })
        Log.d("sendBroadcastMessage", "Broadcast message sent: Title: $title, Message: $message")
    }
    //override fun onStop() {
    //    super.onStop()
    //    sendBroadcastMessage("qweqwe","qweqwe")
    //}

    // 시간 선택 함수
    private fun showTimePickerDialogForButton(button: Button, hour: Int, minute: Int) {
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // 선택된 시간, 분을 hour, minute로 갱신
            this.hour = selectedHour
            this.minute = selectedMinute

            // 이벤트 발생 버튼에 따라 시작 시간 또는 종료 시간을 갱신
            if (button == binding.startTimeButton) {
                startHour = selectedHour
                startMinute = selectedMinute

                // 시작 시간이 종료 시간보다 크거나 같은 경우 -> 종료 시간 갱신
                if (!compareStartTimeWithEndTime()) {
                    endHour = startHour+1
                    endMinute = startMinute
                }
            } else { // button == binding.endTimeButton
                endHour = selectedHour
                endMinute = selectedMinute

                // 시작 시간이 종료 시간보다 크거나 같은 경우 -> 시작 시간 갱신
                if (!compareStartTimeWithEndTime()) {
                    startHour = endHour-1
                    startMinute = endMinute
                }
            }

            // 선택된 시간으로 버튼의 텍스트 갱신
            updateTimeButtonText()
        }, hour, minute, false) // 24시간제가 아닌 12시간제로 설정

        timePickerDialog.show()
    }

    // 선택된 시간을 비교하여 시작 시간과 종료 시간을 변경하는 함수
    private fun compareStartTimeWithEndTime(): Boolean {
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.YEAR, startYear)
        startCalendar.set(Calendar.MONTH, startMonth)
        startCalendar.set(Calendar.DAY_OF_MONTH, startDay)
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour)
        startCalendar.set(Calendar.MINUTE, startMinute)

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.YEAR, endYear)
        endCalendar.set(Calendar.MONTH, endMonth)
        endCalendar.set(Calendar.DAY_OF_MONTH, endDay)
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour)
        endCalendar.set(Calendar.MINUTE, endMinute)

        // 시작 시간이 종료 시간보다 과거라면 true, 같거나 미래라면 false
        return startCalendar.before(endCalendar)
    }

    // 시간 선택 후 버튼의 텍스트도 갱신시키는 함수
    private fun updateTimeButtonText() {
        var isPM = startHour >= 12 // 오후면 true, 오전이면 false
        var hourToShow = if (startHour % 12 == 0) 12 else startHour % 12
        binding.startTimeButton.text = String.format("%s %02d:%02d", if (isPM) "오후" else "오전", hourToShow, startMinute)

        isPM = endHour >= 12 // 오후면 true, 오전이면 false
        hourToShow = if (endHour % 12 == 0) 12 else endHour % 12
        binding.endTimeButton.text = String.format("%s %02d:%02d", if (isPM) "오후" else "오전", hourToShow, endMinute)
    }

    @Override
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_add_event, menu)
        return true
    }


    @Override
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done_add_event -> {
                if (binding.title.text.isBlank()) {
                    MotionToast.darkColorToast(
                        this,
                        "일정 추가 실패",
                        "일정명을 입력하세요",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                    )
                } else {
                    /* --- 일정을 추가하는 경우(schedule == null) --- */
                    if (schedule == null) {
                        val scheduleRef = myRef.push()
                        // DB에 추가할 데이터 한번에 정의(최초 등록인 == 최종 수정인)
                        val scheduleData = ScheduleData(
                            scheduleRef.key!!,
                            MySharedPreferences.getUserName(this),
                            MyApplication.email!!,
                            MySharedPreferences.getUserName(this),
                            MyApplication.email!!,
                            "${binding.title.text}",
                            "${startYear}-${startMonth + 1}-${startDay}",
                            "${startHour}:${startMinute}",
                            "${endYear}-${endMonth + 1}-${endDay}",
                            "${endHour}:${endMinute}",
                            scheduleColor,
                            minutesBefore
                        )

                        scheduleRef.setValue(scheduleData)
                            .addOnSuccessListener {
                                MotionToast.darkColorToast(
                                    this,
                                    "일정 추가 완료",
                                    "일정이 성공적으로 추가되었습니다",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        this,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )
                                sendBroadcastMessage("일정 알림", "${binding.title.text} 일정이 추가되었습니다.")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnCanceledListener {
                                MotionToast.darkColorToast(
                                    this,
                                    "일정 추가 실패",
                                    "다시 시도해주세요",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        this,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )
                            }
                    } else { /* --- 일정을 수정하는 경우 --- */
                        // 일정을 수정하면 최종 수정한 사람이 본인으로 바뀜
                        val scheduleRef = myRef.child(schedule!!.key)
                        // DB에 추가할 데이터 한번에 정의
                        val scheduleData = ScheduleData(
                            scheduleRef.key!!,
                            schedule!!.firstTimeRegistrant,
                            schedule!!.firstTimeRegistrantAccount,
                            MySharedPreferences.getUserName(this),
                            MyApplication.email!!,
                            "${binding.title.text}",
                            "${startYear}-${startMonth + 1}-${startDay}",
                            "${startHour}:${startMinute}",
                            "${endYear}-${endMonth + 1}-${endDay}",
                            "${endHour}:${endMinute}",
                            scheduleColor,
                            minutesBefore
                        )

                        scheduleRef.setValue(scheduleData)
                            .addOnSuccessListener {
                                MotionToast.darkColorToast(
                                    this,
                                    "일정 수정 완료",
                                    "일정이 성공적으로 수정되었습니다",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        this,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )
                                sendBroadcastMessage("일정 알림", "${binding.title.text} 일정이 수정되었습니다.")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnCanceledListener {
                                MotionToast.darkColorToast(
                                    this,
                                    "일정 수정 실패",
                                    "다시 시도해주세요",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        this,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )
                            }
                    }
                }
                true
            }
            else -> false
        }
    }
}