package com.estilo.estilo_al_paso

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.estilo.estilo_al_paso.data.model.Usuarios
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class RegisterAdminActivity : AppCompatActivity() {

    private lateinit var nombreLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var nombreInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_admin)

        supportActionBar?.title = "Registrar Administrador"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nombreLayout = findViewById(R.id.regAdminNombreLayout)
        passwordLayout = findViewById(R.id.regAdminPasswordLayout)
        nombreInput = findViewById(R.id.regAdminNombreInput)
        passwordInput = findViewById(R.id.regAdminPasswordInput)
        progressBar = findViewById(R.id.regAdminProgress)

        findViewById<Button>(R.id.btnRegistrarAdmin).setOnClickListener { registrarAdmin() }
    }

    private fun registrarAdmin() {
        val nombre = nombreInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        nombreLayout.error = null
        passwordLayout.error = null

        if (nombre.isEmpty()) { nombreLayout.error = "Ingresa un nombre"; return }
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
                val nuevoAdmin = Usuarios(
                    nameUser = nombre,
                    passwordUser = password,
                    rolUser = SessionManager.ROL_ADMIN,
                    estadoUser = "activo"
                )
                db.collection("usuarios").add(nuevoAdmin)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, "Administrador creado", Toast.LENGTH_SHORT).show()
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
        findViewById<Button>(R.id.btnRegistrarAdmin).isEnabled = !loading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}