package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.model.Prenda
import com.google.firebase.firestore.FirebaseFirestore

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
            .whereEqualTo("estadoPaquete", Paquete.EstadoPaquete.activo.name)
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

}