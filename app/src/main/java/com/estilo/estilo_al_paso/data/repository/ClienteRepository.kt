package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.remote.FirebaseService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class ClienteRepository {
    private val db = FirebaseService.db
    private val clientesRef = db.collection("clientes")

    fun crearCliente(
        name: String,
        dni: String,
        direccion: String,
        ciudad: String,
        telefono: String
    ) {

        val idGenerado = clientesRef.document().id

        val cliente = Cliente(
            idCliente = idGenerado,
            nameCliente = name,
            dniCliente = dni,
            direccionCliente = direccion,
            ciudadCliente = ciudad,
            telefonoCliente = telefono
        )

        clientesRef.document(idGenerado).set(cliente)
    }

    fun obtenerClientesActivos() =
        clientesRef.whereEqualTo(
            "estadoCliente",
            Cliente.EstadoCliente.activo.name
        )

    fun escucharClientesActivos(onResult: (List<Cliente>) -> Unit) {

        clientesRef
            .whereEqualTo(
                "estadoCliente",
                Cliente.EstadoCliente.activo.name
            )
            .orderBy("deudaTotal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener

                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(Cliente::class.java)
                } ?: emptyList()

                onResult(lista)
            }
    }

    fun cambiarEstado(clienteId: String, nuevoEstado: Cliente.EstadoCliente) {
        clientesRef
            .document(clienteId)
            .update("estadoCliente", nuevoEstado)
    }

    fun actualizarDeuda(clienteId: String, monto: Double) {
        clientesRef
            .document(clienteId)
            .update("deudaTotal", FieldValue.increment(monto))
    }

    fun actualizarTienePendientes(
        clienteId: String,
        tienePendientes: Boolean
    ) {
        clientesRef
            .document(clienteId)
            .update("tienePendientes", tienePendientes)
    }

    fun filtrarPorPendientes(lista: List<Cliente>): List<Cliente> {
        return lista.filter { it.tienePendientes }
    }

    fun filtrarPorPagados(lista: List<Cliente>): List<Cliente> {
        return lista.filter { !it.tienePendientes }
    }

    fun filtrarPorNombreODni(
        lista: List<Cliente>,
        query: String
    ): List<Cliente> {

        val texto = query.lowercase().trim()

        return lista.filter {
            it.nameCliente.lowercase().contains(texto) ||
                    it.dniCliente.contains(texto)
        }
    }
}