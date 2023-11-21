package com.example.sharedcalendar

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MyApplication : MultiDexApplication() {
    companion object {
        lateinit var auth: FirebaseAuth
        var email: String? = null
        var email_revised: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
    }
}