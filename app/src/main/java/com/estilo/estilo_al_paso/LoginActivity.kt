package com.estilo.estilo_al_paso

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.estilo.estilo_al_paso.data.model.Usuarios
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

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

        findViewById<TextView>(R.id.tvIrARegistrar).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun intentarLogin() {
        val usuario = usuarioInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        usuarioLayout.error = null
        passwordLayout.error = null

        if (usuario.isEmpty()) { usuarioLayout.error = "Ingresa tu usuario"; return }
        if (password.isEmpty()) { passwordLayout.error = "Ingresa tu contraseña"; return }

        setLoading(true)

        db.collection("usuarios")
            .whereEqualTo("nameUser", usuario)
            .whereEqualTo("estadoUser", "activo")
            .get()
            .addOnSuccessListener { docs ->
                setLoading(false)
                if (docs.isEmpty) {
                    usuarioLayout.error = "Usuario no encontrado"
                    return@addOnSuccessListener
                }
                val doc = docs.documents[0]
                val user = doc.toObject(Usuarios::class.java)
                if (user == null || user.passwordUser != password) {
                    passwordLayout.error = "Contraseña incorrecta"
                    return@addOnSuccessListener
                }
                sessionManager.guardarSesion(doc.id, user.nameUser, user.rolUser)
                irAMain()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
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