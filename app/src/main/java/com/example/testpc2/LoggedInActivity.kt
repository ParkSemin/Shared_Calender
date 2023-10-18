package com.example.testpc2

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoggedInActivity : AppCompatActivity() {

    private val PREFS_NAME = "login_data"
    private val KEY_USERNAME = "username"
    private val KEY_REAL_NAME = "real_name"
    private val KEY_EMAIL = "email"
    private val KEY_DOB = "dob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_in)

        val textViewLoggedInInfo: TextView = findViewById(R.id.textViewLoggedInInfo)
        val textViewName: TextView = findViewById(R.id.textViewName)
        val textViewEmail: TextView = findViewById(R.id.textViewEmail)
        val textViewDOB: TextView = findViewById(R.id.textViewDOB)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null)
        val realName = prefs.getString(KEY_REAL_NAME, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val dob = prefs.getString(KEY_DOB, null)

        textViewLoggedInInfo.text = "Logged in as: $username"
        textViewName.text = "Name: $realName"
        textViewEmail.text = "Email: $email"
        textViewDOB.text = "Date of Birth: $dob"
    }
}
