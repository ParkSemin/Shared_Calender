package com.example.sharedcalendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // 초기 이메일 인증, 가입하기 버튼 비활성화
        binding.btnVerify.isEnabled = false
        binding.btnSignUp.isEnabled = false

        // 모든 필드 입력 되면 이메일 인증 버튼 활성화
        // 1. 이름 필드
        binding.inputName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                name_ok = s?.length!! >= 2
                changeVerifyButtonActivation()
            }
        })
        // 2. 아이디 필드
        binding.inputId.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                id_ok = s?.length!! > 6 && '@' in s
                changeVerifyButtonActivation()
            }
        })
        // 3. 비밀번호 필드
        binding.inputPw.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_ok = s?.length!! >= 6 && checkPassword() // 6자 이상 + 비밀번호 입력 정상
                changeVerifyButtonActivation()
            }
        })
        // 4. 비밀번호 확인 필드
        binding.inputPwCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_ok = s?.length!! >= 6 && checkPassword() // 6자 이상 + 비밀번호 입력 정상
                changeVerifyButtonActivation()
            }
        })

        // 이메일 인증 리스너
        binding.btnVerify.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val email = binding.inputId.text.toString()
            val passwd = binding.inputPw.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MyApplication.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { sendTask ->
                        if (sendTask.isSuccessful) {
                            MotionToast.darkColorToast(
                                this,
                                "이메일 전송 완료",
                                "입력한 이메일을 확인하세요",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                            )
                            binding.btnSignUp.isEnabled = true
                            binding.btnVerify.isEnabled = false
                        } else {
                            MotionToast.darkColorToast(
                                this,
                                "이메일 전송 실패",
                                "입력한 이메일을 확인하세요",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                            )
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

        // 가입하기 버튼 리스너
        binding.btnSignUp.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val name: String = binding.inputName.text.toString()
            val email: String = binding.inputId.text.toString()
            val password: String = binding.inputPw.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (MyApplication.checkAuth()) {
                        MotionToast.darkColorToast(
                            this,
                            "회원 가입 완료",
                            "가입한 아이디로 로그인을 진행해주세요",
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

                        // 로그인 액티비티로 다시 돌아감
                        var intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        MotionToast.darkColorToast(
                            this,
                            "회원 가입 실패",
                            "이메일 인증이 완료되지 않았습니다",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(
                                this,
                                www.sanju.motiontoast.R.font.helvetica_regular
                            )
                        )
                    }
                }
            }
        }
    }

    // 이메일 인증 버튼 활성화 상태 변경 메소드
    fun changeVerifyButtonActivation() : Unit {
        binding.btnVerify.isEnabled = name_ok == true && id_ok == true && pw_ok == true
        Log.d("btnVerify", "${binding.btnVerify.isEnabled}, $name_ok, $id_ok, $pw_ok")
    }

    // 비밀번호 == 비밀번호 확인이면 true, 아니면 false
    fun checkPassword() : Boolean {
        return binding.inputPw.text.toString() == binding.inputPwCheck.text.toString()
    }
}


