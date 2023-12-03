package com.example.sharedcalendar


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.sharedcalendar.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
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

    // 알림 권한 위한 변수 선언
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1

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

        // 알림 권한을 확인
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // [아직 권한이 허용되지 않은 경우] : 권한 요청 팝업 띄움
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
        } else {
            // [알림 권한이 이미 허용된 경우]
        }

        val cm: ConnectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        //cm.registerNetworkCallback(builder.build(),networkCallBack) // 중간에 인터넷 연결이 끊어지는 것은 일단은 허용시켜 놓았음.

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 알림 권한이 허용된 경우
                Snackbar.make(
                    binding.root,
                    "알림 권한이 허용되었습니다.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                // 알림 권한이 거부된 경우
                Snackbar.make(
                    binding.root,
                    "알림 권한이 거부되었습니다. 알림을 받으려면 설정에서 알림 권한을 허용하세요.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
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

