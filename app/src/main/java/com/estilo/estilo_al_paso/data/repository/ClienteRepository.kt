package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.remote.FirebaseService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



class ClienteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val clientesRef = db.collection("clientes")

    fun crearCliente(
        name: String,
        dni: String,
        direccion: String,
        ciudad: String,
        telefono: String,
        onSuccess: (Cliente) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nuevoDoc = clientesRef.document(dni)

        val cliente = Cliente(
            idCliente = dni,
            nameCliente = name,
            dniCliente = dni,
            direccionCliente = direccion,
            ciudadCliente = ciudad,
            telefonoCliente = telefono,
            estadoCliente = Cliente.EstadoCliente.activo,
            paqueteActivoId = null,
            deudaTotal = 0.0
        )

        nuevoDoc.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onError(Exception("Cliente con DNI $dni ya existe"))
            } else {
                nuevoDoc.set(cliente)
                    .addOnSuccessListener { onSuccess(cliente) }
                    .addOnFailureListener { onError(it) }            }
        }
    }

    fun obtenerClientePorId(
        idCliente: String,
        onSuccess: (Cliente?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        clientesRef.document(idCliente)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObject(Cliente::class.java))
            }
            .addOnFailureListener { onError(it) }
    }


    fun escucharClientesActivos(
        onResult: (List<Cliente>) -> Unit
    ) {

        clientesRef
            .whereEqualTo(
                "estadoCliente",
                Cliente.EstadoCliente.activo.name
            )
            .orderBy("deudaTotal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener

                val lista = snapshot?.toObjects(Cliente::class.java)
                    ?: emptyList()

                onResult(lista)
            }
    }

    fun actualizarCliente(
        cliente: Cliente,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        clientesRef.document(cliente.idCliente)
            .update(
                mapOf(
                    "nameCliente" to cliente.nameCliente,
                    "dniCliente" to cliente.dniCliente,
                    "direccionCliente" to cliente.direccionCliente,
                    "ciudadCliente" to cliente.ciudadCliente,
                    "telefonoCliente" to cliente.telefonoCliente
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun cambiarEstado(
        clienteId: String,
        nuevoEstado: Cliente.EstadoCliente,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        clientesRef.document(clienteId)
            .update("estadoCliente", nuevoEstado.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun actualizarDeuda(
        clienteId: String,
        monto: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        clientesRef.document(clienteId)
            .update("deudaTotal", FieldValue.increment(monto))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
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

    fun filtrarPorPendientes(lista: List<Cliente>): List<Cliente> {
        return lista.filter { it.deudaTotal > 0 && it.paqueteSeleccionado != null }
    }

    fun filtrarPorPagados(lista: List<Cliente>) =
        lista.filter { it.deudaTotal == 0.0 && it.paqueteSeleccionado?.estadoPaquete == Paquete.EstadoPaquete.activo }

    fun filtrarPorSinPrendas(lista: List<Cliente>) =
        lista.filter { it.paqueteSeleccionado == null || it.paqueteSeleccionado?.estadoPaquete == Paquete.EstadoPaquete.enviado }

    fun obtenerClientes(
        onSuccess: (List<Cliente>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("clientes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Cliente::class.java)
                onSuccess(lista)
            }
            .addOnFailureListener {
                onError(it)
            }
    }
}