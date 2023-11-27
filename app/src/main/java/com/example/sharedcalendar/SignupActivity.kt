package com.example.sharedcalendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.sharedcalendar.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SignupActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }

    private var database: DatabaseReference = Firebase.database.reference
    private val myRef = database.database.getReference("users")

    // 이메일 인증 버튼 활성화 여부 확인을 위한 boolean 변수
    private var name_ok: Boolean = false
    private var id_ok: Boolean = false
    private var pw_ok: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 초기 가입하기 버튼 비활성화
        binding.btnSignUp.isEnabled = false

        // 모든 필드 입력 되면 가입하기 버튼 활성화
        // 1. 이름 필드
        binding.inputName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                name_ok = s?.length!! >= 2
                if (name_ok) {
                    binding.nameokText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
                    binding.nameokText.text = "유효한 이름"
                } else {
                    binding.nameokText.setTextColor(Color.RED)
                    binding.nameokText.text = "이름은 최소 2자 이상이어야 합니다"
                }
                changeSignUpButtonActivation()
            }
        })
        // 2. 아이디 필드
        binding.inputId.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                id_ok = s?.length!! > 6 && '@' in s
                if (id_ok) {
                    binding.idokText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
                    binding.idokText.text = "유효한 아이디"
                } else {
                    binding.idokText.setTextColor(Color.RED)
                    binding.idokText.text = "이메일은 6자 이상이고 '@'를 포함해야 합니다"
                }
                changeSignUpButtonActivation()
            }
        })
        // 3. 비밀번호 필드
        binding.inputPw.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_ok = s?.length!! >= 6 && checkPassword() // 6자 이상 + 비밀번호 입력 정상
                if (s?.length!! >= 6) {
                    binding.pwokText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
                    binding.pwokText.text = "유효한 비밀번호"
                } else {
                    binding.pwokText.setTextColor(Color.RED)
                    binding.pwokText.text = "비밀번호는 6자 이상이어야 합니다"
                }
                changeSignUpButtonActivation()
            }
        })
        // 4. 비밀번호 확인 필드
        binding.inputPwCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_ok = s?.length!! >= 6 && checkPassword() // 6자 이상 + 비밀번호 입력 정상
                if (pw_ok) {
                    binding.pwcheckokText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
                    binding.pwcheckokText.text = "유효한 비밀번호 확인"
                } else {
                    binding.pwcheckokText.setTextColor(Color.RED)
                    binding.pwcheckokText.text = "비밀번호가 위와 같아야 합니다"
                }
                changeSignUpButtonActivation()
            }
        })

        // 가입하기 버튼 리스너
        binding.btnSignUp.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val name: String = binding.inputName.text.toString()
            val email: String = binding.inputId.text.toString()
            val password: String = binding.inputPw.text.toString()
            MyApplication.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MotionToast.darkColorToast(
                        this,
                        "회원 가입 완료",
                        "회원 가입이 성공적으로 진행되었습니다",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(
                            this,
                            www.sanju.motiontoast.R.font.helvetica_regular
                        )
                    )
                    // RealtimeDatabase에 회원 정보 추가
                    myRef.child(email.replace(".", "@")).child("name").setValue(name)

                    // *feedback* 로그인 화면이 아닌 바로 메인 화면으로 이동하게 수정
                    MyApplication.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // 기본으로 자동 로그인 설정함
                                MySharedPreferences.setUserId(this, email)
                                MySharedPreferences.setUserPass(this, password)

                                MyApplication.email = email
                                MyApplication.email_revised = email.replace(".", "@")

                                myRef.child(MyApplication.email_revised.toString()).get()
                                    .addOnSuccessListener {
                                        val dataMap = it.value as Map<String, String>
                                        val name = dataMap["name"]
                                        MySharedPreferences.setUserName(this, name)

                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                            }
                        }
                } else {
                    MotionToast.darkColorToast(
                        this,
                        "회원 가입 실패",
                        "이미 존재하는 이메일입니다",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                    )
                }
            }
        }
    }

    // 이메일 인증 버튼 활성화 상태 변경 메소드
    fun changeSignUpButtonActivation() : Unit {
        binding.btnSignUp.isEnabled = name_ok == true && id_ok == true && pw_ok == true
    }

    // 비밀번호 == 비밀번호 확인이면 true, 아니면 false
    fun checkPassword() : Boolean {
        return binding.inputPw.text.toString() == binding.inputPwCheck.text.toString()
    }
}


