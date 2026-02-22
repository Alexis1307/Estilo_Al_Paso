package com.estilo.estilo_al_paso.data.model

data class Cliente(
    val idCliente: String = "",

    val nameCliente: String = "",
    val dniCliente: String = "",
    val telefonoCliente: String = "",
    val ciudadCliente: String = "",
    val direccionCliente: String = "",

    val estadoCliente: EstadoCliente = EstadoCliente.activo,

    var paqueteActivoId: String? = null,

    val deudaTotal: Double = 0.0,

    val fechaRegistro: Long = System.currentTimeMillis(),
    var paqueteSeleccionado: Paquete? = null
) {
    enum class EstadoCliente {
        activo,
        desactivado
    }
}