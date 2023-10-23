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
                        // 로그인 성공 후 이메일 인증 상태 확인
                        val user = auth.currentUser
                        if (user?.isEmailVerified == true) {
                            // 이메일 인증 성공 시
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("username", email)
                            startActivity(intent)
                        } else {
                            // 이메일 인증 미완료
                            Toast.makeText(this@LoginActivity, "이메일 인증을 해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 로그인 실패
                        Toast.makeText(this@LoginActivity, "비밀번호나 이메일이 맞지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.buttonSignUp.setOnClickListener {
            // 회원 가입 페이지로 이동
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.buttonFindPassword.setOnClickListener {
            // 비밀번호 재설정 페이지로 이동
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}