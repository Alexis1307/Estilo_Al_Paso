package com.estilo.estilo_al_paso.ui.cliente

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.estilo.estilo_al_paso.R
class DetailsClienteActivity : AppCompatActivity() {

    private lateinit var viewModel: DetailsClienteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_client)

        val idCliente = intent.getStringExtra("idCliente")

        viewModel = ViewModelProvider(this)[DetailsClienteViewModel::class.java]

        idCliente?.let {
            viewModel.cargarDetalleCliente(it)
        }

        viewModel.cliente.observe(this) { cliente ->

            findViewById<TextView>(R.id.dtlNombreCliente).text = cliente.nameCliente
            findViewById<TextView>(R.id.dtlDniCliente).text = cliente.dniCliente
            findViewById<TextView>(R.id.dtlTelefonoCliente).text = cliente.telefonoCliente
            findViewById<TextView>(R.id.dtlCiudadCliente).text = cliente.ciudadCliente
            findViewById<TextView>(R.id.dtlDireccionCliente).text = cliente.direccionCliente

        }

    }
}
