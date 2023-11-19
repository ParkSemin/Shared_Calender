package com.example.sharedcalendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedcalendar.databinding.ScheduleItemBinding

class SearchAdapter(private val scheduleList: List<ScheduleData>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    // 뷰 홀더를 생성하고 뷰를 연결하는 부분
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 데이터 바인딩을 사용하여 레이아웃을 inflate하고 바인딩 객체 생성
        val binding = ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // 뷰 홀더와 데이터를 바인딩하는 부분
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    // 어댑터가 관리하는 아이템의 개수 반환
    override fun getItemCount(): Int {
        return scheduleList.size
    }

    inner class ViewHolder(private val binding: ScheduleItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // ViewHolder 내의 바인딩된 View들을 참조하는 변수들
        // 예: binding.titleTextView, binding.startDateTextView, ...

        fun bind(schedule: ScheduleData) {
            // ViewHolder 내의 View에 데이터를 바인딩하는 함수
            binding.titleTextView.text = schedule.title // 일정 제목 텍스트 설정
            binding.startDateTextView.text = schedule.start_date // 시작 날짜 텍스트 설정
            binding.startTimeTextView.text = schedule.start_time // 시작 시간 텍스트 설정
            binding.endDateTextView.text = schedule.end_date // 종료 날짜 텍스트 설정
            binding.endTimeTextView.text = schedule.end_time // 종료 시간 텍스트 설정
            // 다른 필드에 대한 바인딩도 추가 가능
        }
    }
}