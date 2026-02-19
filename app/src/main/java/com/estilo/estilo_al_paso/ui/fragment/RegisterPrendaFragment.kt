package com.estilo.estilo_al_paso.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.ui.prenda.PrendaAdapter
import com.estilo.estilo_al_paso.ui.prenda.RegistrarPrendaViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RegisterPrendaFragment : Fragment() {
    private lateinit var viewModel: RegistrarPrendaViewModel
    private lateinit var adapter: PrendaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_sales_registration,
            container,
            false
        )

        viewModel = ViewModelProvider(this)
            .get(RegistrarPrendaViewModel::class.java)

        setupRecyclerView(view)
        setupObservers(view)
        setupListeners(view)

        cargarClientes(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        adapter = PrendaAdapter { prenda ->
            viewModel.eliminarPrenda(prenda)
        }

        val recycler = view.findViewById<RecyclerView>(R.id.rvPrendas)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
    }

    private fun setupObservers(view: View) {

        viewModel.prendas.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
        }

        viewModel.totalPagar.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.tvTotal)
                .text = "Total: S/ %.2f".format(it)
        }

        viewModel.totalPendiente.observe(viewLifecycleOwner) { pendiente ->
            view.findViewById<TextView>(R.id.tvPendiente)
                .text = "Pendiente: S/ %.2f".format(pendiente)

            val pagado = viewModel.totalPagar.value!! - pendiente

            view.findViewById<TextView>(R.id.tvPagado)
                .text = "Pagado: S/ %.2f".format(pagado)
        }

        viewModel.clienteSeleccionado.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.tvResumenCliente)
                .text = "Cliente: ${it?.nameCliente ?: "-"}"
        }

        viewModel.guardadoExitoso.observe(viewLifecycleOwner) {
            if (it == true) {
                context?.let {
                    Toast.makeText(it, "Paquete guardado correctamente"
                        , Toast.LENGTH_LONG).show()
                }

            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(),
                    it,
                    Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            }
    }

    private fun setupListeners(view: View) {

        val btnAgregar = view.findViewById<MaterialButton>(R.id.btnAgregarPrenda)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarPaquete)

        btnAgregar.setOnClickListener {

            val precioText = view.findViewById<EditText>(R.id.etPrecio).text.toString()
            val descripcion = view.findViewById<EditText>(R.id.etDescripcion).text.toString()

            if (precioText.isEmpty() || descripcion.isEmpty()) return@setOnClickListener

            val precio = precioText.toDouble()

            val estadoPago = if (
                view.findViewById<Chip>(R.id.chipPendiente).isChecked
            ) Prenda.EstadoPago.pendiente
            else Prenda.EstadoPago.pagado

            val estadoPrenda = if (
                view.findViewById<Chip>(R.id.chipBuenestado).isChecked
            ) Prenda.EstadoPrenda.buenEstado
            else if (view.findViewById<Chip>(R.id.chipLavanderia).isChecked
                ) Prenda.EstadoPrenda.lavanderia
            else    Prenda.EstadoPrenda.reparacion

            val prenda = Prenda(
                idPrenda = UUID.randomUUID().toString(),
                descripcionPrenda = descripcion,
                precioPrenda = precio,
                estadoPago = estadoPago,
                estadoPrenda = estadoPrenda
            )

            viewModel.agregarPrenda(prenda)
        }

        btnGuardar.setOnClickListener {
            viewModel.guardarPaquete()
        }
    }

    private fun cargarClientes(view: View) {

        val db = FirebaseFirestore.getInstance()

        db.collection("clientes")
            .get()
            .addOnSuccessListener { result ->

                if (!isAdded) return@addOnSuccessListener

                val listaClientes = result.toObjects(Cliente::class.java)
                val nombresClientes = listaClientes.map { it.nameCliente }

                context?.let { ctx ->

                    val adapterClientes = ArrayAdapter(
                        ctx,
                        android.R.layout.simple_dropdown_item_1line,
                        nombresClientes
                    )

                    val autoComplete =
                        view.findViewById<AutoCompleteTextView>(R.id.actClientes)

                    autoComplete.setAdapter(adapterClientes)

                    autoComplete.setOnItemClickListener { _, _, position, _ ->
                        val clienteSeleccionado = listaClientes[position]
                        viewModel.seleccionarCliente(clienteSeleccionado)
                    }
                }
            }
    }

}