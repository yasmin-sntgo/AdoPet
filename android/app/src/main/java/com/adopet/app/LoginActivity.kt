package com.adopet.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : BaseActivity() {
    private lateinit var database: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        database = DatabaseHelper(this)
        findViewById<View>(R.id.authBack).setOnClickListener { finish() }
        findViewById<View>(R.id.authRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        findViewById<View>(R.id.authSubmit).setOnClickListener { login() }
    }

    override fun onResume() {
        super.onResume()
        if (::database.isInitialized && database.isLoggedIn()) finish()
    }

    private fun login() {
        val email = findViewById<TextInputEditText>(R.id.authEmail).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.authPassword).text.toString()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_LONG).show()
            return
        }
        val button = findViewById<View>(R.id.authSubmit)
        button.isEnabled = false
        runDatabase {
            try {
                database.login(email, password)
                updateScreen { finish() }
            } finally {
                updateScreen { button.isEnabled = true }
            }
        }
    }
}
