package com.adopet.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import com.google.android.material.appbar.MaterialToolbar

class FilterActivity : BaseActivity() {
    private lateinit var species: Spinner
    private lateinit var size: Spinner
    private lateinit var sex: Spinner
    private lateinit var available: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        species=findViewById(R.id.spSpecies)
        size=findViewById(R.id.spSize)
        sex=findViewById(R.id.spSex)
        available=findViewById(R.id.checkOnlyAvailable)
        setup(species,listOf("Todos","Cachorro","Gato","Outro"))
        setup(size,listOf("Todos","Pequeno","Médio","Grande"))
        setup(sex,listOf("Todos","Fêmea","Macho"))
        load()
        findViewById<android.view.View>(R.id.btnApply).setOnClickListener { save()
            finish() }
        findViewById<android.view.View>(R.id.btnClear).setOnClickListener { getSharedPreferences("filters",MODE_PRIVATE).edit().clear().apply()
            finish() }
    }
    private fun setup(s:Spinner, values:List<String>) { s.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,values) }
    private fun select(spinner: Spinner, value: String) {
        for (position in 0 until spinner.count) {
            if (spinner.getItemAtPosition(position) == value) {
                spinner.setSelection(position)
                return
            }
        }
        spinner.setSelection(0)
    }
    private fun load() { getSharedPreferences("filters",MODE_PRIVATE).run { select(species,getString("species","Todos")!!)
            select(size,getString("size","Todos")!!)
            select(sex,getString("sex","Todos")!!)
            available.isChecked=getBoolean("available",false) } }
    private fun save() { getSharedPreferences("filters",MODE_PRIVATE).edit().putString("species",species.selectedItem.toString()).putString("size",size.selectedItem.toString()).putString("sex",sex.selectedItem.toString()).putBoolean("available",available.isChecked).apply() }
}
