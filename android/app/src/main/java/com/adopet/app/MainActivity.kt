package com.adopet.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : BaseActivity() {
    private lateinit var database: DatabaseHelper
    private lateinit var list: GridLayout
    private lateinit var count: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = DatabaseHelper(this)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.menu.add("Atualizar")
        toolbar.menu.add("Sobre as imagens")
        toolbar.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Atualizar" -> showAnimals()
                else -> AlertDialog.Builder(this).setTitle("Imagens de demonstração")
                    .setMessage("Fotografias ilustrativas dos exemplos. Cachorro: Herwig Kavallar, domínio público, Wikimedia Commons, File:Labrador_Retriever_portrait.jpg. Gato: Alvesgaspar, CC BY-SA 3.0. Fonte: https://commons.wikimedia.org/wiki/File:Cat_March_2010-1.jpg Licença: https://creativecommons.org/licenses/by-sa/3.0/")
                    .setPositiveButton("Fechar", null).show()
            }
            true
        }
        list = findViewById(R.id.petList)
        count = findViewById(R.id.titleCount)
        findViewById<View>(R.id.btnAccount).setOnClickListener {
            if (database.isLoggedIn()) {
                runDatabase { try { database.logout() } finally { updateScreen { updateAccount(); showAnimals() } } }
            } else startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<View>(R.id.fabAdd).setOnClickListener { startActivity(Intent(this, AnimalFormActivity::class.java)) }
        findViewById<View>(R.id.btnFilter).setOnClickListener { startActivity(Intent(this, FilterActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        updateAccount()
        showAnimals()
    }

    private fun updateAccount() {
        findViewById<android.widget.Button>(R.id.btnAccount).text = if (database.isLoggedIn()) "Sair da conta" else "Entrar / Criar conta"
        findViewById<View>(R.id.fabAdd).visibility = if (database.isResponsible()) View.VISIBLE else View.GONE
    }

    private fun showAnimals() {
        val prefs = getSharedPreferences("filters", MODE_PRIVATE)
        count.text = "Carregando os animais..."
        runDatabase {
            database.refreshSession()
            val animals = database.getAnimals(
                prefs.getString("species", "Todos") ?: "Todos",
                prefs.getString("size", "Todos") ?: "Todos",
                prefs.getString("sex", "Todos") ?: "Todos",
                prefs.getBoolean("available", false)
            )
            updateScreen { updateAccount(); displayAnimals(animals) }
        }
    }

    private fun displayAnimals(animals: List<Animal>) {
        list.removeAllViews()
        count.text = if (animals.size == 1) "1 pet encontrado" else "${animals.size} pets encontrados"
        if (animals.isEmpty()) {
            TextView(this).apply {
                text = "Nenhum animal encontrado.\nTente alterar os filtros."
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(getColor(R.color.muted))
                setPadding(16, 80, 16, 80)
                layoutParams = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(0, 2, 1f)
                    width = 0
                }
                list.addView(this)
            }
            return
        }
        animals.forEach { list.addView(createCard(it)) }
    }

    // Mesma organização do primeiro protótipo: dois cartões por linha.
    private fun createCard(animal: Animal): View {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(8), dp(8), dp(8), dp(8))
        card.setBackgroundColor(Color.rgb(245, 249, 246))
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        params.setMargins(dp(4), dp(4), dp(4), dp(4))
        card.layoutParams = params

        // O projeto original usava este ícone como espaço para a foto.
        val image = ImageView(this)
        image.setImageResource(PetImages.resource(animal))
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        image.contentDescription = "Imagem ilustrativa de ${animal.species}"
        card.addView(image, LinearLayout.LayoutParams(-1, dp(120)))
        val imageCaption = TextView(this)
        imageCaption.text = "Imagem ilustrativa"
        imageCaption.textSize = 11f
        imageCaption.setTextColor(getColor(R.color.muted))
        card.addView(imageCaption)

        val name = TextView(this)
        name.text = animal.name
        name.textSize = 18f
        name.setTextColor(getColor(R.color.ink))
        name.setTypeface(name.typeface, 1)
        name.setPadding(0, dp(8), 0, 0)
        card.addView(name)

        val summary = TextView(this)
        val years = if (animal.age == 1) "ano" else "anos"
        summary.text = "${animal.species} • ${animal.age} $years\nPorte ${animal.size.lowercase()}"
        summary.textSize = 13f
        summary.setTextColor(getColor(R.color.muted))
        card.addView(summary)

        val status = TextView(this)
        status.text = if (animal.adopted) "Já adotado" else "Disponível"
        status.textSize = 13f
        status.setTextColor(getColor(R.color.green_primary))
        card.addView(status)

        val details = com.google.android.material.button.MaterialButton(this)
        details.text = "Ver detalhes"
        details.isAllCaps = false
        details.cornerRadius = dp(10)
        details.setOnClickListener {
            startActivity(Intent(this, AnimalDetailActivity::class.java).putExtra("animal_id", animal.id))
        }
        card.addView(details, LinearLayout.LayoutParams(-1, -2))
        return card
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
