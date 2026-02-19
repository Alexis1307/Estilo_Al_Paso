package com.estilo.estilo_al_paso.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.model.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PaqueteRepository {
    private val db = FirebaseFirestore.getInstance()

    private val clientesRef = db.collection("clientes")
    private val paquetesRef = db.collection("paquetes")
    private val prendasRef = db.collection("prendas")

    fun obtenerPaqueteActivo(
        clienteId: String,
        onResult: (Paquete?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        paquetesRef
            .whereEqualTo("idCliente", clienteId)
            .whereIn(
                "estadoPaquete",
                listOf(
                    Paquete.EstadoPaquete.activo.name,
                    Paquete.EstadoPaquete.pendienteEnvio.name
                )
            )
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val paquete = result.documents[0].toObject(Paquete::class.java)
                    onResult(paquete)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun crearPaquete(
        cliente: Cliente,
        prendas: List<Prenda>,
        onSuccess: (Paquete) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val paqueteRef = paquetesRef.document()
        val paqueteId = paqueteRef.id

        val totalPagar = prendas.sumOf { it.precioPrenda }

        val paquete = Paquete(
            idPaquete = paqueteId,
            idCliente = cliente.idCliente,
            fechaCreacion = System.currentTimeMillis(),
            totalPrendas = prendas.size,
            totalPagar = totalPagar,
            estadoPaquete = Paquete.EstadoPaquete.activo
        )

        paqueteRef.set(paquete)
            .addOnSuccessListener { onSuccess(paquete) }
            .addOnFailureListener { onError(it) }
    }

    fun guardarPrendas(
        paqueteId: String,
        prendas: List<Prenda>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val batch = db.batch()

        prendas.forEach { prenda ->
            val prendaRef = prendasRef.document()
            val prendaConPaquete = prenda.copy(
                idPrenda = prendaRef.id,
                idPaquete = paqueteId
            )
            batch.set(prendaRef, prendaConPaquete)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerPaquetePorId(
        idPaquete: String,
        onSuccess: (Paquete?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("paquetes")
            .document(idPaquete)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObject(Paquete::class.java))
            }
            .addOnFailureListener { onError(it) }
    }


    fun actualizarDeudaCliente(
        cliente: Cliente,
        montoPendiente: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nuevaDeuda = cliente.deudaTotal + montoPendiente

        clientesRef.document(cliente.idCliente)
            .update("deudaTotal", nuevaDeuda)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun guardarPaqueteCompleto(
        cliente: Cliente,
        prendas: List<Prenda>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        obtenerPaqueteActivo(
            cliente.idCliente,
            onResult = { paqueteActivo ->

                val totalPendiente = prendas
                    .filter { it.estadoPago == Prenda.EstadoPago.pendiente }
                    .sumOf { it.precioPrenda }

                if (paqueteActivo != null) {
                    guardarPrendas(
                        paqueteActivo.idPaquete,
                        prendas,
                        onSuccess = {
                            if (totalPendiente > 0) {
                                actualizarDeudaCliente(
                                    cliente,
                                    totalPendiente,
                                    onSuccess = { onSuccess() },
                                    onError = { onError(it) }
                                )
                            } else {
                                onSuccess()
                            }
                        },
                        onError = { onError(it) }
                    )

                } else {
                    crearPaquete(
                        cliente,
                        prendas,
                        onSuccess = { nuevoPaquete ->

                            guardarPrendas(
                                nuevoPaquete.idPaquete,
                                prendas,
                                onSuccess = {
                                    if (totalPendiente > 0) {
                                        actualizarDeudaCliente(
                                            cliente,
                                            totalPendiente,
                                            onSuccess = { onSuccess() },
                                            onError = { onError(it) }
                                        )
                                    } else {
                                        onSuccess()
                                    }
                                },
                                onError = { onError(it) }
                            )

                        },
                        onError = { onError(it) }
                    )
                }

            },
            onError = { onError(it) }
        )
    }

    fun verificarSiTienePendientes(
        idPaquete: String,
        onResult: (Boolean) -> Unit
    ) {

        db.collection("prendas")
            .whereEqualTo("idPaquete", idPaquete)
            .whereEqualTo("estadoPago", "pendiente")
            .get()
            .addOnSuccessListener { snapshot ->

                val tienePendientes = snapshot.documents.isNotEmpty()
                onResult(tienePendientes)
            }
    }

    fun obtenerPrendasPorPaquete(
        idPaquete: String,
        onResult: (List<Prenda>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        prendasRef
            .whereEqualTo("idPaquete", idPaquete)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.toObjects(Prenda::class.java)
                onResult(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun cambiarEstadoPaquete(
        idPaquete: String,
        nuevoEstado: Paquete.EstadoPaquete,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        paquetesRef
            .document(idPaquete)
            .update("estadoPaquete", nuevoEstado.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun confirmarEnvio(
        paquete: Paquete,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val envioRef = db.collection("envios").document()

        val envioData = hashMapOf(
            "idEnvio" to envioRef.id,
            "idPaquete" to paquete.idPaquete,
            "idCliente" to paquete.idCliente,
            "fechaEnvio" to System.currentTimeMillis(),
            "estadoEnvio" to "pendiente"
        )

        db.runBatch { batch ->

            val paqueteRef = paquetesRef.document(paquete.idPaquete)
            batch.update(paqueteRef, "estadoPaquete", Paquete.EstadoPaquete.pendienteEnvio.name)

            batch.set(envioRef, envioData)

        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerHistorialPaquetes(
        clienteId: String,
        onResult: (List<Paquete>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        paquetesRef
            .whereEqualTo("idCliente", clienteId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.toObjects(Paquete::class.java)
                onResult(lista)
            }
            .addOnFailureListener { onError(it) }
    }

}