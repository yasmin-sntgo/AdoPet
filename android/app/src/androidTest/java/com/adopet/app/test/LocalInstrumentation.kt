package com.adopet.app.test

import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import com.adopet.app.Animal
import com.adopet.app.DatabaseHelper

// Usa bancos separados. Não altera os cadastros da apresentação.
class LocalInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    private fun denied(action: () -> Unit) {
        var blocked = false
        try { action() } catch (error: IllegalArgumentException) { blocked = true }
        check(blocked) { "Uma operação que deveria ser bloqueada foi aceita." }
    }

    override fun onStart() {
        val name = "test_local_${System.currentTimeMillis()}.db"
        var db = DatabaseHelper(targetContext, name)
        try {
            check(db.getAnimals().size == 4)
            denied { db.getInterests(1) }
            val animal = Animal(name="Teste", species="Coelho", age=1, size="Pequeno", sex="Fêmea",
                location="Cidade de teste", description="Animal temporário", health="Saudável", guardian="Teste")
            denied { db.insertAnimal(animal) }
            denied { db.login("responsavel@adopet.local", "errada") }
            db.login("responsavel@adopet.local", "Adopet@2026")
            check(db.getAnimal(1).canManage)
            check(!db.getAnimal(2).canManage)
            val id = db.insertAnimal(animal)
            check(db.getAnimal(id).canManage)
            check(db.getAnimals("Outro", "Pequeno", "Fêmea", true).any { it.id == id })
            db.updateAnimal(animal.copy(id=id, species="Calopsita", name="Editado"))
            db.close()
            db = DatabaseHelper(targetContext, name)
            check(db.getAnimal(id).name == "Editado")
            check(db.isResponsible())
            val disposable = db.insertAnimal(animal.copy(name="Excluir"))
            db.deleteAnimal(disposable)
            denied { db.getAnimal(disposable) }
            denied { db.insertAnimal(animal.copy(age=-1)) }
            db.logout()
            db.register("Outro responsável", "outro@example.com", "11999990001", "Senha1234", "responsavel")
            check(!db.getAnimal(id).canManage)
            denied { db.updateAnimal(animal.copy(id=id)) }
            denied { db.deleteAnimal(id) }
            denied { db.getInterests(id) }
            denied { db.register("Duplicado", "OUTRO@example.com", "11999990001", "Senha1234", "responsavel") }
            db.register("Adotante", "adotante@example.com", "11999990002", "Senha1234", "adotante")
            denied { db.insertAnimal(animal) }
            db.logout()
            denied { db.insertInterest(id, "Teste", "11999990003", "Casa", true, true, "Motivo", "invalido", true) }
            denied { db.insertInterest(id, "Teste", "11999990003", "Casa", true, true, "Motivo", "teste@example.com", false) }
            db.insertInterest(id, "Teste", "(11) 99999-0003", "Casa", true, true, "Motivo", "teste@example.com", true)
            denied { db.insertInterest(id, "Teste", "11999990003", "Casa", true, true, "Motivo", "teste@example.com", true) }
            denied { db.insertInterest(4, "Teste", "11999990003", "Casa", true, true, "Motivo", "teste@example.com", true) }
            db.login("responsavel@adopet.local", "Adopet@2026")
            check(db.getInterests(id).single().contains("teste@example.com"))
            denied { db.deleteAnimal(id) }
            db.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use { check(!it.moveToFirst()) }
            checkMigration()
            finish(Activity.RESULT_OK, Bundle().apply {
                putString("stream", "OK: SQLite local, CRUD, filtro Outro, persistência, login, perfis, propriedade, validações, duplicidade, histórico, migração e integridade.\nTODOS OS TESTES PASSARAM\n")
            })
        } catch (error: Throwable) {
            finish(Activity.RESULT_CANCELED, Bundle().apply { putString("stream", "FALHOU: ${error.stackTraceToString()}") })
        } finally {
            db.clearSession()
            db.close()
            targetContext.deleteDatabase(name)
        }
    }

    private fun checkMigration() {
        val name = "test_migration_${System.currentTimeMillis()}.db"
        val old = targetContext.openOrCreateDatabase(name, 0, null)
        old.execSQL("""CREATE TABLE animals(id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL, species TEXT NOT NULL, age INTEGER NOT NULL,
            size TEXT NOT NULL, sex TEXT NOT NULL, location TEXT NOT NULL,
            description TEXT NOT NULL, health TEXT NOT NULL, guardian TEXT NOT NULL,
            adopted INTEGER NOT NULL DEFAULT 0)""")
        old.execSQL("""CREATE TABLE interests(id INTEGER PRIMARY KEY AUTOINCREMENT,
            animal_id INTEGER NOT NULL REFERENCES animals(id) ON DELETE RESTRICT,
            adopter_name TEXT NOT NULL, phone TEXT NOT NULL, housing TEXT NOT NULL,
            has_screens INTEGER NOT NULL, has_experience INTEGER NOT NULL,
            reason TEXT NOT NULL, created_at TEXT NOT NULL)""")
        old.execSQL("INSERT INTO animals VALUES(1,'Preservado','Gato',2,'Pequeno','Macho','Cidade','Descrição','Saúde','Instituto Patinhas Felizes',0)")
        old.execSQL("INSERT INTO interests VALUES(1,1,'Pessoa','11999990000','Casa',1,1,'Motivo','100000')")
        old.version = 2
        old.close()
        val migrated = DatabaseHelper(targetContext, name)
        try {
            check(migrated.getAnimal(1).name == "Preservado")
            migrated.login("responsavel@adopet.local", "Adopet@2026")
            check(migrated.getInterests(1).size == 1)
            check(migrated.getAnimal(1).canManage)
        } finally {
            migrated.clearSession()
            migrated.close()
            targetContext.deleteDatabase(name)
        }
    }
}
