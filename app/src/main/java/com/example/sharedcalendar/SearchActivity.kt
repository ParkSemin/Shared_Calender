package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharedcalendar.databinding.ActivitySearchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: SearchAdapter // 사용자 정의 어댑터
    private val searchResults = mutableListOf<ScheduleData>() // 검색 결과를 저장할 리스트

    private lateinit var binding: ActivitySearchBinding // 데이터 바인딩 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 데이터베이스 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().reference.child("schedules")

        // RecyclerView 설정
        // SearchActivity 내부
        adapter = SearchAdapter(searchResults).apply {
            onItemClickListener = object : SearchAdapter.OnItemClickListener {
                override fun onItemClick(schedule: ScheduleData) {
                    // 클릭된 아이템의 ScheduleData를 가지고 AddEventActivity 시작
                    val intent = Intent(this@SearchActivity, AddEventActivity::class.java)
                    intent.putExtra("tempSchedule", schedule)
                    startActivity(intent)
                }
            }
        }
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter


        // EditText에 텍스트 입력 시 검색 수행
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 이전 텍스트 변경 이벤트
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중 이벤트
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후 이벤트
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchDatabase(query)
                } else {
                    // 검색어가 비어있을 때는 검색 결과를 클리어하고 리스트를 업데이트하지 않음
                    searchResults.clear()
                }
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun searchDatabase(query: String) {
        val queryRef = databaseReference.orderByChild("title")

        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                searchResults.clear()
                // 검색어에 따라 결과를 필터링하여 리스트에 추가
                for (snapshot in dataSnapshot.children) {
                    val scheduleData = snapshot.getValue(ScheduleData::class.java)
                    scheduleData?.let {
                        if (it.title.contains(query, ignoreCase = true)) {
                            searchResults.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 검색 오류 처리
            }
        })
    }
}