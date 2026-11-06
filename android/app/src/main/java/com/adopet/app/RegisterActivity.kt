package com.adopet.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        findViewById<Spinner>(R.id.authRole).adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, listOf("Quero adotar", "Sou responsável por animais"))
        findViewById<View>(R.id.authBack).setOnClickListener { finish() }
        findViewById<View>(R.id.authSubmit).setOnClickListener { register() }
    }

    private fun register() {
        val name = findViewById<TextInputEditText>(R.id.authName).text.toString().trim()
        val email = findViewById<TextInputEditText>(R.id.authEmail).text.toString().trim()
        val phone = findViewById<TextInputEditText>(R.id.authPhone).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.authPassword).text.toString()
        val confirm = findViewById<TextInputEditText>(R.id.authConfirm).text.toString()
        if (name.isBlank() || phone.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            || password.length < 8 || password != confirm) {
            Toast.makeText(this, "Confira os campos e confirme uma senha com pelo menos 8 caracteres.", Toast.LENGTH_LONG).show()
            return
        }
        val role = if (findViewById<Spinner>(R.id.authRole).selectedItemPosition == 0) "adotante" else "responsavel"
        val button = findViewById<View>(R.id.authSubmit)
        button.isEnabled = false
        runDatabase {
            try {
                DatabaseHelper(this).register(name, email, phone, password, role)
                updateScreen { finish() }
            } finally {
                updateScreen { button.isEnabled = true }
            }
        }
    }
}
