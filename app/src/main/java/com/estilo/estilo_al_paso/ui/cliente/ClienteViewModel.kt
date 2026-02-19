package com.estilo.estilo_al_paso.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.repository.ClienteRepository

class ClienteViewModel : ViewModel(){
    private val repository = ClienteRepository()

    private val _clientesOriginales = MutableLiveData<List<Cliente>>()
    private val _clientesFiltrados = MutableLiveData<List<Cliente>>()
    val clientesFiltrados: LiveData<List<Cliente>> = _clientesFiltrados

    private var filtroEstado: String? = null
    private var textoBusqueda: String = ""

    fun crearCliente(
            name: String,
            dni: String,
            direccion: String,
            ciudad: String,
            telefono: String
    ){
        repository.crearCliente(name,dni,direccion,ciudad,telefono)
    }

    fun cargarClientes(){
        repository.escucharClientesActivos { lista ->
            _clientesOriginales.value = lista
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {

        var lista = _clientesOriginales.value ?: return

        lista = when (filtroEstado) {
            "pendientes" -> repository.filtrarPorPendientes(lista)
            "pagados" -> repository.filtrarPorPagados(lista)
            else -> lista
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
        filtroEstado = "pendientes"
        aplicarFiltros()
    }

    fun filtrarPagados() {
        filtroEstado = "pagados"
        aplicarFiltros()
    }

    fun limpiarFiltroEstado() {
        filtroEstado = null
        aplicarFiltros()
    }
}