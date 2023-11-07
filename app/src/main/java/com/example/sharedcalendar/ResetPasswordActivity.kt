package com.example.sharedcalendar

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.sharedcalendar.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class ResetPasswordActivity : AppCompatActivity() {
    private val binding by lazy { ActivityResetPasswordBinding.inflate(layoutInflater) }

    private var id_ok: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 비밀 번호 초기화 버튼 초기 비활성화
        binding.buttonResetPassword.isEnabled = false

        //이메일 필드
        binding.editTextResetEmail.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                id_ok = s?.length!! > 6 && '@' in s
                changeVerifyButtonActivation()
            }
        })

        // 비밀번호 재설정 버튼 클릭 리스너 설정
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextResetEmail.text.toString()

            if (email.isNotEmpty()) {
                // Firebase를 사용하여 비밀번호 재설정 이메일 전송
                MyApplication.auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            MotionToast.darkColorToast(
                                this,
                                "메일 전송 완료",
                                "회원님의 이메일로 비밀번호 재설정 메일이 보내졌습니다.",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(
                                    this,
                                    www.sanju.motiontoast.R.font.helvetica_regular
                                )
                            )
                            finish()
                        } else {
                            MotionToast.darkColorToast(
                                this,
                                "메일 전송 실패",
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
                    "메일 전송 실패",
                    "올바른 이메일 형식을 입력해주세요.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }
    }
    // 이메일 인증 버튼 활성화 상태 변경 메소드
    fun changeVerifyButtonActivation() : Unit {
        binding.buttonResetPassword.isEnabled = id_ok == true
    }
}