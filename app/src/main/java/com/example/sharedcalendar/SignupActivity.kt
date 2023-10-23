package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    // 뷰바인딩 객체 선언
    private lateinit var binding: ActivitySignupBinding

    // Firebase Authentication 인스턴스 선언
    private lateinit var auth: FirebaseAuth

    // 사용자 정보를 저장하기 위한 키값 정의
    private val KEY_REAL_NAME = "real_name"
    private val KEY_EMAIL = "email"
    private val KEY_DOB = "dob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()

        // 뷰바인딩 초기화 및 레이아웃 설정
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // '회원가입 완료' 버튼 클릭 리스너 설정
        binding.buttonCompleteSignUp.setOnClickListener {
            // 입력 필드에서 데이터 가져오기
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextSignUpPassword.text.toString()
            val confirmPassword = binding.editTextSignUpConfirmPassword.text.toString()
            val realName = binding.editTextRealName.text.toString()
            val dob = binding.editTextDOB.text.toString()

            // 비밀번호와 비밀번호 확인이 일치하는지 검사
            if (password == confirmPassword) {
                // Firebase Authentication을 사용하여 사용자 등록
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Firestore에 추가적인 사용자 정보 저장
                            val userMap = hashMapOf(
                                KEY_REAL_NAME to realName,
                                KEY_EMAIL to email,
                                KEY_DOB to dob
                            )

                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(email).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this@SignupActivity, "Signed up successfully!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@SignupActivity, "Failed to save user info. Try again!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this@SignupActivity, "Failed to sign up. Try again!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignupActivity, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}