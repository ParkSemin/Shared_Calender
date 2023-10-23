package com.example.sharedcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivityAddEventBinding
import java.util.Calendar

class AddEventActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddEventBinding.inflate(layoutInflater) }

    // MainActivity로부터 넘겨 받은 년, 월, 일, 요일 정보 저장할 year, month, day, dayOfWeekString 멤버 변수 선언
    private var year = 0
    private var month  = 0
    private var day = 0
    private var dayOfWeekString = ""

    // 시간(기본 값 : 08시)과 분(기본 값 00분)을 저장할 멤버 변수 hour, minute 선언
    private var hour = 8
    private var minute = 0

    // 시작 일과 종료 일을 클래스의 멤버 변수로 선언
    private var start_year = 0
    private var start_month = 0
    private var start_day = 0
    private var start_dayOfWeekString = ""
    private var end_year = 0
    private var end_month = 0
    private var end_day = 0
    private var end_dayOfWeekString = ""

    // 시작 시간과 종료 시간 또한 멤버 변수로 선언
    private var start_hour = 0
    private var start_minute = 0
    private var end_hour = 0
    private var end_minute = 0

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
        start_year = year
        start_month = month
        start_day = day
        start_dayOfWeekString = dayOfWeekString
        end_year = year
        end_month = month
        end_day = day
        end_dayOfWeekString = dayOfWeekString

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
        start_hour = hour
        start_minute = minute
        end_hour = hour+1
        end_minute = minute
        updateTimeButtonText()

        // year, month, day를 startDateButton, endDateButton의 text로 설정
        binding.startDateButton.text = "${start_month + 1}월 ${start_day}일 (${dayOfWeekString})"
        binding.endDateButton.text = "${end_month + 1}월 ${end_day}일 (${dayOfWeekString})"

        // DatePickerDialog 설정
        binding.startDateButton.setOnClickListener {
            showDatePickerDialogForButton(
                binding.startDateButton,
                start_year,
                start_month,
                start_day
            )
        }
        binding.endDateButton.setOnClickListener {
            showDatePickerDialogForButton(binding.endDateButton, end_year, end_month, end_day)
        }

        // TimePickerDialog 설정
        binding.startTimeButton.setOnClickListener {
            showTimePickerDialogForButton(binding.startTimeButton, start_hour, start_minute)
        }
        binding.endTimeButton.setOnClickListener {
            showTimePickerDialogForButton(binding.endTimeButton, end_hour, end_minute)
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
                start_year = selectedYear
                start_month = selectedMonth
                start_day = selectedDay
                start_dayOfWeekString = dayOfWeekString

                // 갱신된 시작 일이 종료 일보다 더 미래인 경우, 종료 일 갱신
                if (start_year > end_year) {
                    end_year = start_year
                    end_month = start_month
                    end_day = start_day
                    end_dayOfWeekString = start_dayOfWeekString
                } else if (start_month > end_month) {
                    end_month = start_month
                    end_day = start_day
                    end_dayOfWeekString = start_dayOfWeekString
                } else if (start_day > end_day) {
                    end_day = start_day
                    end_dayOfWeekString = start_dayOfWeekString
                }
            } else if (button == binding.endDateButton) {
                end_year = selectedYear
                end_month = selectedMonth
                end_day = selectedDay
                end_dayOfWeekString = dayOfWeekString

                // 갱신된 종료 일이 시작 일보다 더 과거인 경우, 시작 일 갱신
                if (start_year > end_year) {
                    start_year = end_year
                    start_month = end_month
                    start_day = end_day
                    start_dayOfWeekString = end_dayOfWeekString
                } else if (start_month > end_month) {
                    start_month = end_month
                    start_day = end_day
                    start_dayOfWeekString = end_dayOfWeekString
                } else if (start_day > end_day) {
                    start_day = end_day
                    start_dayOfWeekString = end_dayOfWeekString
                }
            }

            // 선택된 날짜로 버튼의 텍스트 갱신
            updateDateButtonText()
        }, year, month, day)

        datePickerDialog.show()
    }

    // 날짜 선택 후 버튼의 텍스트도 갱신시키는 함수
    private fun updateDateButtonText() {
        binding.startDateButton.text = "${start_month+1}월 ${start_day}일 (${start_dayOfWeekString})"
        binding.endDateButton.text = "${end_month+1}월 ${end_day}일 (${end_dayOfWeekString})"
    }

    // 시간 선택 함수
    private fun showTimePickerDialogForButton(button: Button, hour: Int, minute: Int) {
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // 선택된 시간, 분을 hour, minute로 갱신
            this.hour = selectedHour
            this.minute = selectedMinute

            // 이벤트 발생 버튼에 따라 시작 시간 또는 종료 시간을 갱신
            if (button == binding.startTimeButton) {
                start_hour = selectedHour
                start_minute = selectedMinute

                // 시작 시간이 종료 시간보다 크거나 같은 경우 -> 종료 시간 갱신
                if (compareStartTimeWithEndTime() == false) {
                    end_hour = start_hour+1
                    end_minute = start_minute
                }
            } else { // button == binding.endTimeButton
                end_hour = selectedHour
                end_minute = selectedMinute

                // 시작 시간이 종료 시간보다 크거나 같은 경우 -> 시작 시간 갱신
                if (compareStartTimeWithEndTime() == false) {
                    start_hour = end_hour-1
                    start_minute = end_minute
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
        startCalendar.set(Calendar.YEAR, start_year)
        startCalendar.set(Calendar.MONTH, start_month)
        startCalendar.set(Calendar.DAY_OF_MONTH, start_day)
        startCalendar.set(Calendar.HOUR_OF_DAY, start_hour)
        startCalendar.set(Calendar.MINUTE, start_minute)

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.YEAR, end_year)
        endCalendar.set(Calendar.MONTH, end_month)
        endCalendar.set(Calendar.DAY_OF_MONTH, end_day)
        endCalendar.set(Calendar.HOUR_OF_DAY, end_hour)
        endCalendar.set(Calendar.MINUTE, end_minute)

        // 시작 시간이 종료 시간보다 과거라면 true, 같거나 미래라면 false
        return startCalendar.before(endCalendar)
    }

    // 시간 선택 후 버튼의 텍스트도 갱신시키는 함수
    private fun updateTimeButtonText() {
        var isPM = start_hour >= 12 // 오후면 true, 오전이면 false
        var hourToShow = if (start_hour % 12 == 0) 12 else start_hour % 12
        binding.startTimeButton.text = String.format("%s %02d:%02d", if (isPM) "오후" else "오전", hourToShow, start_minute)

        isPM = end_hour >= 12 // 오후면 true, 오전이면 false
        hourToShow = if (end_hour % 12 == 0) 12 else end_hour % 12
        binding.endTimeButton.text = String.format("%s %02d:%02d", if (isPM) "오후" else "오전", hourToShow, end_minute)
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
                // 체크 버튼을 클릭했을 때 실행할 동작 작성
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}