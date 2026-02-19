package com.estilo.estilo_al_paso.ui.cliente

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.ui.prenda.PrendaAdapter

class DetailsClienteActivity : AppCompatActivity() {

    private lateinit var viewModel: DetailsClienteViewModel
    private var clienteId: String? = null

    private lateinit var prendaAdapter: PrendaAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_client)

        clienteId = intent.getStringExtra("idCliente")

        viewModel = ViewModelProvider(this)[DetailsClienteViewModel::class.java]

        val recycler = findViewById<RecyclerView>(R.id.rvPrendas)

        prendaAdapter = PrendaAdapter {
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = prendaAdapter


        clienteId?.let {
            viewModel.cargarDetalleCliente(it)
            viewModel.cargarPaqueteActivo(it)
        }

        findViewById<Button>(R.id.btnConfirmarEnvio).setOnClickListener {
            viewModel.confirmarEnvio()
        }

        findViewById<Button>(R.id.btnHistorial).setOnClickListener {
            clienteId?.let {
                viewModel.cargarHistorial(it)
            }
        }

        viewModel.cliente.observe(this) { cliente ->
            findViewById<TextView>(R.id.dtlNombreCliente).text = cliente.nameCliente
            findViewById<TextView>(R.id.dtlDniCliente).text = cliente.dniCliente
            findViewById<TextView>(R.id.dtlTelefonoCliente).text = cliente.telefonoCliente
            findViewById<TextView>(R.id.dtlCiudadCliente).text = cliente.ciudadCliente
            findViewById<TextView>(R.id.dtlDireccionCliente).text = cliente.direccionCliente

        }

        viewModel.totalPrendas.observe(this) {
            findViewById<TextView>(R.id.tvTotalPrendas).text = it.toString()
        }

        viewModel.totalPagado.observe(this) {
            findViewById<TextView>(R.id.tvTotalPagado)
                .text = "S/ %.2f".format(it)
        }

        viewModel.totalDeuda.observe(this) {
            findViewById<TextView>(R.id.tvTotalDeuda)
                .text = "S/ %.2f".format(it)
        }

        viewModel.prendas.observe(this) { lista ->
            prendaAdapter.submitList(lista)
        }

        viewModel.prendas.observe(this) { lista ->
            println("PRENDAS CARGADAS: ${lista.size}")
            prendaAdapter.submitList(lista)
        }


        viewModel.historial.observe(this) { lista ->

            Toast.makeText(
                this,
                "Tiene ${lista.size} paquetes en historial",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.envioConfirmado.observe(this) { confirmado ->
            if (confirmado) {
                Toast.makeText(this, "Envío confirmado", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
