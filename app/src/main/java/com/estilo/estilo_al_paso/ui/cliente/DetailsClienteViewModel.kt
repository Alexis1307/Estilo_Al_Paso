package com.estilo.estilo_al_paso.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.data.repository.ClienteRepository
import com.estilo.estilo_al_paso.data.repository.PaqueteRepository

class DetailsClienteViewModel : ViewModel() {

    private val clienteRepository = ClienteRepository()
    private val paqueteRepository = PaqueteRepository()

    private val _cliente = MutableLiveData<Cliente>()
    val cliente: LiveData<Cliente> = _cliente

    private val _paqueteActivo = MutableLiveData<Paquete?>()
    val paqueteActivo: LiveData<Paquete?> = _paqueteActivo

    private val _prendas = MutableLiveData<List<Prenda>>()
    val prendas: LiveData<List<Prenda>> = _prendas

    private val _envioConfirmado = MutableLiveData<Boolean>()
    val envioConfirmado: LiveData<Boolean> = _envioConfirmado

    private val _historial = MutableLiveData<List<Paquete>>()
    val historial: LiveData<List<Paquete>> = _historial

    private val _totalPrendas = MutableLiveData<Int>()
    val totalPrendas: LiveData<Int> = _totalPrendas

    private val _totalPagado = MutableLiveData<Double>()
    val totalPagado: LiveData<Double> = _totalPagado

    private val _totalDeuda = MutableLiveData<Double>()
    val totalDeuda: LiveData<Double> = _totalDeuda



    fun cargarDetalleCliente(idCliente: String) {
        clienteRepository.obtenerClientePorId(
            idCliente,
            onSuccess = { it?.let { _cliente.value = it } },
            onError = {}
        )
    }

    fun cargarPaqueteActivo(idCliente: String) {

        paqueteRepository.obtenerPaqueteActivo(
            idCliente,
            onResult = { paquete ->

                _paqueteActivo.value = paquete

                paquete?.let {
                    cargarPrendas(it.idPaquete)
                }
            },
            onError = {}
        )
    }

    private fun cargarPrendas(idPaquete: String) {

        paqueteRepository.obtenerPrendasPorPaquete(
            idPaquete,
            onResult = { lista ->

                _prendas.value = lista

                _totalPrendas.value = lista.size

                val pagado = lista
                    .filter { it.estadoPago == Prenda.EstadoPago.pagado }
                    .sumOf { it.precioPrenda }

                val deuda = lista
                    .filter { it.estadoPago == Prenda.EstadoPago.pendiente }
                    .sumOf { it.precioPrenda }

                _totalPagado.value = pagado
                _totalDeuda.value = deuda
            }, onError = {}
        )
    }

    fun confirmarEnvio() {

        val paquete = _paqueteActivo.value ?: return

        if (_prendas.value.isNullOrEmpty()) return

        paqueteRepository.confirmarEnvio(
            paquete,
            onSuccess = {
                _envioConfirmado.value = true
            },
            onError = {}
        )
    }

    fun cargarHistorial(clienteId: String) {

        paqueteRepository.obtenerHistorialPaquetes(
            clienteId,
            onResult = { lista ->
                _historial.value = lista
            },
            onError = {}
        )
    }

}