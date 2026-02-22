package com.estilo.estilo_al_paso.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.ui.cliente.ClienteAdapter
import com.estilo.estilo_al_paso.ui.cliente.RegistrarClienteActivity
import com.estilo.estilo_al_paso.ui.cliente.ClienteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import androidx.core.widget.addTextChangedListener
import com.estilo.estilo_al_paso.ui.cliente.DetailsClienteActivity
import com.estilo.estilo_al_paso.ui.cliente.DetailsClienteViewModel


class ClientesFragment : Fragment() {

    private val viewModel: ClienteViewModel by viewModels()
    private val detailsViewModel: DetailsClienteViewModel by viewModels()

    private lateinit var adapter: ClienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView(view)
        configurarFab(view)
        configurarBuscador(view)
        configurarBotones(view)
        observarClientes()

        detailsViewModel.actualizarClientes.observe(viewLifecycleOwner) {
            viewModel.cargarClientes()
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter.itemCount == 0) {
            viewModel.cargarClientes()
        }
    }

    private fun configurarRecyclerView(view: View) {
        adapter = ClienteAdapter { cliente ->

            val intent = Intent(requireContext(), DetailsClienteActivity::class.java)
            intent.putExtra("idCliente", cliente.idCliente)
            startActivity(intent)

        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvClientes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

    }

    private fun configurarFab(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.registerCliente)

        fab.setOnClickListener {
            val intent = Intent(requireContext(), RegistrarClienteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observarClientes() {
        viewModel.clientesFiltrados.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
        }
    }

    private fun configurarBuscador(view: View) {

        val etBuscar = view.findViewById<TextInputEditText>(R.id.etBuscar)

        etBuscar.addTextChangedListener { editable ->
            viewModel.actualizarTextoBusqueda(editable?.toString() ?: "")
        }
    }

    private fun configurarBotones(view: View) {

        val btnPendientes = view.findViewById<Button>(R.id.btnPendiente)
        val btnPagados = view.findViewById<Button>(R.id.btnPagado)
        val btnSinPrendas = view.findViewById<Button>(R.id.btnSinPrendas)
        val btnTodos = view.findViewById<Button>(R.id.btnTodos)

        btnPendientes.setOnClickListener {
            viewModel.filtrarPendientes()
        }

        btnPagados.setOnClickListener {
            viewModel.filtrarPagados()
        }

        btnSinPrendas.setOnClickListener {
            viewModel.filtrarSinPrendas()
        }

        btnTodos.setOnClickListener {
            viewModel.limpiarFiltroEstado()
        }
    }
}