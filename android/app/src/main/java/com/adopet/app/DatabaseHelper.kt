package com.adopet.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(private val context: Context, databaseName: String = "adopet.db") : SQLiteOpenHelper(context, databaseName, null, 3) {
    override fun onConfigure(db: SQLiteDatabase) { super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true) }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE animals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL, species TEXT NOT NULL, age INTEGER NOT NULL,
                size TEXT NOT NULL, sex TEXT NOT NULL, location TEXT NOT NULL,
                description TEXT NOT NULL, health TEXT NOT NULL,
                guardian TEXT NOT NULL, adopted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE interests (
                id INTEGER PRIMARY KEY AUTOINCREMENT, animal_id INTEGER NOT NULL REFERENCES animals(id) ON DELETE RESTRICT,
                adopter_name TEXT NOT NULL, phone TEXT NOT NULL, housing TEXT NOT NULL,
                has_screens INTEGER NOT NULL, has_experience INTEGER NOT NULL,
                reason TEXT NOT NULL, created_at TEXT NOT NULL
            )
        """.trimIndent())
        seed(db)
        addAccounts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Preserva os dados da versão anterior, incluindo interesses já enviados.
            db.execSQL("ALTER TABLE interests RENAME TO interests_v1")
            db.execSQL("""CREATE TABLE interests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                animal_id INTEGER NOT NULL REFERENCES animals(id) ON DELETE RESTRICT,
                adopter_name TEXT NOT NULL, phone TEXT NOT NULL, housing TEXT NOT NULL,
                has_screens INTEGER NOT NULL, has_experience INTEGER NOT NULL,
                reason TEXT NOT NULL, created_at TEXT NOT NULL)""")
            db.execSQL("""INSERT INTO interests SELECT i.* FROM interests_v1 i
                WHERE EXISTS (SELECT 1 FROM animals a WHERE a.id=i.animal_id)""")
            db.execSQL("DROP TABLE interests_v1")
        }
        if (oldVersion < 3) addAccounts(db)
    }

    private fun seed(db: SQLiteDatabase) {
        insertAnimal(db, Animal(name="Luna", species="Cachorro", age=2, size="Médio", sex="Fêmea", location="São Paulo - SP", description="Luna foi resgatada perto de uma escola. É carinhosa, brincalhona e se dá muito bem com crianças e outros cães.", health="Vacinada, castrada e vermifugada.", guardian="Instituto Patinhas Felizes"))
        insertAnimal(db, Animal(name="Mingau", species="Gato", age=4, size="Pequeno", sex="Macho", location="Campinas - SP", description="Mingau é tranquilo e adora ficar perto das pessoas. É ideal para apartamento e já está acostumado com caixa de areia.", health="Castrado, vacinado e teste FIV/FeLV negativo.", guardian="Protetora Ana Souza"))
        insertAnimal(db, Animal(name="Thor", species="Cachorro", age=7, size="Grande", sex="Macho", location="Osasco - SP", description="Thor é um cão maduro, companheiro e protetor. Procura uma família paciente e com espaço para passeios.", health="Vacinado e castrado. Usa medicação diária para as articulações.", guardian="Abrigo Novo Lar"))
        insertAnimal(db, Animal(name="Nina", species="Gato", age=1, size="Pequeno", sex="Fêmea", location="São Paulo - SP", description="Nina é curiosa, ativa e muito apegada. Convive bem com outros gatos.", health="Vacinada, castrada e vermifugada.", guardian="Instituto Patinhas Felizes", adopted=true))
    }

    private fun values(a: Animal) = ContentValues().apply {
        put("name", a.name)
        put("species", a.species)
        put("age", a.age)
        put("size", a.size)
        put("sex", a.sex)
        put("location", a.location)
        put("description", a.description)
        put("health", a.health)
        put("guardian", a.guardian)
        put("adopted", if (a.adopted) 1 else 0)
    }

    private fun insertAnimal(db: SQLiteDatabase, animal: Animal) = db.insert("animals", null, values(animal))
    private fun validate(animal: Animal) {
        require(listOf(animal.name, animal.location, animal.description, animal.guardian).all { it.isNotBlank() }) { "Preencha os campos obrigatórios." }
        require(animal.age in 0..50) { "Informe uma idade entre 0 e 50 anos." }
        require(animal.species.isNotBlank() && animal.species.length <= 50 && !animal.species.equals("Todos", true)) { "Informe um tipo de animal com até 50 caracteres." }
        require(animal.size in listOf("Pequeno", "Médio", "Grande")) { "Porte inválido." }
        require(animal.sex in listOf("Fêmea", "Macho")) { "Sexo inválido." }
    }
    fun insertAnimal(animal: Animal): Long { validate(animal)
        require(isResponsible()) { "Entre com uma conta de responsável." }
        val data = values(animal.copy(guardian=guardianName()))
        data.put("owner_id", currentUserId())
        return writableDatabase.insertOrThrow("animals", null, data).also { check(it != -1L) { "Não foi possível salvar o animal." } } }
    fun updateAnimal(animal: Animal): Int {
        validate(animal)
        requireOwner(animal.id)
        return writableDatabase.update("animals", values(animal.copy(guardian=guardianName())), "id=?", arrayOf(animal.id.toString())).also { require(it == 1) { "Animal não encontrado." } }
    }
    fun deleteAnimal(id: Long): Int {
        requireOwner(id)
        val db = writableDatabase
        db.beginTransaction()
        try {
            require(getInterests(id).isEmpty()) { "Este animal possui interesses registrados. O histórico deve ser preservado." }
            val count = db.delete("animals", "id=?", arrayOf(id.toString()))
            require(count == 1) { "Animal não encontrado." }
            db.setTransactionSuccessful()
            return count
        } finally { db.endTransaction() }
    }

    fun getInterests(animalId: Long): List<String> {
        requireOwner(animalId)
        val entries = mutableListOf<String>()
        readableDatabase.query("interests", null, "animal_id=?", arrayOf(animalId.toString()), null, null, "id DESC").use { c ->
            while (c.moveToNext()) {
                fun value(key: String) = c.getString(c.getColumnIndexOrThrow(key))
                val date = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(value("created_at").toLong()))
                entries += "${value("adopter_name")} • ${value("phone")}\n${value("email")}\nMoradia: ${value("housing")}\nTelas: ${if(value("has_screens")=="1") "Sim" else "Não"} • Experiência: ${if(value("has_experience")=="1") "Sim" else "Não"}\n${value("reason")}\n$date"
            }
        }
        return entries
    }

    fun getAnimal(id: Long): Animal {
        readableDatabase.query("animals", null, "id=?", arrayOf(id.toString()), null, null, null).use { c ->
            require(c.moveToFirst()) { "Animal não encontrado." }
            return fromCursor(c)
        }
    }

    fun getAnimals(species: String="Todos", size: String="Todos", sex: String="Todos", onlyAvailable: Boolean=false): List<Animal> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (species == "Outro") { conditions += "species NOT IN (?,?)"
            args += listOf("Cachorro", "Gato") }
        else if (species != "Todos") { conditions += "species=?"
            args += species }
        if (size != "Todos") { conditions += "size=?"
            args += size }
        if (sex != "Todos") { conditions += "sex=?"
            args += sex }
        if (onlyAvailable) conditions += "adopted=0"
        var where: String? = null
        var selectionArgs: Array<String>? = null
        if (conditions.isNotEmpty()) {
            where = conditions.joinToString(" AND ")
            selectionArgs = args.toTypedArray()
        }
        val result = mutableListOf<Animal>()
        readableDatabase.query("animals", null, where, selectionArgs, null, null, "adopted ASC, name ASC").use { c ->
            while (c.moveToNext()) result += fromCursor(c)
        }
        return result
    }

    private fun fromCursor(c: android.database.Cursor) = Animal(
    id=c.getLong(c.getColumnIndexOrThrow("id")), name=c.getString(c.getColumnIndexOrThrow("name")),
    species=c.getString(c.getColumnIndexOrThrow("species")), age=c.getInt(c.getColumnIndexOrThrow("age")),
    size=c.getString(c.getColumnIndexOrThrow("size")), sex=c.getString(c.getColumnIndexOrThrow("sex")),
    location=c.getString(c.getColumnIndexOrThrow("location")), description=c.getString(c.getColumnIndexOrThrow("description")),
    health=c.getString(c.getColumnIndexOrThrow("health")), guardian=c.getString(c.getColumnIndexOrThrow("guardian")),
    adopted=c.getInt(c.getColumnIndexOrThrow("adopted")) == 1,
    canManage=isResponsible() && c.getLong(c.getColumnIndexOrThrow("owner_id")) == currentUserId()
    )

    fun insertInterest(animalId: Long, name: String, phone: String, housing: String, screens: Boolean, experience: Boolean, reason: String, email: String, consent: Boolean): Long {
        require(consent) { "Confirme a responsabilidade." }
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Informe um e-mail válido." }
        val digits = phone.filter { it.isDigit() }
        require(name.isNotBlank() && reason.isNotBlank()) { "Preencha nome e motivação." }
        require(digits.length in 10..13) { "Informe telefone com DDD válido." }
        require(housing in listOf("Casa", "Apartamento", "Chácara / sítio", "Outro")) { "Selecione o tipo de moradia." }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val animal = getAnimal(animalId)
            require(!animal.adopted) { "Este animal não está disponível para adoção." }
            db.query("interests", arrayOf("id"), "animal_id=? AND phone=?", arrayOf(animalId.toString(), digits), null, null, null).use {
                require(!it.moveToFirst()) { "Já existe interesse deste telefone para este animal." }
            }
            val values = ContentValues().apply {
                put("animal_id", animalId)
                put("email", email.trim())
                put("consent", 1)
                put("adopter_name", name.trim())
                put("phone", digits)
                put("housing", housing)
                put("has_screens", if(screens) 1 else 0)
                put("has_experience", if(experience) 1 else 0)
                put("reason", reason.trim())
                put("created_at", System.currentTimeMillis().toString())
            }
            val id = db.insertOrThrow("interests", null, values)
            db.setTransactionSuccessful()
            return id
        } finally { db.endTransaction() }
    }

    // As contas e a sessão pertencem apenas a esta instalação do aplicativo.
    private val session = context.getSharedPreferences("local_session_" + databaseName, Context.MODE_PRIVATE)

    private fun addAccounts(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE COLLATE NOCASE, phone TEXT NOT NULL,
            password_hash TEXT NOT NULL, salt TEXT NOT NULL,
            role TEXT NOT NULL CHECK(role IN ('adotante','responsavel')))""")
        db.execSQL("ALTER TABLE animals ADD COLUMN owner_id INTEGER REFERENCES users(id)")
        db.execSQL("ALTER TABLE interests ADD COLUMN email TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE interests ADD COLUMN consent INTEGER NOT NULL DEFAULT 0")
        val salt = newSalt()
        val user = ContentValues().apply {
            put("name", "Instituto Patinhas Felizes")
            put("email", "responsavel@adopet.local")
            put("phone", "11999990000")
            put("salt", salt)
            put("password_hash", passwordHash("Adopet@2026", salt))
            put("role", "responsavel")
        }
        val id = db.insertOrThrow("users", null, user)
        // Vincula apenas os exemplos deste responsável. Não atribui animais de terceiros.
        db.execSQL("UPDATE animals SET owner_id=? WHERE guardian=?", arrayOf(id, "Instituto Patinhas Felizes"))
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    private fun passwordHash(password: String, salt: String): String {
        val bytes = android.util.Base64.decode(salt, android.util.Base64.NO_WRAP)
        val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), bytes, 60000, 256)
        val key = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec)
        spec.clearPassword()
        return android.util.Base64.encodeToString(key.encoded, android.util.Base64.NO_WRAP)
    }

    private fun currentUserId(): Long = session.getLong("user_id", 0)

    fun isLoggedIn(): Boolean = currentUserId() != 0L
    fun isResponsible(): Boolean = isLoggedIn() && session.getString("role", "") == "responsavel"
    fun guardianName(): String = session.getString("name", "") ?: ""
    fun clearSession() { session.edit().clear().commit() }
    fun logout() { clearSession() }

    fun refreshSession() {
        if (!isLoggedIn()) return
        readableDatabase.query("users", arrayOf("id"), "id=?", arrayOf(currentUserId().toString()), null, null, null).use {
            if (!it.moveToFirst()) clearSession()
        }
    }

    fun login(email: String, password: String) {
        readableDatabase.query("users", null, "email=?", arrayOf(email.trim()), null, null, null).use { c ->
            require(c.moveToFirst()) { "E-mail ou senha incorretos." }
            val expected = c.getString(c.getColumnIndexOrThrow("password_hash"))
            val salt = c.getString(c.getColumnIndexOrThrow("salt"))
            val actual = passwordHash(password, salt)
            require(java.security.MessageDigest.isEqual(expected.toByteArray(), actual.toByteArray())) { "E-mail ou senha incorretos." }
            session.edit().putLong("user_id", c.getLong(c.getColumnIndexOrThrow("id")))
                .putString("name", c.getString(c.getColumnIndexOrThrow("name")))
                .putString("role", c.getString(c.getColumnIndexOrThrow("role"))).commit()
        }
    }

    fun register(name: String, email: String, phone: String, password: String, role: String) {
        require(name.isNotBlank()) { "Informe o nome." }
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Informe um e-mail válido." }
        require(phone.filter { it.isDigit() }.length in 10..13) { "Informe telefone com DDD válido." }
        require(password.length >= 8) { "Use uma senha com pelo menos 8 caracteres." }
        require(role in listOf("adotante", "responsavel")) { "Perfil inválido." }
        val salt = newSalt()
        val values = ContentValues().apply {
            put("name", name.trim())
            put("email", email.trim().lowercase(java.util.Locale.ROOT))
            put("phone", phone.filter { it.isDigit() })
            put("password_hash", passwordHash(password, salt))
            put("salt", salt)
            put("role", role)
        }
        try { writableDatabase.insertOrThrow("users", null, values) }
        catch (error: android.database.sqlite.SQLiteConstraintException) {
            throw IllegalArgumentException("Este e-mail já está cadastrado.")
        }
        login(email, password)
    }

    private fun requireOwner(id: Long) {
        require(getAnimal(id).canManage) { "Você só pode gerenciar os seus próprios animais." }
    }
}
