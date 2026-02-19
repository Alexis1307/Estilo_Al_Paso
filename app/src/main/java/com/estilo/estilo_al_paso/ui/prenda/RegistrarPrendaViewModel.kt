package com.estilo.estilo_al_paso.ui.prenda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.data.repository.ClienteRepository
import com.estilo.estilo_al_paso.data.repository.PaqueteRepository

class RegistrarPrendaViewModel : ViewModel(){
    private val repository = PaqueteRepository()
    private val clienteRepository = ClienteRepository()

    private val _clienteSeleccionado = MutableLiveData<Cliente?>()
    val clienteSeleccionado: LiveData<Cliente?> = _clienteSeleccionado

    private val _prendas = MutableLiveData<MutableList<Prenda>>(mutableListOf())
    val prendas: LiveData<MutableList<Prenda>> = _prendas

    private val _totalPagar = MutableLiveData<Double>(0.0)
    val totalPagar: LiveData<Double> = _totalPagar

    private val _totalPendiente = MutableLiveData<Double>(0.0)
    val totalPendiente: LiveData<Double> = _totalPendiente

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> = _guardadoExitoso


    fun seleccionarCliente(cliente: Cliente) {
        _clienteSeleccionado.value = cliente
    }

    fun agregarPrenda(prenda: Prenda) {
        val listaActual = _prendas.value ?: mutableListOf()
        listaActual.add(prenda)
        _prendas.value = listaActual
        recalcularTotales()
    }

    fun eliminarPrenda(prenda: Prenda) {
        val listaActual = _prendas.value ?: return
        listaActual.remove(prenda)
        _prendas.value = listaActual
        recalcularTotales()
    }

    private fun recalcularTotales() {
        val lista = _prendas.value ?: return

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
            cliente,
            listaPrendas,
            onSuccess = {

                val tienePendientes = listaPrendas.any {
                    it.estadoPago == Prenda.EstadoPago.pendiente
                }

                clienteRepository.actualizarTienePendientes(
                    clienteId = cliente.idCliente,
                    tienePendientes = tienePendientes
                )

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

    private fun limpiarDatos() {
        _prendas.value = mutableListOf()
        _totalPagar.value = 0.0
        _totalPendiente.value = 0.0
    }
}