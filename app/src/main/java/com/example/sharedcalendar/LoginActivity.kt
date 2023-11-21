package com.example.sharedcalendar


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
    private var idOk: Boolean = false
    private var pwOk: Boolean = false

    // 뒤로가기 버튼을 누르면 앱이 종료되기 위해 버튼을 누른 시간을 저장
    private var backPressedTime: Long = 0
    private val callback = object : OnBackPressedCallback(true) {
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

    // 네트워크 상태 확인 메소드
    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        // 중간에 인터넷 연결이 끊어지는 경우
        override fun onLost(network: Network) {
            // 네트워크가 끊길 때 호출됩
            Toast.makeText(applicationContext, "인터넷 연결이 필요합니다.", Toast.LENGTH_LONG).show()
            finishAffinity()
            System.runFinalization()
            exitProcess(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val cm: ConnectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        cm.registerNetworkCallback(builder.build(),networkCallBack)

        // 인터넷 연결 상태 확인
        if (!isNetworkAvailable()) {
            // 인터넷 연결이 되지 않았을 때 종료
            Toast.makeText(this, "인터넷 연결이 필요합니다.", Toast.LENGTH_LONG).show()
            finishAffinity()
            System.runFinalization()
            exitProcess(0)
        }

        // 뒤로가기 콜백 추가
        this.onBackPressedDispatcher.addCallback(this, callback)

        MyApplication.auth = FirebaseAuth.getInstance()
        binding.btnLogin.isEnabled = false

        // 자동 로그인 구현
        if (!(MySharedPreferences.getUserId(this).isBlank() && MySharedPreferences.getUserPass(this).isBlank())) {
            val email = MySharedPreferences.getUserId(this)
            val password = MySharedPreferences.getUserPass(this)

            MyApplication.email = email
            MyApplication.email_revised = email.replace(".", "@")

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 활성화를 위한 리스너
        binding.inputId.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                idOk = s?.length!! > 6
                changeLoginButtonActivation()
            }
        })
        binding.inputPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pwOk = s?.length!! >= 6
                changeLoginButtonActivation()
            }
        })

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
                            MySharedPreferences.setUserName(this, name)

                            val intent = Intent(this, MainActivity::class.java)
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
        binding.btnLogin.isEnabled = idOk == true && pwOk == true
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}
