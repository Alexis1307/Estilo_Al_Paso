package com.estilo.estilo_al_paso.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.ui.cliente.ClienteAdapter
import com.estilo.estilo_al_paso.ui.cliente.RegistrarClienteActivity
import com.estilo.estilo_al_paso.viewmodel.ClienteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ClientesFragment : Fragment() {

    private val viewModel: ClienteViewModel by viewModels()
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
        observarClientes()

        viewModel.cargarClientes()
    }

    private fun configurarRecyclerView(view: View) {
        adapter = ClienteAdapter()

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
        viewModel.clientes.observe(viewLifecycleOwner) { lista ->

            val listaOrdenada = lista.sortedByDescending { it.deudaTotal > 0 }

            adapter.actualizarLista(listaOrdenada)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.cargarClientes()
    }
}