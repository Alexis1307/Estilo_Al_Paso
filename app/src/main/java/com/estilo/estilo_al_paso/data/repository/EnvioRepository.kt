package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Envio
import com.google.firebase.firestore.FirebaseFirestore

class EnvioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val enviosRef = db.collection("envios")

    fun crearEnvio(
        idCliente: String,
        idPaquete: String,
        tipoEnvio: Envio.TipoEnvio,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val idGenerado = enviosRef.document().id

        val envioMap = hashMapOf(
            "idEnvio" to idGenerado,
            "idCliente" to idCliente,
            "idPaquete" to idPaquete,
            "tipoEnvio" to tipoEnvio.name,
            "estadoEnvio" to Envio.EstadoEnvio.programado.name,
            "fechaProgramada" to System.currentTimeMillis(),
            "fechaCompletado" to null
        )

        enviosRef.document(idGenerado)
            .set(envioMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun completarEnvio(
        idEnvio: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        enviosRef.document(idEnvio)
            .update(
                mapOf(
                    "estadoEnvio" to Envio.EstadoEnvio.completado.name,
                    "fechaCompletado" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun cancelarEnvio(
        idEnvio: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        enviosRef.document(idEnvio)
            .update("estadoEnvio", Envio.EstadoEnvio.cancelado.name)
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
                        Envio(
                            idEnvio = doc.getString("idEnvio") ?: "",
                            idCliente = doc.getString("idCliente") ?: "",
                            idPaquete = doc.getString("idPaquete") ?: "",
                            tipoEnvio = Envio.TipoEnvio.valueOf(
                                doc.getString("tipoEnvio") ?: "delivery"
                            ),
                            estadoEnvio = Envio.EstadoEnvio.valueOf(
                                doc.getString("estadoEnvio") ?: "programado"
                            ),
                            fechaProgramada = doc.getLong("fechaProgramada") ?: 0L,
                            fechaCompletado = doc.getLong("fechaCompletado")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                onResult(lista)
            }
            .addOnFailureListener { onError(it) }
    }
}
