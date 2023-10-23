package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivityLoginBinding

    // 사용자 로그인 정보를 저장하기 위한 키값 정의
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩 초기화 및 레이아웃 설정
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            // 입력 필드에서 사용자 이름과 비밀번호 가져오기
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()

            // Firestore 데이터베이스 인스턴스를 가져오기
            val db = FirebaseFirestore.getInstance()

            // "users" 컬렉션에서 사용자 정보 조회
            db.collection("users").document(username).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val savedPassword = document.getString(KEY_PASSWORD)
                        // 저장된 비밀번호와 입력된 비밀번호가 일치하는지 확인
                        if (password == savedPassword) {
                            // 로그인 성공
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                        } else {
                            // 비밀번호가 일치하지 않음
                            Toast.makeText(this@LoginActivity, "Password incorrect!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 사용자를 찾을 수 없음
                        Toast.makeText(this@LoginActivity, "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.buttonSignUp.setOnClickListener {
            // 회원 가입 페이지로 이동
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}