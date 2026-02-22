package com.estilo.estilo_al_paso.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.ui.prenda.PrendaAdapter
import com.estilo.estilo_al_paso.ui.prenda.RegistrarPrendaViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.util.*

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

        viewModel.cargarClientes()

        return view
    }

    private fun setupRecyclerView(view: View) {
        adapter = PrendaAdapter { prenda ->
            viewModel.eliminarPrenda(prenda)
        }

        view.findViewById<RecyclerView>(R.id.rvPrendas).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RegisterPrendaFragment.adapter
        }
    }

    private fun setupObservers(view: View) {

        viewModel.prendas.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
        }

        viewModel.clienteSeleccionado.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.tvNombreCliente)
                .text = "Cliente                              ${it?.nameCliente ?: "-"}"
        }

        viewModel.totalPrendas.observe(viewLifecycleOwner) { cantidad ->
            view.findViewById<TextView>(R.id.tvResumenCantidad).text =
                "Cantidad prendas:         $cantidad"
        }

        viewModel.totalPagar.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.tvTotal)
                .text = "Total:                                S/ %.2f".format(it)
        }

        viewModel.totalPendiente.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.tvPendiente)
                .text = "Pendiente:                       S/ %.2f".format(it)

            val pagado = (viewModel.totalPagar.value ?: 0.0) - it
            view.findViewById<TextView>(R.id.tvPagado)
                .text = "Pagado:                           S/ %.2f".format(pagado)
        }

        viewModel.clientes.observe(viewLifecycleOwner) { listaClientes ->
            val nombres = listaClientes.map { it.nameCliente }
            val adapterClientes = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )

            val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.actClientes)
            autoComplete.setAdapter(adapterClientes)

            autoComplete.setOnItemClickListener { parent, _, position, _ ->
                val clienteSeleccionado = parent.getItemAtPosition(position) as String
                val cliente = listaClientes.find { it.nameCliente == clienteSeleccionado }
                if (cliente != null) {
                    viewModel.seleccionarCliente(cliente)
                }
            }

            autoComplete.setOnEditorActionListener { textView, actionId, _ ->
                val nombreInput = textView.text.toString().trim()
                val cliente = listaClientes.find { it.nameCliente == nombreInput }
                if (cliente != null) {
                    viewModel.seleccionarCliente(cliente)
                }
                false
            }

            autoComplete.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val nombreInput = autoComplete.text.toString().trim()
                    val cliente = listaClientes.find { it.nameCliente == nombreInput }
                    if (cliente != null) {
                        viewModel.seleccionarCliente(cliente)
                    }
                }
            }
        }

        viewModel.guardadoExitoso.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(
                    requireContext(),
                    "Paquete guardado correctamente",
                    Toast.LENGTH_LONG
                ).show()
                limpiarCamposUI(view)
                viewModel.limpiarEventoGuardado()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            view.findViewById<MaterialButton>(R.id.btnGuardarPaquete)
                .isEnabled = !isLoading
        }
    }

    private fun setupListeners(view: View) {

        val btnAgregar = view.findViewById<MaterialButton>(R.id.btnAgregarPrenda)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarPaquete)

        btnAgregar.setOnClickListener {

            val precioText =
                view.findViewById<EditText>(R.id.etPrecio).text.toString()
            val descripcion =
                view.findViewById<EditText>(R.id.etDescripcion).text.toString()

            if (precioText.isBlank() || descripcion.isBlank()) return@setOnClickListener

            val precio = precioText.toDoubleOrNull() ?: return@setOnClickListener

            val estadoPago =
                if (view.findViewById<Chip>(R.id.chipPendiente).isChecked)
                    Prenda.EstadoPago.pendiente
                else
                    Prenda.EstadoPago.pagado

            val estadoPrenda = when {
                view.findViewById<Chip>(R.id.chipBuenestado).isChecked ->
                    Prenda.EstadoPrenda.buenEstado
                view.findViewById<Chip>(R.id.chipLavanderia).isChecked ->
                    Prenda.EstadoPrenda.lavanderia
                else ->
                    Prenda.EstadoPrenda.reparacion
            }

            val prenda = Prenda(
                idPrenda = UUID.randomUUID().toString(),
                descripcionPrenda = descripcion,
                precioPrenda = precio,
                estadoPago = estadoPago,
                estadoPrenda = estadoPrenda
            )

            viewModel.agregarPrenda(prenda)

            view.findViewById<EditText>(R.id.etPrecio).text.clear()
            view.findViewById<EditText>(R.id.etDescripcion).text.clear()
        }

        btnGuardar.setOnClickListener {
            val cliente = viewModel.clienteSeleccionado.value
            if (cliente == null) {
                Toast.makeText(requireContext(), "Selecciona un cliente válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.guardarPaquete()
        }
    }

    private fun limpiarCamposUI(view: View) {
        view.findViewById<AutoCompleteTextView>(R.id.actClientes).text.clear()
        view.findViewById<TextView>(R.id.tvNombreCliente).text = "Cliente: -"
    }
}