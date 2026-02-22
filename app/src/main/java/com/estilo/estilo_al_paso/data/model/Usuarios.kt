package com.estilo.estilo_al_paso.data.model

data class Usuarios (
    val idUser : String = "",
    val nameUser : String = "",
    val passwordUser : String = "",
    val rolUser : String = "",
    val estadoUser : String = "activo",
    val fechaCreacion : Long = System.currentTimeMillis(),
)