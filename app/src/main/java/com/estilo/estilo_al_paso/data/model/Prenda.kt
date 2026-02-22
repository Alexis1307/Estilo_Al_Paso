package com.estilo.estilo_al_paso.data.model

data class Prenda(
    val idPrenda: String = "",

    val descripcionPrenda: String = "",
    val precioPrenda: Double = 0.0,

    val estadoPago: EstadoPago = EstadoPago.pagado,
    val estadoPrenda: EstadoPrenda = EstadoPrenda.buenEstado,

    val fechaRegistro: Long = System.currentTimeMillis()
) {
    enum class EstadoPago {
        pagado,
        pendiente
    }

    enum class EstadoPrenda {
        buenEstado,
        lavanderia,
        reparacion
    }
}