package com.estilo.estilo_al_paso.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.repository.ClienteRepository

class ClienteViewModel : ViewModel(){
    private val repository = ClienteRepository()
    private val _clientes = MutableLiveData<List<Cliente>>()

    val clientes : LiveData<List<Cliente>> = _clientes

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
            _clientes.postValue(lista)
        }
    }


}