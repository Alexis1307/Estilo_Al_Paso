package com.estilo.estilo_al_paso.data.model

data class Paquete(
    val idPaquete: String = "",

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaEnvio: Long? = null,

    val estadoPaquete: EstadoPaquete = EstadoPaquete.activo,

    val totalPrendas: Int = 0,
    val totalPagado: Double = 0.0,
    val totalPendiente: Double = 0.0
) {
    enum class EstadoPaquete {
        activo,
        enviado
    }
}