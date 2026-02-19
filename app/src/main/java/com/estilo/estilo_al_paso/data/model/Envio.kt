package com.estilo.estilo_al_paso.data.model

data class Envio(
    val idEnvio: String = "",
    val idPaquete: String = "",
    val idCliente: String = "",

    val nombreCliente: String = "",
    val dniCliente: String = "",
    val telefonoCliente: String = "",
    val ciudadCliente: String = "",
    val direccionCliente: String = "",
    val totalPrendas: Int = 0,

    val tipoEnvio: TipoEnvio = TipoEnvio.delivery,
    val estadoEnvio: EstadoEnvio = EstadoEnvio.programado,

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


