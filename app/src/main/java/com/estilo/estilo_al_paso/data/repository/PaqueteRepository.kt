package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Envio
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.model.Prenda
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PaqueteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val clientesRef = db.collection("clientes")
    private fun paquetesRef(clienteId: String) =
        clientesRef.document(clienteId).collection("paquetes")
    private fun prendasRef(clienteId: String, paqueteId: String) =
        paquetesRef(clienteId)
            .document(paqueteId)
            .collection("prendas")

    private val estadisticasRepository = EstadisticasRepository()

    private val statsRef =
        FirebaseFirestore.getInstance()
            .collection("estadisticas_global")
            .document("resumen")

    fun obtenerPaqueteActivo(
        clienteId: String,
        paqueteActivoId: String?,
        onResult: (Paquete?) -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (paqueteActivoId == null) {
            onResult(null)
            return
        }

        paquetesRef(clienteId)
            .document(paqueteActivoId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject(Paquete::class.java))
            }
            .addOnFailureListener { onError(it) }
    }

    fun guardarPaqueteCompleto(
        cliente: Cliente,
        prendas: List<Prenda>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val clienteDoc = clientesRef.document(cliente.idCliente)
        val estadisticasRepo = EstadisticasRepository()

        db.runTransaction { transaction ->

            val clienteSnapshot = transaction.get(clienteDoc)
            val clienteActual = clienteSnapshot.toObject(Cliente::class.java)
                ?: throw Exception("Cliente no encontrado")

            val paqueteActivoId = clienteActual.paqueteActivoId

            val totalPendiente = prendas
                .filter { it.estadoPago == Prenda.EstadoPago.pendiente }
                .sumOf { it.precioPrenda }

            val totalPagado = prendas
                .filter { it.estadoPago == Prenda.EstadoPago.pagado }
                .sumOf { it.precioPrenda }

            val totalPrendas = prendas.size

            val paqueteParaEstadisticas: Paquete

            if (paqueteActivoId != null) {
                val paqueteDoc = paquetesRef(cliente.idCliente).document(paqueteActivoId)
                val paqueteSnapshot = transaction.get(paqueteDoc)
                val paqueteActual = paqueteSnapshot.toObject(Paquete::class.java)
                    ?: throw Exception("Paquete no encontrado")

                transaction.update(
                    paqueteDoc,
                    mapOf(
                        "totalPrendas" to (paqueteActual.totalPrendas + prendas.size),
                        "totalPagado" to (paqueteActual.totalPagado + totalPagado),
                        "totalPendiente" to (paqueteActual.totalPendiente + totalPendiente)
                    )
                )

                prendas.forEach { prenda ->
                    val prendaDoc = prendasRef(cliente.idCliente, paqueteActivoId).document()
                    transaction.set(prendaDoc, prenda.copy(idPrenda = prendaDoc.id))
                }

                transaction.update(
                    clientesRef.document(cliente.idCliente),
                    "paqueteSeleccionado",
                    paqueteActual.copy(
                        totalPrendas = paqueteActual.totalPrendas + prendas.size,
                        totalPagado = paqueteActual.totalPagado + totalPagado,
                        totalPendiente = paqueteActual.totalPendiente + totalPendiente
                    )
                )

                paqueteParaEstadisticas = paqueteActual.copy(
                    totalPrendas = paqueteActual.totalPrendas + totalPrendas,
                    totalPagado = paqueteActual.totalPagado + totalPagado,
                    totalPendiente = paqueteActual.totalPendiente + totalPendiente
                )

            } else {

                val nuevoPaqueteDoc = paquetesRef(cliente.idCliente).document()

                val nuevoPaquete = Paquete(
                    idPaquete = nuevoPaqueteDoc.id,
                    totalPrendas = totalPrendas,
                    totalPagado = totalPagado,
                    totalPendiente = totalPendiente,
                    estadoPaquete = Paquete.EstadoPaquete.activo
                )

                transaction.set(nuevoPaqueteDoc, nuevoPaquete)

                prendas.forEach { prenda ->
                    val prendaDoc = prendasRef(cliente.idCliente, nuevoPaqueteDoc.id).document()
                    transaction.set(
                        prendaDoc,
                        prenda.copy(idPrenda = prendaDoc.id)
                    )
                }

                transaction.update(clienteDoc, mapOf(
                    "paqueteActivoId" to nuevoPaqueteDoc.id,
                    "paqueteSeleccionado" to nuevoPaquete
                ))

                transaction.update(statsRef, "totalPaquetesActivos", FieldValue.increment(1))

                paqueteParaEstadisticas = nuevoPaquete
            }

            if (totalPendiente > 0) {
                transaction.update(
                    clienteDoc,
                    "deudaTotal",
                    FieldValue.increment(totalPendiente)
                )
            }

            paqueteParaEstadisticas

        }.addOnSuccessListener {
            estadisticasRepo.actualizarPorPaquete(prendas)
            onSuccess()
        }

    }

    fun obtenerPrendasPorPaquete(
        clienteId: String,
        paqueteId: String,
        onResult: (List<Prenda>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        prendasRef(clienteId, paqueteId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Prenda::class.java))
            }
            .addOnFailureListener { onError(it) }
    }

    fun confirmarEnvioContraEntrega(
        cliente: Cliente,
        paquete: Paquete,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val clienteDoc = clientesRef.document(cliente.idCliente)
        val paqueteDoc = paquetesRef(cliente.idCliente).document(paquete.idPaquete)
        val envioDoc = db.collection("envios").document()

        db.runTransaction { transaction ->

            val paqueteSnapshot = transaction.get(paqueteDoc)
            val paqueteActual = paqueteSnapshot.toObject(Paquete::class.java)
                ?: throw Exception("Paquete no encontrado")

            if (paqueteActual.estadoPaquete != Paquete.EstadoPaquete.activo) {
                throw Exception("El paquete no está activo")
            }

            transaction.update(
                paqueteDoc,
                mapOf(
                    "fechaEnvio" to System.currentTimeMillis()
                )
            )

            val envio = Envio(
                idEnvio = envioDoc.id,
                clienteId = cliente.idCliente,
                paqueteId = paqueteActual.idPaquete,
                nombreCliente = cliente.nameCliente,
                dniCliente = cliente.dniCliente,
                telefonoCliente = cliente.telefonoCliente,
                ciudadCliente = cliente.ciudadCliente,
                direccionCliente = cliente.direccionCliente,
                totalPrendas = paqueteActual.totalPrendas,
                totalPendiente = paqueteActual.totalPendiente,
                estadoEnvio = Envio.EstadoEnvio.programado
            )

            transaction.set(envioDoc, envio)

            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerHistorialPaquetes(
        clienteId: String,
        onResult: (List<Paquete>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        paquetesRef(clienteId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Paquete::class.java))
            }
            .addOnFailureListener { onError(it) }
    }

    fun marcarTodoComoPagado(
        clienteId: String,
        paqueteId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val clienteDoc = clientesRef.document(clienteId)
        val paqueteDoc = paquetesRef(clienteId).document(paqueteId)
        val prendasCollection = prendasRef(clienteId, paqueteId)

        prendasCollection.get()
            .addOnSuccessListener { prendasSnapshot ->

                db.runTransaction { transaction ->

                    val paqueteSnapshot = transaction.get(paqueteDoc)
                    val paquete = paqueteSnapshot.toObject(Paquete::class.java)
                        ?: throw Exception("Paquete no encontrado")

                    if (paquete.totalPendiente <= 0.0) {
                        throw Exception("No hay deuda pendiente")
                    }

                    var totalPendienteReal = 0.0

                    for (document in prendasSnapshot.documents) {

                        val prenda = document.toObject(Prenda::class.java)

                        if (prenda != null &&
                            prenda.estadoPago == Prenda.EstadoPago.pendiente
                        ) {

                            totalPendienteReal += prenda.precioPrenda

                            transaction.update(
                                document.reference,
                                "estadoPago",
                                Prenda.EstadoPago.pagado.name
                            )
                        }
                    }

                    if (totalPendienteReal <= 0.0) {
                        throw Exception("No hay prendas pendientes")
                    }

                    estadisticasRepository.moverPendienteAVendido(
                        transaction,
                        totalPendienteReal
                    )

                    transaction.update(
                        paqueteDoc,
                        mapOf(
                            "totalPendiente" to 0.0,
                            "totalPagado" to paquete.totalPagado + totalPendienteReal
                        )
                    )


                    transaction.update(
                        clienteDoc,
                        mapOf(
                            "deudaTotal" to 0.0,
                            "paqueteActivoId" to paquete.idPaquete,
                            "paqueteSeleccionado" to paquete.copy(
                                totalPagado = paquete.totalPagado + totalPendienteReal,
                                totalPendiente = 0.0
                            )
                        )
                    )

                    null
                }
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }

            }
            .addOnFailureListener { onError(it) }
    }

    fun pagarPrendaIndividual(
        clienteId: String,
        paqueteId: String,
        prendaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val clienteDoc = clientesRef.document(clienteId)
        val paqueteDoc = paquetesRef(clienteId).document(paqueteId)
        val prendaDoc = prendasRef(clienteId, paqueteId).document(prendaId)

        db.runTransaction { transaction ->

            val prendaSnapshot = transaction.get(prendaDoc)
            val prenda = prendaSnapshot.toObject(Prenda::class.java)
                ?: throw Exception("Prenda no encontrada")

            if (prenda.estadoPago == Prenda.EstadoPago.pagado) {
                throw Exception("La prenda ya está pagada")
            }

            val precio = prenda.precioPrenda

            val paqueteSnapshot = transaction.get(paqueteDoc)
            val paquete = paqueteSnapshot.toObject(Paquete::class.java)
                ?: throw Exception("Paquete no encontrado")

            if (paquete.estadoPaquete != Paquete.EstadoPaquete.activo) {
                throw Exception("El paquete no está activo")
            }

            if (paquete.totalPendiente < precio) {
                throw Exception("Inconsistencia en totales")
            }

            transaction.update(
                prendaDoc,
                "estadoPago",
                Prenda.EstadoPago.pagado.name
            )

            transaction.update(
                paqueteDoc,
                mapOf(
                    "totalPendiente" to (paquete.totalPendiente - precio),
                    "totalPagado" to (paquete.totalPagado + precio)
                )
            )

            transaction.update(
                clienteDoc,
                "deudaTotal",
                FieldValue.increment(-precio)
            )

            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerUltimoPaquete(
        clienteId: String,
        onResult: (Paquete?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        clientesRef.document(clienteId)
            .collection("paquetes")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val paquete = snapshot.documents.firstOrNull()?.toObject(Paquete::class.java)
                onResult(paquete)
            }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerPaquetePorId(
        clienteId: String,
        paqueteId: String,
        onResult: (Paquete?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        paquetesRef(clienteId)
            .document(paqueteId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject(Paquete::class.java))
            }
            .addOnFailureListener { onError(it) }
    }
}