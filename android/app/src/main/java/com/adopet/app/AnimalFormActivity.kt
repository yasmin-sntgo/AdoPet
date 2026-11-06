package com.adopet.app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.google.android.material.textfield.TextInputLayout
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AnimalFormActivity : BaseActivity() {
    private lateinit var database: DatabaseHelper
    private var animalId = 0L
    private lateinit var name: TextInputEditText
    private lateinit var age: TextInputEditText
    private lateinit var location: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var health: TextInputEditText
    private lateinit var guardian: TextInputEditText
    private lateinit var otherSpecies: TextInputEditText
    private lateinit var otherSpeciesLayout: TextInputLayout
    private lateinit var species: Spinner
    private lateinit var size: Spinner
    private lateinit var sex: Spinner
    private lateinit var adopted: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        database=DatabaseHelper(this)
        if (!database.isResponsible()) { finish(); return }
        animalId=intent.getLongExtra("animal_id",0)
        findViewById<MaterialToolbar>(R.id.toolbar).apply { setNavigationOnClickListener { finish() }
            title=if(animalId==0L) "Cadastrar animal" else "Editar animal" }
        name=findViewById(R.id.inputName)
        age=findViewById(R.id.inputAge)
        location=findViewById(R.id.inputLocation)
        description=findViewById(R.id.inputDescription)
        health=findViewById(R.id.inputHealth)
        guardian=findViewById(R.id.inputGuardian)
        guardian.setText(database.guardianName())
        guardian.isEnabled = false
        species=findViewById(R.id.spSpecies)
        size=findViewById(R.id.spSize)
        sex=findViewById(R.id.spSex)
        adopted=findViewById(R.id.checkAdopted)
        setupSpinner(species, listOf("Cachorro","Gato","Outro"))
        setupSpinner(size,listOf("Pequeno","Médio","Grande"))
        setupSpinner(sex,listOf("Fêmea","Macho"))
        otherSpecies=findViewById(R.id.inputOtherSpecies)
        otherSpeciesLayout=findViewById(R.id.layoutOtherSpecies)
        species.onItemSelectedListener=object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { updateSpeciesField() }
            override fun onNothingSelected(parent: AdapterView<*>?) { updateSpeciesField() }
        }
        if(animalId!=0L) fillForm()
        updateSpeciesField()
        findViewById<android.view.View>(R.id.btnSave).setOnClickListener { save() }
    }


    private fun setupSpinner(spinner: Spinner, values: List<String>) { spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,values) }
    private fun select(spinner: Spinner, value: String) {
        for (position in 0 until spinner.count) {
            if (spinner.getItemAtPosition(position) == value) {
                spinner.setSelection(position)
                return
            }
        }
        spinner.setSelection(0)
    }
    // Na edição, recupera os dados que já estão salvos neste celular.
    private fun fillForm() {
        findViewById<View>(R.id.btnSave).isEnabled = false
        runDatabase {
            val animal = database.getAnimal(animalId)
            updateScreen {
        
                name.setText(animal.name)
                age.setText(animal.age.toString())
                location.setText(animal.location)
                description.setText(animal.description)
                health.setText(animal.health)
                guardian.setText(animal.guardian)
        
                if (animal.species == "Cachorro" || animal.species == "Gato") {
                    select(species, animal.species)
                } else {
                    select(species, "Outro")
                    if (animal.species != "Outro") {
                        otherSpecies.setText(animal.species)
                    }
                }
        
                select(size, animal.size)
                select(sex, animal.sex)
                adopted.isChecked = animal.adopted
                updateSpeciesField()
                findViewById<View>(R.id.btnSave).isEnabled = true
            }
        }
    }

    private fun updateSpeciesField() {
        otherSpeciesLayout.visibility=if(species.selectedItem=="Outro") View.VISIBLE else View.GONE
        if(species.selectedItem!="Outro") otherSpecies.error=null
    }

    private fun save() {
        val missingField = name.text.isNullOrBlank() || age.text.isNullOrBlank() ||
            location.text.isNullOrBlank() || description.text.isNullOrBlank() ||
            guardian.text.isNullOrBlank()
        if(missingField) { Toast.makeText(this,"Preencha todos os campos obrigatórios (*)",Toast.LENGTH_LONG).show()
            return }
        val parsedAge=age.text.toString().toIntOrNull()
        if(parsedAge == null || parsedAge !in 0..50) { age.error="Informe uma idade entre 0 e 50 anos"
            return }
        val selectedSpecies=if(species.selectedItem=="Outro") otherSpecies.text.toString().trim() else species.selectedItem.toString()
        if(selectedSpecies.isBlank() || selectedSpecies.equals("Outro",true) || selectedSpecies.equals("Todos",true)) {
            otherSpecies.error="Informe o tipo do animal, por exemplo: Coelho"
            otherSpecies.requestFocus()
            return
        }
        var healthText = health.text.toString().trim()
        if (healthText.isBlank()) {
            healthText = "Informações não cadastradas."
        }
        val animal = Animal(
            id = animalId,
            name = name.text.toString().trim(),
            species = selectedSpecies,
            age = parsedAge,
            size = size.selectedItem.toString(),
            sex = sex.selectedItem.toString(),
            location = location.text.toString().trim(),
            description = description.text.toString().trim(),
            health = healthText,
            guardian = guardian.text.toString().trim(),
            adopted = adopted.isChecked
        )
        findViewById<View>(R.id.btnSave).isEnabled = false
        runDatabase {
            try {
                if (animalId == 0L) database.insertAnimal(animal) else database.updateAnimal(animal)
                updateScreen {
                    Toast.makeText(this, "Animal salvo neste celular!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } finally {
                updateScreen { findViewById<View>(R.id.btnSave).isEnabled = true }
            }
        }
    }
}
