package com.example.sharedcalendar


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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.sharedcalendar.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private var database: DatabaseReference = Firebase.database.reference
    private var myRef = database.database.getReference("users")

    // 로그인 버튼 활성화 여부 확인을 위한 boolean 변수 2개
    private var id_ok: Boolean = false
    private var pw_ok: Boolean = false

    // 뒤로가기 버튼을 누르면 앱이 종료되기 위해 버튼을 누른 시간을 저장
    private var backPressedTime: Long = 0
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(applicationContext, "한번 더 누르면 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
            } else {
                finishAffinity()
                System.runFinalization()
                exitProcess(0)
            }
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 뒤로가기 콜백 추가
        this.onBackPressedDispatcher.addCallback(this, callback)

        MyApplication.auth = FirebaseAuth.getInstance()
        binding.btnLogin.isEnabled = false

        // 로그인 버튼 활성화를 위한 리스너
        binding.inputId.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                id_ok = s?.length!! > 6
                changeLoginButtonActivation()
            }
        })
        binding.inputPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_ok = s?.length!! >= 6
                changeLoginButtonActivation()
            }
        })

        // 자동 로그인 구현
        if (!(MySharedPreferences.getUserId(this).isNullOrBlank() && MySharedPreferences.getUserPass(this).isNullOrBlank())) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 리스너
        binding.btnLogin.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val email: String = binding.inputId.text.toString()
            val password: String = binding.inputPw.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (binding.checkAutoLogin.isChecked) {
                            MySharedPreferences.setUserId(this, email)
                            MySharedPreferences.setUserPass(this, password)
                        }
                        MyApplication.email = email
                        MyApplication.email_revised = email.replace(".", "@")

                        myRef.child(MyApplication.email_revised.toString()).get().addOnSuccessListener {
                            val dataMap = it.value as Map<String, String>
                            val name = dataMap["name"]
                            Log.d("SEMIN_name", "$name")
                            MyApplication.name = name

                            var intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        MotionToast.darkColorToast(
                            this,
                            "로그인 실패",
                            "아이디 또는 비밀번호를 확인하세요",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                        )
                    }
                }
        }

        // 아이디 찾기 리스너
        binding.btnFindId.setOnClickListener {
            Toast.makeText(baseContext, "아이디 찾기 클릭됨", Toast.LENGTH_SHORT).show()
        }
        // 비밀번호 찾기 리스너
        binding.btnFindPw.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
        // 회원가입 리스너
        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
    // 로그인 버튼 활성화 상태 변경 메소드
    fun changeLoginButtonActivation() : Unit {
        binding.btnLogin.isEnabled = id_ok == true && pw_ok == true
    }
}
