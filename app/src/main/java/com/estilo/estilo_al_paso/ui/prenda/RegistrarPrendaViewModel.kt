package com.estilo.estilo_al_paso.ui.prenda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.data.repository.PaqueteRepository
import com.google.firebase.firestore.FirebaseFirestore

class RegistrarPrendaViewModel : ViewModel() {

    private val repository = PaqueteRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _clienteSeleccionado = MutableLiveData<Cliente?>()
    val clienteSeleccionado: LiveData<Cliente?> = _clienteSeleccionado

    private val _prendas = MutableLiveData<List<Prenda>>(emptyList())
    val prendas: LiveData<List<Prenda>> = _prendas

    private val _totalPrendas = MutableLiveData(0)
    val totalPrendas: LiveData<Int> get() = _totalPrendas
    private val _totalPagar = MutableLiveData(0.0)
    val totalPagar: LiveData<Double> = _totalPagar

    private val _totalPendiente = MutableLiveData(0.0)
    val totalPendiente: LiveData<Double> = _totalPendiente

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> = _guardadoExitoso

    private val _clientes = MutableLiveData<List<Cliente>>(emptyList())
    val clientes: LiveData<List<Cliente>> = _clientes


    fun seleccionarCliente(cliente: Cliente) {
        _clienteSeleccionado.value = cliente
    }

    fun cargarClientes() {
        _loading.value = true
        db.collection("clientes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Cliente::class.java)
                _clientes.value = lista
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _loading.value = false
            }
    }


    fun agregarPrenda(prenda: Prenda) {
        val listaActual = _prendas.value?.toMutableList() ?: mutableListOf()
        listaActual.add(prenda)
        _prendas.value = listaActual
        _totalPrendas.value = listaActual.size

        recalcularTotales()
    }

    fun eliminarPrenda(prenda: Prenda) {
        val listaActual = _prendas.value?.toMutableList() ?: return
        listaActual.remove(prenda)
        _prendas.value = listaActual
        _totalPrendas.value = listaActual.size

        recalcularTotales()
    }

    private fun recalcularTotales() {
        val lista = _prendas.value.orEmpty()

        val total = lista.sumOf { it.precioPrenda }
        val pendiente = lista
            .filter { it.estadoPago == Prenda.EstadoPago.pendiente }
            .sumOf { it.precioPrenda }

        _totalPagar.value = total
        _totalPendiente.value = pendiente
    }

    fun guardarPaquete() {
        val cliente = _clienteSeleccionado.value
        val listaPrendas = _prendas.value

        if (cliente == null) {
            _error.value = "Debe seleccionar un cliente"
            return
        }

        if (listaPrendas.isNullOrEmpty()) {
            _error.value = "Debe agregar al menos una prenda"
            return
        }

        _loading.value = true

        repository.guardarPaqueteCompleto(
            cliente = cliente,
            prendas = listaPrendas,
            onSuccess = {
                _loading.postValue(false)
                _guardadoExitoso.postValue(true)
                limpiarDatos()
            },
            onError = {
                _loading.postValue(false)
                _error.postValue(it.message)
            }
        )
    }

    fun limpiarDatos() {
        _prendas.value = emptyList()
        _totalPrendas.value = 0
        _clienteSeleccionado.value = null
    }

    fun limpiarEventoGuardado() {
        _guardadoExitoso.value = false
    }
}