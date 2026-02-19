package com.estilo.estilo_al_paso.ui.cliente

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.repository.ClienteRepository
import com.google.android.material.textfield.TextInputLayout

class RegistrarClienteActivity : AppCompatActivity() {

    private lateinit var repository: ClienteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nuevo_cliente)

        repository = ClienteRepository()

        // Campos del formulario
        val nombre = findViewById<TextInputLayout>(R.id.rgsNombreCli)
        val dni = findViewById<TextInputLayout>(R.id.rgsDniCli)
        val telefono = findViewById<TextInputLayout>(R.id.rgsTelefCli)
        val direccion = findViewById<TextInputLayout>(R.id.rgsDireccCli)

        val radioGroup = findViewById<RadioGroup>(R.id.radioCiudadGroup)

        val ciudadOtroLayout = findViewById<TextInputLayout>(R.id.rgsCiudadOtroLayout)
        val ciudadOtroInput = findViewById<EditText>(R.id.rgsCiudadOtroInput)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarCliente)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOtro) {
                ciudadOtroLayout.visibility = android.view.View.VISIBLE
            } else {
                ciudadOtroLayout.visibility = android.view.View.GONE
            }
        }

        btnRegistrar.setOnClickListener {
            val nombreTexto = nombre.editText?.text.toString().trim()
            val dniTextoRaw = dni.editText?.text.toString().trim()
            val telefonoTextoRaw = telefono.editText?.text.toString().trim()
            val direccionTexto = direccion.editText?.text.toString().trim()

            val dniTexto = dniTextoRaw.filter { it.isDigit() }
            val telefonoTexto = telefonoTextoRaw.replace("+51", "").filter { it.isDigit() }

            nombre.error = null
            dni.error = null
            telefono.error = null
            ciudadOtroLayout.error = null

            var esValido = true

            if (nombreTexto.isEmpty()) {
                nombre.error = "El nombre es obligatorio"
                esValido = false
            }

            if (dniTexto.length != 8) {
                dni.error = "El DNI debe tener 8 dígitos"
                esValido = false
            }

            if (telefonoTexto.length != 9) {
                telefono.error = "El teléfono debe tener 9 dígitos"
                esValido = false
            }

            val ciudadTexto = when (radioGroup.checkedRadioButtonId) {
                R.id.radioTrujillo -> "Trujillo"
                R.id.radioOtro -> ciudadOtroInput.text.toString().trim()
                else -> ""
            }

            if (ciudadTexto.isEmpty()) {
                ciudadOtroLayout.error = "Debes ingresar la ciudad"
                esValido = false
            }

            if (!esValido) return@setOnClickListener

            val tipoEnvio = if (ciudadTexto.equals("Trujillo", ignoreCase = true)) {
                "delivery"
            } else {
                "encomienda"
            }

            repository.crearCliente(
                name = nombreTexto,
                dni = dniTexto,
                direccion = direccionTexto,
                ciudad = ciudadTexto,
                telefono = telefonoTexto
            )

            Toast.makeText(
                this,
                "Cliente registrado correctamente. Tipo de envío: $tipoEnvio",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}
