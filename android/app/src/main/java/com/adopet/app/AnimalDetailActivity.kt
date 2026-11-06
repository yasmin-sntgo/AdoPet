package com.adopet.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar

class AnimalDetailActivity : BaseActivity() {
    private lateinit var database: DatabaseHelper
    private var animalId=0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        database=DatabaseHelper(this)
        animalId=intent.getLongExtra("animal_id",0)
        findViewById<View>(R.id.btnInterests).visibility = View.GONE
        findViewById<View>(R.id.btnEdit).visibility = View.GONE
        findViewById<View>(R.id.btnDelete).visibility = View.GONE
        findViewById<View>(R.id.btnAdopt).isEnabled = false
        findViewById<View>(R.id.btnEdit).isEnabled = false
        findViewById<View>(R.id.btnDelete).isEnabled = false
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        findViewById<View>(R.id.btnInterests).setOnClickListener {
            runDatabase {
                val entries = database.getInterests(animalId)
                updateScreen {
                    AlertDialog.Builder(this)
                        .setTitle("Interesses registrados neste celular")
                        .setMessage(if (entries.isEmpty()) "Nenhum interesse registrado." else entries.joinToString("\n\n────────────\n\n"))
                        .setPositiveButton("Fechar", null).show()
                }
            }
        }
        findViewById<View>(R.id.btnEdit).setOnClickListener { startActivity(Intent(this,AnimalFormActivity::class.java).putExtra("animal_id",animalId)) }
        findViewById<View>(R.id.btnDelete).setOnClickListener { confirmDelete() }
        findViewById<View>(R.id.btnAdopt).setOnClickListener { startActivity(Intent(this,InterestActivity::class.java).putExtra("animal_id",animalId)) }
    }

    override fun onResume() { super.onResume()
        load() }
    private fun load() {
        runDatabase {
            val animal = database.getAnimal(animalId)
            updateScreen {
                findViewById<android.widget.ImageView>(R.id.petImage).setImageResource(PetImages.resource(animal))
                findViewById<View>(R.id.btnEdit).visibility = if (animal.canManage) View.VISIBLE else View.GONE
                findViewById<View>(R.id.btnDelete).visibility = if (animal.canManage) View.VISIBLE else View.GONE
                findViewById<View>(R.id.btnInterests).visibility = if (animal.canManage) View.VISIBLE else View.GONE
                findViewById<TextView>(R.id.petName).text = animal.name
                findViewById<TextView>(R.id.petStatus).text = if (animal.adopted) "  JÁ ADOTADO  " else "  DISPONÍVEL  "
                findViewById<TextView>(R.id.petSummary).text = "${animal.species} • ${animal.sex} • ${animal.age} anos • Porte ${animal.size.lowercase()}\n📍 ${animal.location}"
                findViewById<TextView>(R.id.petDescription).text = animal.description
                findViewById<TextView>(R.id.petHealth).text = animal.health
                findViewById<TextView>(R.id.petGuardian).text = animal.guardian
                findViewById<View>(R.id.btnAdopt).isEnabled = !animal.adopted
                findViewById<View>(R.id.btnEdit).isEnabled = true
                findViewById<View>(R.id.btnDelete).isEnabled = true
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Excluir animal?")
            .setMessage("Essa ação não poderá ser desfeita.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                runDatabase {
                    database.deleteAnimal(animalId)
                    updateScreen {
                        Toast.makeText(this, "Animal excluído", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .show()
    }
}
