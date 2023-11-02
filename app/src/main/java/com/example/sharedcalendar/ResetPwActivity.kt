package com.example.sharedcalendar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class ResetPwActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivityResetPasswordBinding

    // Firebase Authentication 인스턴스 선언
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()

        // 뷰바인딩 초기화 및 레이아웃 설정
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 비밀번호 재설정 버튼 클릭 리스너 설정
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextResetEmail.text.toString()

            if (email.isNotEmpty()) {
                // Firebase를 사용하여 비밀번호 재설정 이메일 전송
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@ResetPwActivity, "비밀번호 재설정 이메일이 전송되었습니다!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ResetPwActivity, "오류 발생: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@ResetPwActivity, "이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
