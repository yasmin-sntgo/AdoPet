package com.adopet.app
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
open class BaseActivity : AppCompatActivity() {

    // O banco roda fora da tela para não travar os botões e a navegação.
    protected fun runDatabase(task: () -> Unit) {
        Thread {
            try {
                task()
            } catch (error: Exception) {
                updateScreen {
                    val message = if (error is IllegalArgumentException) error.message
                        else "Não foi possível concluir a operação. Tente novamente."
                    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    protected fun updateScreen(action: () -> Unit) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) action()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(findViewById(android.R.id.content))
    }
}
