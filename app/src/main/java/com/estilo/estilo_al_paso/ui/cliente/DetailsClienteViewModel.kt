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

    private val _historial = MutableLiveData<List<Paquete>>()
    val historial: LiveData<List<Paquete>> = _historial

    private val _cliente = MutableLiveData<Cliente>()
    val cliente: LiveData<Cliente> = _cliente

    private val _paqueteSeleccionado = MutableLiveData<Paquete?>()
    val paqueteSeleccionado: LiveData<Paquete?> = _paqueteSeleccionado

    private val _prendas = MutableLiveData<List<Prenda>>()
    val prendas: LiveData<List<Prenda>> = _prendas

    private val _totalPrendas = MutableLiveData(0)
    val totalPrendas: LiveData<Int> = _totalPrendas

    private val _totalPagado = MutableLiveData(0.0)
    val totalPagado: LiveData<Double> = _totalPagado

    private val _totalDeuda = MutableLiveData(0.0)
    val totalDeuda: LiveData<Double> = _totalDeuda

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _envioConfirmado = MutableLiveData(false)
    val envioConfirmado: LiveData<Boolean> = _envioConfirmado

    private val _actualizarClientes = MutableLiveData<Unit>()
    val actualizarClientes: LiveData<Unit> = _actualizarClientes

    fun cargarDetalleCliente(clienteId: String) {
        clienteRepository.obtenerClientePorId(clienteId,
            onSuccess = { cliente ->
                if (cliente == null) {
                    _error.value = "Cliente no encontrado"
                    return@obtenerClientePorId
                }

                _cliente.value = cliente

                val paqueteActivoId = cliente.paqueteActivoId

                if (!paqueteActivoId.isNullOrEmpty()) {
                    cargarPaquete(cliente.idCliente, paqueteActivoId)
                } else {
                    _paqueteSeleccionado.value = null
                    _prendas.value = emptyList()
                    _totalPrendas.value = 0
                    _totalPagado.value = 0.0
                    _totalDeuda.value = 0.0
                }
            },
            onError = { _error.value = "No se pudo cargar el cliente" }
        )
    }

    private fun cargarPaquete(
        clienteId: String,
        paqueteId: String,
        onPaqueteCargado: (Paquete?) -> Unit = {}
    ) {
        paqueteRepository.obtenerPaquetePorId(clienteId, paqueteId,
            onResult = { paquete ->
                _paqueteSeleccionado.value = paquete
                cargarPrendas(paquete)
                onPaqueteCargado(paquete)
            },
            onError = {
                _error.value = "No se pudo cargar el paquete"
                onPaqueteCargado(null)
            }
        )
    }

    private fun cargarPrendas(paquete: Paquete?) {
        if (paquete == null) {
            _prendas.value = emptyList()
            _totalPrendas.value = 0
            _totalDeuda.value = 0.0
            _totalPagado.value = 0.0
            return
        }

        paqueteRepository.obtenerPrendasPorPaquete(
            _cliente.value!!.idCliente,
            paquete.idPaquete,
            onResult = { lista ->
                _prendas.value = lista
                _totalPrendas.value = lista.size
                _totalDeuda.value = lista.sumOf { if (it.estadoPago == Prenda.EstadoPago.pendiente) it.precioPrenda else 0.0 }
                _totalPagado.value = lista.sumOf { if (it.estadoPago == Prenda.EstadoPago.pagado) it.precioPrenda else 0.0 }
            },
            onError = { _error.value = "No se pudieron cargar las prendas" }
        )
    }

    fun marcarTodoComoPagado() {
        val cliente = _cliente.value ?: return
        val paquete = _paqueteSeleccionado.value ?: return

        _loading.value = true
        paqueteRepository.marcarTodoComoPagado(cliente.idCliente, paquete.idPaquete,
            onSuccess = {
                // Recargar el paquete y las prendas
                cargarPaquete(cliente.idCliente, paquete.idPaquete) { paqueteActualizado ->
                    _paqueteSeleccionado.value = paqueteActualizado
                    cliente.paqueteSeleccionado = paqueteActualizado
                    cliente.paqueteActivoId = paqueteActualizado?.idPaquete
                    _cliente.value = cliente
                    _loading.value = false

                    _actualizarClientes.value = Unit
                }
            },
            onError = {
                _loading.value = false
                _error.value = it.message
            }
        )
    }

    fun confirmarEnvio() {
        val cliente = _cliente.value ?: return
        val paquete = _paqueteSeleccionado.value ?: return

        paqueteRepository.confirmarEnvioContraEntrega(cliente, paquete,
            onSuccess = {
                _envioConfirmado.value = true
            },
            onError = { _error.value = it.message }
        )
    }

    fun cargarHistorial(clienteId: String) {
        paqueteRepository.obtenerHistorialPaquetes(
            clienteId,
            onResult = { lista ->
                _historial.value = lista
            },
            onError = { _error.value = "No se pudo cargar el historial" }
        )
    }
}