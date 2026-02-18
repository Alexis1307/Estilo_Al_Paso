package com.estilo.estilo_al_paso.data.model

data class Envio(
    val idEnvio: String = "",
    val idPaquete: String = "",
    val idCliente: String = "",
    val tipoEnvio: TipoEnvio = TipoEnvio.delivery,
    val estadoEnvio: EstadoEnvio = EstadoEnvio.programado,
    val fechaProgramada: Long = System.currentTimeMillis(),
    val fechaCompletado: Long? = null,
){
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

