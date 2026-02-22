package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Cliente
import com.estilo.estilo_al_paso.data.model.Envio
import com.estilo.estilo_al_paso.data.model.Paquete
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EnvioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val enviosRef = db.collection("envios")
    private val clientesRef = db.collection("clientes")
    private fun paquetesRef(clienteId: String) = db.collection("clientes")
        .document(clienteId)
        .collection("paquetes")

    private val statsRef =
        FirebaseFirestore.getInstance()
            .collection("estadisticas_global")
            .document("resumen")

    fun completarEnvioDefinitivo(
        envio: Envio,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val envioDoc = enviosRef.document(envio.idEnvio)
        val clienteDoc = clientesRef.document(envio.clienteId)
        val paqueteDoc = paquetesRef(envio.clienteId).document(envio.paqueteId)

        db.runTransaction { transaction ->

            val envioSnapshot = transaction.get(envioDoc)
            val clienteSnapshot = transaction.get(clienteDoc)
            val paqueteSnapshot = transaction.get(paqueteDoc)

            val envioActual = envioSnapshot.toObject(Envio::class.java)
                ?: throw Exception("Envío no encontrado")

            if (envioActual.estadoEnvio != Envio.EstadoEnvio.programado) {
                throw Exception("Solo se pueden completar envíos programados")
            }

            val paquete = paqueteSnapshot.toObject(Paquete::class.java)
                ?: throw Exception("Paquete no encontrado")

            transaction.update(envioDoc, mapOf(
                "estadoEnvio" to Envio.EstadoEnvio.completado.name,
                "fechaEnvioReal" to System.currentTimeMillis()
            ))

            transaction.update(paqueteDoc, mapOf(
                "estadoPaquete" to Paquete.EstadoPaquete.enviado.name
            ))

            transaction.update(clienteDoc,
                "paqueteSeleccionado", paquete.copy(
                    estadoPaquete = Paquete.EstadoPaquete.enviado
                ))


            transaction.update(clienteDoc, "paqueteActivoId", null)

            if (paquete.estadoPaquete == Paquete.EstadoPaquete.activo) {
                transaction.update(
                    statsRef,
                    "totalPaquetesActivos",
                    FieldValue.increment(-1)
                )
            }

            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }

    }

    fun cancelarEnvio(
        envio: Envio,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val envioDoc = enviosRef.document(envio.idEnvio)
        val clienteDoc = db.collection("clientes").document(envio.clienteId)
        val paqueteDoc = clienteDoc
            .collection("paquetes")
            .document(envio.paqueteId)

        db.runTransaction { transaction ->

            val envioSnapshot = transaction.get(envioDoc)
            val envioActual = envioSnapshot.toObject(Envio::class.java)
                ?: throw Exception("Envío no encontrado")

            if (envioActual.estadoEnvio != Envio.EstadoEnvio.programado) {
                throw Exception("Solo se pueden cancelar envíos programados")
            }

            transaction.update(
                envioDoc,
                "estadoEnvio",
                Envio.EstadoEnvio.cancelado.name
            )

            transaction.update(
                paqueteDoc,
                "estadoPaquete",
                Paquete.EstadoPaquete.activo.name
            )

            transaction.update(
                clienteDoc,
                "paqueteActivoId",
                envio.paqueteId
            )

            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerEnviosProgramados(
        onResult: (List<Envio>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        enviosRef
            .whereEqualTo("estadoEnvio", Envio.EstadoEnvio.programado.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Envio::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("FIREBASE_ERROR", "Error mapeando documento ${doc.id}: ${e.message}")
                        null
                    }
                }
                onResult(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun escucharEnviosProgramados(
        onResult: (List<Envio>) -> Unit
    ) {

        enviosRef
            .whereEqualTo(
                "estadoEnvio",
                Envio.EstadoEnvio.programado.name
            )
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener

                val lista = snapshot?.toObjects(Envio::class.java)
                    ?: emptyList()

                onResult(lista)
            }
    }

    fun obtenerEnviosPorCliente(
        clienteId: String,
        onResult: (List<Envio>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        enviosRef
            .whereEqualTo("clienteId", clienteId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Envio::class.java))
            }
            .addOnFailureListener { onError(it) }
    }
}
