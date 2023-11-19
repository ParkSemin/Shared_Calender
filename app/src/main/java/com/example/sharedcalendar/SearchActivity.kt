package com.example.sharedcalendar

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                .child(MyApplication.email_revised.toString())

            // RecyclerView 설정
            adapter = SearchAdapter(searchResults)
            binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.searchRecyclerView.adapter = adapter

            // EditText에 텍스트 입력 시 검색 수행
            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // 이전 텍스트 변경 이벤트
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트 변경 중 이벤트
                }

                override fun afterTextChanged(s: Editable?) {
                    // 텍스트 변경 후 이벤트
                    val query = s.toString().trim()
                    searchDatabase(query)
                }
            })
        }

        private fun searchDatabase(query: String) {
            // Firebase 데이터베이스에서 검색 쿼리 수행
            val queryRef = databaseReference.orderByChild("title")

            queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    searchResults.clear()
                    for (snapshot in dataSnapshot.children) {
                        val scheduleData = snapshot.getValue(ScheduleData::class.java)
                        scheduleData?.let {
                            if (it.title.contains(query, ignoreCase = true)) {
                                // 검색어가 일정명에 포함되어 있는 경우에만 추가
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