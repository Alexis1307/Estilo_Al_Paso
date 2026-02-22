package com.estilo.estilo_al_paso.data.model

data class EstadisticasGenerales(
    val totalPaquetesActivos: Int = 0,
    val totalPrendasVendidas: Int = 0,
    val montoTotalVendido: Double = 0.0,
    val montoTotalPendiente: Double = 0.0
)