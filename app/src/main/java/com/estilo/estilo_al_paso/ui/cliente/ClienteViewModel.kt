package com.estilo.estilo_al_paso.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.repository.ClienteRepository

class ClienteViewModel : ViewModel() {

    private val repository = ClienteRepository()

    private val _clienteCreado = MutableLiveData<Boolean>()
    val clienteCreado: LiveData<Boolean> = _clienteCreado

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _clientesOriginales = MutableLiveData<List<Cliente>>()
    private val _clientesFiltrados = MutableLiveData<List<Cliente>>()
    val clientesFiltrados: LiveData<List<Cliente>> = _clientesFiltrados

    private var filtroEstado: FiltroEstado? = null
    private var textoBusqueda: String = ""

    private var listenerRegistrado = false

    enum class FiltroEstado {
        PENDIENTES,
        PAGADOS,
        SIN_PRENDAS
    }

    fun crearCliente(
        name: String,
        dni: String,
        direccion: String,
        ciudad: String,
        telefono: String
    ) {
        repository.crearCliente(
            name,
            dni,
            direccion,
            ciudad,
            telefono,
            onSuccess = {
                _clienteCreado.value = true
                filtroEstado = null
                aplicarFiltros()
            },
            onError = {
                _error.value = it.message
            }
        )
    }

    fun cargarClientes() {
        repository.escucharClientesActivos { lista ->
            _clientesOriginales.value = lista
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {

        var lista = _clientesOriginales.value ?: return

        lista = when (filtroEstado) {
            FiltroEstado.PENDIENTES -> repository.filtrarPorPendientes(lista)
            FiltroEstado.PAGADOS -> repository.filtrarPorPagados(lista)
            FiltroEstado.SIN_PRENDAS -> repository.filtrarPorSinPrendas(lista)
            null -> lista
        }

        if (textoBusqueda.isNotBlank()) {
            lista = repository.filtrarPorNombreODni(lista, textoBusqueda)
        }

        _clientesFiltrados.value = lista
    }


    fun actualizarTextoBusqueda(texto: String) {
        textoBusqueda = texto
        aplicarFiltros()
    }

    fun filtrarPendientes() {
        filtroEstado = FiltroEstado.PENDIENTES
        aplicarFiltros()
    }

    fun filtrarPagados() {
        filtroEstado = FiltroEstado.PAGADOS
        aplicarFiltros()
    }

    fun limpiarFiltroEstado() {
        filtroEstado = null
        aplicarFiltros()
    }

    fun filtrarSinPrendas() {
        filtroEstado = FiltroEstado.SIN_PRENDAS
        aplicarFiltros()
    }


}