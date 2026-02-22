package com.estilo.estilo_al_paso.ui.cliente

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.estilo.estilo_al_paso.R
import com.google.android.material.textfield.TextInputLayout

class RegistrarClienteActivity : AppCompatActivity() {

    private lateinit var viewModel: ClienteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nuevo_cliente)

        viewModel = ViewModelProvider(this)[ClienteViewModel::class.java]

        val nombre = findViewById<TextInputLayout>(R.id.rgsNombreCli)
        val dni = findViewById<TextInputLayout>(R.id.rgsDniCli)
        val telefono = findViewById<TextInputLayout>(R.id.rgsTelefCli)
        val direccion = findViewById<TextInputLayout>(R.id.rgsDireccCli)

        val radioGroup = findViewById<RadioGroup>(R.id.radioCiudadGroup)

        val ciudadOtroLayout = findViewById<TextInputLayout>(R.id.rgsCiudadOtroLayout)
        val ciudadOtroInput = findViewById<EditText>(R.id.rgsCiudadOtroInput)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarCliente)

        viewModel.clienteCreado.observe(this) {
            Toast.makeText(this, "Cliente registrado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, it ?: "Error al registrar", Toast.LENGTH_LONG).show()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            ciudadOtroLayout.visibility =
                if (checkedId == R.id.radioOtro) View.VISIBLE else View.GONE
        }

        btnRegistrar.setOnClickListener {

            val nombreTexto = nombre.editText?.text.toString().trim()
            val dniTexto = dni.editText?.text.toString().trim().filter { it.isDigit() }
            val telefonoTexto = telefono.editText?.text.toString()
                .replace("+51", "")
                .filter { it.isDigit() }
            val direccionTexto = direccion.editText?.text.toString().trim()

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

            viewModel.crearCliente(
                name = nombreTexto,
                dni = dniTexto,
                direccion = direccionTexto,
                ciudad = ciudadTexto,
                telefono = telefonoTexto
            )
        }
    }
}
