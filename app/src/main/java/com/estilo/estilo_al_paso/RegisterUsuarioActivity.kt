package com.estilo.estilo_al_paso

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
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

class RegisterUsuarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROL = "extra_rol"
    }

    private lateinit var nombreLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var nombreInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var rolACrear: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_usuario)

        rolACrear = intent.getStringExtra(EXTRA_ROL) ?: SessionManager.ROL_EMPLEADO

        val titulo = if (rolACrear == SessionManager.ROL_ADMIN) "Registrar Administrador" else "Registrar Empleado"
        findViewById<TextView>(R.id.regTitulo).text = titulo
        supportActionBar?.title = titulo
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nombreLayout = findViewById(R.id.regNombreLayout)
        passwordLayout = findViewById(R.id.regPasswordLayout)
        nombreInput = findViewById(R.id.regNombreInput)
        passwordInput = findViewById(R.id.regPasswordInput)
        progressBar = findViewById(R.id.regProgress)

        findViewById<Button>(R.id.btnRegistrarUsuario).setOnClickListener { registrar() }
    }

    private fun registrar() {
        val nombre = nombreInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        nombreLayout.error = null
        passwordLayout.error = null

        if (nombre.isEmpty()) { nombreLayout.error = "Ingresa un nombre"; return }
        if (nombre.length < 3) { nombreLayout.error = "Mínimo 3 caracteres"; return }
        if (password.isEmpty()) { passwordLayout.error = "Ingresa una contraseña"; return }
        if (password.length < 6) { passwordLayout.error = "Mínimo 6 caracteres"; return }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val docs = withContext(Dispatchers.IO) {
                    db.collection("usuarios")
                        .whereEqualTo("nameUser", nombre)
                        .get()
                        .await()
                }

                if (!docs.isEmpty) {
                    setLoading(false)
                    nombreLayout.error = "Ese nombre ya existe"
                    return@launch
                }

                val passwordHash = withContext(Dispatchers.IO) {
                    BCryptHelper.hashPassword(password)
                }

                val nuevoUsuario = Usuarios(
                    nameUser = nombre,
                    passwordUser = passwordHash,
                    rolUser = rolACrear,
                    estadoUser = "activo"
                )

                withContext(Dispatchers.IO) {
                    db.collection("usuarios").add(nuevoUsuario).await()
                }

                setLoading(false)
                val msg = if (rolACrear == SessionManager.ROL_ADMIN) "Administrador creado" else "Empleado creado"
                Toast.makeText(this@RegisterUsuarioActivity, msg, Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@RegisterUsuarioActivity, "Error al registrar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btnRegistrarUsuario).isEnabled = !loading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}