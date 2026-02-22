package com.estilo.estilo_al_paso.data.model

data class Envio(
    val idEnvio: String = "",

    val clienteId: String = "",
    val paqueteId: String = "",

    val nombreCliente: String = "",
    val dniCliente: String = "",
    val telefonoCliente: String = "",
    val ciudadCliente: String = "",
    val direccionCliente: String = "",

    val tipoEnvio: TipoEnvio = TipoEnvio.delivery,

    val totalPrendas: Int = 0,
    val totalPendiente: Double = 0.0,

    val estadoEnvio: EstadoEnvio = EstadoEnvio.programado,

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaEnvioReal: Long? = null
) {
    enum class TipoEnvio {
        delivery,
        encomienda
    }

    enum class EstadoEnvio {
        programado,
        cancelado,
        completado
    }
}


