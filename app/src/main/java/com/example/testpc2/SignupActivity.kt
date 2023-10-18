package com.example.testpc2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private val PREFS_NAME = "login_data"
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"
    private val KEY_REAL_NAME = "real_name"
    private val KEY_EMAIL = "email"
    private val KEY_DOB = "dob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val editTextUsername: EditText = findViewById(R.id.editTextSignUpUsername)
        val editTextPassword: EditText = findViewById(R.id.editTextSignUpPassword)
        val editTextConfirmPassword: EditText = findViewById(R.id.editTextSignUpConfirmPassword)
        val editTextRealName: EditText = findViewById(R.id.editTextRealName)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextDOB: EditText = findViewById(R.id.editTextDOB)
        val buttonCompleteSignUp: Button = findViewById(R.id.buttonCompleteSignUp)

        buttonCompleteSignUp.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            val realName = editTextRealName.text.toString()
            val email = editTextEmail.text.toString()
            val dob = editTextDOB.text.toString()

            if (password == confirmPassword) {
                // SharedPreferences에 회원정보 저장
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString(KEY_USERNAME, username)
                editor.putString(KEY_PASSWORD, password)
                editor.putString(KEY_REAL_NAME, realName)
                editor.putString(KEY_EMAIL, email)
                editor.putString(KEY_DOB, dob)
                editor.apply()

                Toast.makeText(this@SignupActivity, "Signed up successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignupActivity, Login::class.java)
                finish()
            } else {
                Toast.makeText(this@SignupActivity, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


