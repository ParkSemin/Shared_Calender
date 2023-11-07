package com.example.sharedcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.sharedcalendar.databinding.ActivityAddEventBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import org.jetbrains.annotations.Async.Schedule
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.Calendar

class AddEventActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddEventBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private var myRef = database.database.getReference("schedules").child(MyApplication.email_revised.toString())

    // MainActivity로부터 넘겨 받은 년, 월, 일, 요일 정보 저장할 year, month, day, dayOfWeekString 멤버 변수 선언
    private var year = 0
    private var month  = 0
    private var day = 0
    private var dayOfWeekString = ""

    // 시간(기본 값 : 08시)과 분(기본 값 00분)을 저장할 멤버 변수 hour, minute 선언
    private var hour = 8
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

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MainActivity에서 넘겨받은 selectedDate 설정(API 33 이후부터는 if 문의 '참'부분, 33 미만은 '거짓'부분)
        val selectedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("selectedDate", Calendar::class.java)
        } else {
            intent.getSerializableExtra("selectedDate") as Calendar
        }

        // 넘겨 받은 selectedDate를 통해 year, month, day 변수 초기화
        year = selectedDate!!.get(Calendar.YEAR)
        month = selectedDate!!.get(Calendar.MONTH)
        day = selectedDate!!.get(Calendar.DAY_OF_MONTH)
        dayOfWeekString = when (selectedDate!!.get(Calendar.DAY_OF_WEEK)) {
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

        // 등록하려는 일정의 날짜가 오늘이라면 현재 시간으로 시작 시간 및 종료 시간 설정
        val calendar = Calendar.getInstance()
        if (selectedDate[Calendar.YEAR] == calendar[Calendar.YEAR] && selectedDate[Calendar.MONTH] == calendar[Calendar.MONTH] && selectedDate[Calendar.DAY_OF_MONTH] == calendar[Calendar.DAY_OF_MONTH]) {
            hour = calendar[Calendar.HOUR_OF_DAY]
            minute = calendar[Calendar.MINUTE]
        } else {
            hour = 8
            minute = 0
        }
        // hour, minute의 초깃값으로 시작 시간, 종료 시간 설정
        startHour = hour
        startMinute = minute
        endHour = hour+1
        endMinute = minute
        updateTimeButtonText()

        // year, month, day를 startDateButton, endDateButton의 text로 설정
        binding.startDateButton.text = String.format("%s월 %s일 (%s)", startMonth + 1, startDay, dayOfWeekString)
        binding.endDateButton.text = String.format("%s월 %s일 (%s)", endMonth + 1, endDay, dayOfWeekString)

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
                    // DB에 추가할 데이터 한번에 정의
                    val scheduleData = ScheduleData(
                        "${binding.title.text}",
                        "${startYear}-${startMonth+1}-${startDay}",
                        "${startHour}:${startMinute}",
                        "${endYear}-${endMonth+1}-${endDay}",
                        "${endHour}:${endMinute}"
                    )
                    myRef.push().setValue(scheduleData)
                        .addOnSuccessListener {
                            MotionToast.darkColorToast(
                                this,
                                "일정 추가 완료",
                                "일정이 성공적으로 추가되었습니다",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                            )
                            val intent: Intent = Intent(this, MainActivity::class.java)
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
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                            )
                        }
                }
                true
            }
            else -> false
        }
    }
}