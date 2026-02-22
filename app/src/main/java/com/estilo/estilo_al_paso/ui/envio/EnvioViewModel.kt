package com.estilo.estilo_al_paso.ui.envio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.Envio
import com.estilo.estilo_al_paso.data.repository.EnvioRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EnvioViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val clientesRef = db.collection("clientes")
    private val repository = EnvioRepository()

    private val _envios = MutableLiveData<List<Envio>>()
    val envios: LiveData<List<Envio>> = _envios

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var listaCompleta: List<Envio> = emptyList()

    private val _actualizarClientes = MutableLiveData<Unit>()
    val actualizarClientes: LiveData<Unit> = _actualizarClientes


    fun cargarEnvios() {
        _loading.value = true
        val db = FirebaseFirestore.getInstance()
        val clientesRef = db.collection("clientes")

        repository.obtenerEnviosProgramados(
            onResult = { lista ->
                val listaActualizada = mutableListOf<Envio>()
                val tasks = lista.map { envio ->
                    clientesRef.document(envio.clienteId).get()
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener { results ->
                        results.forEachIndexed { index, task ->
                            val snapshot = (task.result as DocumentSnapshot)
                            val deuda = snapshot.getDouble("deudaTotal") ?: 0.0
                            val envio = lista[index].copy(totalPendiente = deuda)
                            listaActualizada.add(envio)
                        }

                        listaCompleta = listaActualizada
                        _envios.value = listaActualizada.sortedByDescending { it.fechaCreacion }
                        _loading.value = false
                    }
                    .addOnFailureListener {
                        _error.value = "Error al actualizar envíos: ${it.message}"
                        _loading.value = false
                    }
            },
            onError = {
                _error.value = "Error al cargar envíos: ${it.message}"
                _loading.value = false
            }
        )
    }


    fun filtrarEnvios(query: String) {

        val texto = query.lowercase()

        if (texto.isEmpty()) {
            _envios.value = listaCompleta
        } else {
            val filtrada = listaCompleta.filter { envio ->
                envio.nombreCliente.lowercase().contains(texto) ||
                        envio.dniCliente.contains(texto)
            }
            _envios.value = filtrada
        }
    }


    fun confirmarEnvioDefinitivo(envio: Envio) {
        _loading.value = true
        repository.completarEnvioDefinitivo(envio,
            onSuccess = {
                _loading.value = false
                cargarEnvios()
            },
            onError = {
                _loading.value = false
                _error.value = it.message
            }
        )

        Log.d("ENVIO_DEBUG", "Confirmando envío: envioId=${envio.idEnvio}, paqueteId=${envio.paqueteId}, clienteId=${envio.clienteId}")
    }


    fun cancelarEnvio(envio: Envio) {

        _loading.value = true

        repository.cancelarEnvio(
            envio = envio,
            onSuccess = {
                cargarEnvios()
            },
            onError = {
                _error.value = it.message
                _loading.value = false
            }
        )
    }
}