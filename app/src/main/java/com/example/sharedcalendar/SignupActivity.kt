package com.example.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedcalendar.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    // 뷰바인딩 객체를 위한 선언. 레이아웃의 UI 요소에 접근할 수 있습니다.
    private lateinit var binding: ActivitySignupBinding

    // Firebase 인증을 위한 객체 선언
    private lateinit var auth: FirebaseAuth

    // Firestore에 사용자 정보를 저장할 때 사용될 키값들을 정의합니다.
    private val KEY_REAL_NAME = "real_name"
    private val KEY_EMAIL = "email"
    private val KEY_DOB = "dob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 인증 객체 초기화
        auth = FirebaseAuth.getInstance()

        // 뷰바인딩을 이용하여 레이아웃과 연결합니다.
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // '회원가입 완료' 버튼에 클릭 리스너를 설정합니다.
        binding.buttonCompleteSignUp.setOnClickListener {
            // 사용자 입력값을 변수에 저장합니다.
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextSignUpPassword.text.toString()
            val confirmPassword = binding.editTextSignUpConfirmPassword.text.toString()
            val realName = binding.editTextRealName.text.toString()
            val dob = binding.editTextDOB.text.toString()

            // 입력된 비밀번호와 비밀번호 확인이 일치하는지 검사합니다.
            if (password == confirmPassword) {
                // Firebase Authentication을 사용해 사용자 계정을 생성합니다.
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Firestore에 사용자 정보를 저장합니다.
                            val userMap = hashMapOf(
                                KEY_REAL_NAME to realName,
                                KEY_EMAIL to email,
                                KEY_DOB to dob
                            )

                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(email).set(userMap)
                                .addOnSuccessListener {
                                    // 사용자 정보 저장에 성공하면 인증 메일을 전송합니다.
                                    val user = auth.currentUser
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { emailTask ->
                                            if (emailTask.isSuccessful) {
                                                Toast.makeText(this@SignupActivity, "회원가입 성공! 이메일 인증을 해주세요. $email.", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this@SignupActivity, "이메일이 존제하지 않습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@SignupActivity, "회원정보가 저장되지 않았습니다. 다시 시도하세요!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this@SignupActivity, "회원가입의 실패했습니다. 다시 시도하세요!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignupActivity, "비밀번호가 같지 않습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
