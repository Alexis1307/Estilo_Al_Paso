package com.estilo.estilo_al_paso.data.model

data class HistorialAccion(
    val idHistorial: String = "",
    val idUsuario: String = "",
    val accion: String = "",
    val descripcion: String = "",
    val fechaAccion: Long = System.currentTimeMillis()
)