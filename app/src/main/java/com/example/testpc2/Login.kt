package com.example.testpc2


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    private val PREFS_NAME = "login_data"
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername: EditText = findViewById(R.id.editTextUsername)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val buttonSignUp: Button = findViewById(R.id.buttonSignUp)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedUsername = prefs.getString(KEY_USERNAME, null)
            val savedPassword = prefs.getString(KEY_PASSWORD, null)

            if (username == savedUsername && password == savedPassword) {
                // 로그인 성공
                val intent = Intent(this@Login, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
            } else {
                // 로그인 실패
                Toast.makeText(this@Login, "Login failed!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSignUp.setOnClickListener {
            val intent = Intent(this@Login, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
