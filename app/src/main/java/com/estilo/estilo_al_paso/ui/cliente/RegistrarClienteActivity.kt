package com.estilo.estilo_al_paso.ui.cliente

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.repository.ClienteRepository
import com.google.android.material.textfield.TextInputLayout

class RegistrarClienteActivity : AppCompatActivity() {
    private lateinit var repository: ClienteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nuevo_cliente)

        repository = ClienteRepository()

        val nombre = findViewById<TextInputLayout>(R.id.rgsNombreCli)
        val dni = findViewById<TextInputLayout>(R.id.rgsDniCli)
        val telefono = findViewById<TextInputLayout>(R.id.rgsTelefCli)
        val direccion = findViewById<TextInputLayout>(R.id.rgsDireccCli)
        val ciudad = findViewById<TextInputLayout>(R.id.rgsCiudadCli)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarCliente)

        btnRegistrar.setOnClickListener {

            val nombreTexto = nombre.editText?.text.toString().trim()
            val dniTexto = dni.editText?.text.toString().trim()
            val telefonoTexto = telefono.editText?.text.toString().trim()
            val direccionTexto = direccion.editText?.text.toString().trim()
            val ciudadTexto = ciudad.editText?.text.toString().trim()

            if (nombreTexto.isEmpty() || dniTexto.isEmpty()) {
                Toast.makeText(this, "Nombre y DNI son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repository.crearCliente(
                name = nombreTexto,
                dni = dniTexto,
                direccion = direccionTexto,
                ciudad = ciudadTexto,
                telefono = telefonoTexto
            )

            Toast.makeText(this, "Cliente registrado correctamente", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}