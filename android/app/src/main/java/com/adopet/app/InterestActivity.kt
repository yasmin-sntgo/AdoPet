package com.adopet.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class InterestActivity : BaseActivity() {
    private lateinit var database:DatabaseHelper
    private var animalId=0L
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)
        database=DatabaseHelper(this)
        animalId=intent.getLongExtra("animal_id",0)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        runDatabase {
            val animal = database.getAnimal(animalId)
            updateScreen { findViewById<TextView>(R.id.titlePet).text = "Quero adotar ${animal.name}" }
        }
        findViewById<Spinner>(R.id.spHousing).adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Selecione o tipo de moradia *","Casa","Apartamento","Chácara / sítio","Outro"))
        findViewById<android.view.View>(R.id.btnSubmit).setOnClickListener { submit() }
    }

    private fun submit() {
        val name=findViewById<TextInputEditText>(R.id.inputName).text.toString().trim()
        val phone=findViewById<TextInputEditText>(R.id.inputPhone).text.toString().trim()
        val reason=findViewById<TextInputEditText>(R.id.inputReason).text.toString().trim()
        val housing=findViewById<Spinner>(R.id.spHousing)
        if(name.isBlank() || phone.isBlank() || reason.isBlank() || housing.selectedItemPosition==0) { Toast.makeText(this,"Preencha todos os campos obrigatórios (*)",Toast.LENGTH_LONG).show()
            return }
        val email = findViewById<TextInputEditText>(R.id.inputEmail).text.toString().trim()
        val consent = findViewById<CheckBox>(R.id.checkConsent).isChecked
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || !consent) {
            Toast.makeText(this, "Informe um e-mail válido e confirme a responsabilidade.", Toast.LENGTH_LONG).show()
            return
        }
        val housingText = housing.selectedItem.toString()
        val screens = findViewById<CheckBox>(R.id.checkScreens).isChecked
        val experience = findViewById<CheckBox>(R.id.checkExperience).isChecked
        val submitButton = findViewById<android.view.View>(R.id.btnSubmit)
        submitButton.isEnabled = false
        runDatabase {
            try {
                database.insertInterest(animalId, name, phone, housingText, screens, experience, reason, email, consent)
                updateScreen {
                    AlertDialog.Builder(this)
                        .setTitle("Interesse registrado!")
                        .setMessage("Os dados foram salvos neste celular. Consulte em Interesses registrados no perfil do animal. Nenhuma mensagem foi enviada à ONG.")
                        .setCancelable(false)
                        .setPositiveButton("Concluir") { _, _ -> finish() }
                        .show()
                }
            } finally {
                updateScreen { submitButton.isEnabled = true }
            }
        }
    }
}
