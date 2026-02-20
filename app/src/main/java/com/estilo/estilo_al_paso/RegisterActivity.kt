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

class RegisterActivity : AppCompatActivity() {

    private lateinit var nombreLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var nombreInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nombreLayout = findViewById(R.id.regNombreLayout)
        passwordLayout = findViewById(R.id.regPasswordLayout)
        nombreInput = findViewById(R.id.regNombreInput)
        passwordInput = findViewById(R.id.regPasswordInput)
        progressBar = findViewById(R.id.regProgress)

        findViewById<Button>(R.id.btnRegistrar).setOnClickListener { registrarCliente() }
        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener { finish() }
    }

    private fun registrarCliente() {
        val nombre = nombreInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        nombreLayout.error = null
        passwordLayout.error = null

        if (nombre.isEmpty()) { nombreLayout.error = "Ingresa un nombre de usuario"; return }
        if (nombre.length < 3) { nombreLayout.error = "Mínimo 3 caracteres"; return }
        if (password.isEmpty()) { passwordLayout.error = "Ingresa una contraseña"; return }
        if (password.length < 6) { passwordLayout.error = "Mínimo 6 caracteres"; return }

        setLoading(true)

        db.collection("usuarios")
            .whereEqualTo("nameUser", nombre)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    setLoading(false)
                    nombreLayout.error = "Ese nombre ya existe"
                    return@addOnSuccessListener
                }
                val nuevoUsuario = Usuarios(
                    nameUser = nombre,
                    passwordUser = password,
                    rolUser = SessionManager.ROL_CLIENTE,
                    estadoUser = "activo"
                )
                db.collection("usuarios").add(nuevoUsuario)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        finish()
                    }
                    .addOnFailureListener {
                        setLoading(false)
                        Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btnRegistrar).isEnabled = !loading
    }
}