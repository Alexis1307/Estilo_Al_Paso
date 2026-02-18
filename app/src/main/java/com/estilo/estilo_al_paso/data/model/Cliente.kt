package com.estilo.estilo_al_paso.data.model

data class Cliente (
    val idCliente: String = "",
    val nameCliente : String = "",
    val dniCliente : String = "",
    val direccionCliente : String = "",
    val ciudadCliente : String = "",
    val telefonoCliente : String = "",
    val fechaRegistro : Long = System.currentTimeMillis(),
    val estadoCliente : EstadoCliente = EstadoCliente.activo,//activo o desactivado
    val deudaTotal : Double = 0.0,
    val tienePendientes: Boolean = true
){
    enum class EstadoCliente {
        activo,
        desactivado
    }
}