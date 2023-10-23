package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivityLoginBinding

    // Firebase Authentication 인스턴스 선언
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()

        // 뷰바인딩 초기화 및 레이아웃 설정
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            // 입력 필드에서 이메일과 비밀번호 가져오기
            val email = binding.editTextUsername.text.toString() // Note: Assuming editTextUsername now holds email.
            val password = binding.editTextPassword.text.toString()

            // Firebase Authentication을 사용하여 로그인
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", email) // Sending email as "username" for consistency.
                        startActivity(intent)
                    } else {
                        // 로그인 실패
                        Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.buttonSignUp.setOnClickListener {
            // 회원 가입 페이지로 이동
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}