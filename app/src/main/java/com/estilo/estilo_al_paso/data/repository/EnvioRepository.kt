package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.Envio
import com.google.firebase.firestore.FirebaseFirestore

class EnvioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val enviosRef = db.collection("envios")

    fun crearEnvio(
        envio: Envio,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        enviosRef.document(envio.idEnvio)
            .set(envio)
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
                    "fechaEnvioReal" to System.currentTimeMillis()
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
                        // Esto es lo que está fallando
                        doc.toObject(Envio::class.java)
                    } catch (e: Exception) {
                        // ESTO te dirá en el Logcat por qué falla el mapeo
                        android.util.Log.e("FIREBASE_ERROR", "Error mapeando documento ${doc.id}: ${e.message}")
                        null
                    }
                }
                onResult(lista)
            }
            .addOnFailureListener { onError(it) }
    }





}
