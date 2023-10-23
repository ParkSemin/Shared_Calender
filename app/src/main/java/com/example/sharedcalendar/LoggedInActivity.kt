package com.example.sharedcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivityLoggedInBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LoggedInActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivityLoggedInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩 초기화
        binding = ActivityLoggedInBinding.inflate(layoutInflater)
        setContentView(binding.root) // 바인딩된 레이아웃을 액티비티에 설정

        // Intent에서 "username" 값을 가져옴
        val username = intent.getStringExtra("username")
        fetchUserDataFromFirestore(username) // Firestore에서 사용자 데이터를 가져오는 메서드 호출
    }

    // Firestore에서 사용자 데이터를 가져오는 메서드
    private fun fetchUserDataFromFirestore(username: String?) {
        // username이 null인 경우, 오류 메시지를 표시
        if (username == null) {
            binding.textViewLoggedInInfo.text = "No username provided!"
            return
        }

        // Firestore 인스턴스 가져오기
        val db = FirebaseFirestore.getInstance()

        // "users" 컬렉션에서 username에 해당하는 문서를 가져옴
        db.collection("users").document(username).get()
            .addOnSuccessListener { document -> // 성공적으로 데이터를 가져왔을 때의 동작
                if (document.exists()) { // 해당 문서가 존재하면
                    displayUserData(document) // 사용자 데이터를 화면에 표시하는 메서드 호출
                } else { // 해당 문서가 존재하지 않으면
                    binding.textViewLoggedInInfo.text = "No user data found for $username!"
                }
            }
            .addOnFailureListener { // 데이터를 가져오는데 실패했을 때의 동작
                binding.textViewLoggedInInfo.text = "Failed to fetch user data. Please try again later."
            }
    }

    // 사용자 데이터를 화면에 표시하는 메서드
    private fun displayUserData(document: DocumentSnapshot) {
        binding.textViewLoggedInInfo.text = "로그인된 아이디: ${document.getString("username")}"
        binding.textViewName.text = "이름: ${document.getString("real_name")}"
        binding.textViewEmail.text = "이메일: ${document.getString("email")}"
        binding.textViewDOB.text = "생년월일: ${document.getString("dob")}"
    }
}
