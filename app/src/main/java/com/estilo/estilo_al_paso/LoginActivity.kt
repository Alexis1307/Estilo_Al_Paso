package com.estilo.estilo_al_paso

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estilo.estilo_al_paso.data.model.Usuarios
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var usuarioLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var usuarioInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            irAMain()
            return
        }

        usuarioLayout = findViewById(R.id.loginUsuarioLayout)
        passwordLayout = findViewById(R.id.loginPasswordLayout)
        usuarioInput = findViewById(R.id.loginUsuarioInput)
        passwordInput = findViewById(R.id.loginPasswordInput)
        progressBar = findViewById(R.id.loginProgress)

        findViewById<Button>(R.id.btnIngresar).setOnClickListener { intentarLogin() }
    }

    private fun intentarLogin() {
        val usuario = usuarioInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        usuarioLayout.error = null
        passwordLayout.error = null

        if (usuario.isEmpty()) { usuarioLayout.error = "Ingresa tu usuario"; return }
        if (password.isEmpty()) { passwordLayout.error = "Ingresa tu contraseña"; return }

        setLoading(true)

        lifecycleScope.launch {
            try {

                val docs = withContext(Dispatchers.IO) {
                    db.collection("usuarios")
                        .whereEqualTo("nameUser", usuario)
                        .get()
                        .await()
                }

                if (docs.isEmpty) {
                    setLoading(false)
                    usuarioLayout.error = "Usuario no encontrado"
                    return@launch
                }

                val doc = docs.documents[0]
                val user = doc.toObject(Usuarios::class.java)

                if (user == null) {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                    return@launch
                }


                if (user.estadoUser != "activo") {
                    setLoading(false)
                    usuarioLayout.error = "Usuario desactivado"
                    return@launch
                }

                // BCrypt en hilo de fondo
                val passwordCorrecta = withContext(Dispatchers.IO) {
                    BCryptHelper.verificarPassword(password, user.passwordUser)
                }

                setLoading(false)

                if (!passwordCorrecta) {
                    passwordLayout.error = "Contraseña incorrecta"
                    return@launch
                }

                sessionManager.guardarSesion(doc.id, user.nameUser, user.rolUser)
                irAMain()

            } catch (e: Exception) {
                setLoading(false)
                // Mostrar el error real para diagnosticar
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btnIngresar).isEnabled = !loading
    }
}