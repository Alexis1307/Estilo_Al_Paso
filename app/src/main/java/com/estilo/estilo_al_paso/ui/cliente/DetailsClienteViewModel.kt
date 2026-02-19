package com.estilo.estilo_al_paso.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.repository.ClienteRepository

class DetailsClienteViewModel : ViewModel() {

    private val repository = ClienteRepository()

    private val _cliente = MutableLiveData<Cliente>()
    val cliente: LiveData<Cliente> = _cliente

    fun cargarDetalleCliente(idCliente: String) {
        repository.obtenerClientePorId(idCliente) { cliente ->
            _cliente.postValue(cliente)
        }
    }
}