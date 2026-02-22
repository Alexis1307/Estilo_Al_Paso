package com.estilo.estilo_al_paso.data.repository

import com.estilo.estilo_al_paso.data.model.EstadisticasGenerales
import com.estilo.estilo_al_paso.data.model.Paquete
import com.estilo.estilo_al_paso.data.model.Prenda
import com.estilo.estilo_al_paso.data.remote.FirebaseService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction

class EstadisticasRepository {
    private val db = FirebaseFirestore.getInstance()
    private val statsRef = db.collection("estadisticas_global").document("resumen")

    fun inicializarEstadisticas() {
        statsRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                statsRef.set(EstadisticasGenerales())
            }
        }
    }

    fun actualizarPorPaquete(
        prendas: List<Prenda>
    ) {
        db.runTransaction { transaction ->

            val snapshot = transaction.get(statsRef)
            val stats = snapshot.toObject(EstadisticasGenerales::class.java)
                ?: EstadisticasGenerales()


            val totalPrendasVendidas =
                stats.totalPrendasVendidas + prendas.size

            val montoTotalVendido =
                stats.montoTotalVendido + prendas
                    .filter { it.estadoPago == Prenda.EstadoPago.pagado }
                    .sumOf { it.precioPrenda }

            val montoTotalPendiente =
                stats.montoTotalPendiente + prendas
                    .filter { it.estadoPago == Prenda.EstadoPago.pendiente }
                    .sumOf { it.precioPrenda }

            transaction.update(statsRef, mapOf(
                "totalPrendasVendidas" to totalPrendasVendidas,
                "montoTotalVendido" to montoTotalVendido,
                "montoTotalPendiente" to montoTotalPendiente
            ))

            null
        }
    }

    fun moverPendienteAVendido(
        transaction: Transaction,
        monto: Double
    ) {
        if (monto <= 0) return

        transaction.update(
            statsRef,
            "montoTotalVendido",
            FieldValue.increment(monto)
        )

        transaction.update(
            statsRef,
            "montoTotalPendiente",
            FieldValue.increment(-monto)
        )
    }
}
