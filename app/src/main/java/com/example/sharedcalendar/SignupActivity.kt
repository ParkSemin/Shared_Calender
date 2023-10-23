package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivitySignupBinding
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivitySignupBinding

    // 사용자 정보를 저장하기 위한 키값 정의
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"
    private val KEY_REAL_NAME = "real_name"
    private val KEY_EMAIL = "email"
    private val KEY_DOB = "dob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩 초기화 및 레이아웃 설정
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // '회원가입 완료' 버튼 클릭 리스너 설정
        binding.buttonCompleteSignUp.setOnClickListener {
            // 입력 필드에서 데이터 가져오기
            val username = binding.editTextSignUpUsername.text.toString()
            val password = binding.editTextSignUpPassword.text.toString()
            val confirmPassword = binding.editTextSignUpConfirmPassword.text.toString()
            val realName = binding.editTextRealName.text.toString()
            val email = binding.editTextEmail.text.toString()
            val dob = binding.editTextDOB.text.toString()

            // 비밀번호와 비밀번호 확인이 일치하는지 검사
            if (password == confirmPassword) {
                // 사용자 정보를 맵 형태로 저장
                val userMap = hashMapOf(
                    KEY_USERNAME to username,
                    KEY_PASSWORD to password,
                    KEY_REAL_NAME to realName,
                    KEY_EMAIL to email,
                    KEY_DOB to dob
                )

                // Firestore 데이터베이스 인스턴스를 가져오기
                val db = FirebaseFirestore.getInstance()
                // "users" 컬렉션에 사용자 정보 저장
                db.collection("users").document(username).set(userMap)
                    .addOnSuccessListener {
                        // 성공적으로 회원가입이 완료된 경우 토스트 메시지 표시
                        Toast.makeText(
                            this@SignupActivity,
                            "Signed up successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 로그인 화면으로 이동
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        // 회원가입 중 오류 발생시 토스트 메시지 표시
                        Toast.makeText(
                            this@SignupActivity,
                            "Failed to sign up. Try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                // 비밀번호와 비밀번호 확인이 일치하지 않는 경우 토스트 메시지 표시
                Toast.makeText(this@SignupActivity, "Passwords do not match!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
