package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.remote.FirebaseService
import com.google.firebase.firestore.FieldValue

class EstadisticasRepository {
    private val db = FirebaseService.db

    fun actualizarEstadisticasGlobal(
        montoVendido: Double,
        montoPendiente: Double,
        esPendiente: Boolean
    ) {

        val docRef = db.collection("estadisticas_global")
            .document("resumen")

        val updates = mutableMapOf<String, Any>(
            "totalVendidoGeneral" to FieldValue.increment(montoVendido),
            "totalVendidoPendiente" to FieldValue.increment(montoPendiente)
        )

        if (esPendiente) {
            updates["totalPrendasPendientesGeneral"] = FieldValue.increment(1)
        } else {
            updates["totalPrendasVendidasGeneral"] = FieldValue.increment(1)
        }

        docRef.update(updates)
    }
}