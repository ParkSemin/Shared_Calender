package com.example.testpc2

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sharedPreferences = getSharedPreferences("calendar", Context.MODE_PRIVATE)

        val searchEditText: EditText = findViewById(R.id.searchEditText)
        val resultsTextView: TextView = findViewById(R.id.resultsTextView)

        // Add TextWatcher for real-time search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val results = searchForSchedule(query)
                resultsTextView.text = results
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this implementation
            }
        })
    }

    private fun searchForSchedule(query: String): String {
        val allEntries = sharedPreferences.all
        val results = StringBuilder()

        for ((key, value) in allEntries) {
            if (value is String && value.contains(query, ignoreCase = true)) {
                results.append("$key: $value\n")
            }
        }

        return if (results.isEmpty()) "No results found." else results.toString()
    }
}