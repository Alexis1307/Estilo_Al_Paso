package com.estilo.estilo_al_paso.data.model

data class Prenda (
    val idPrenda : String = "",
    val idPaquete : String = "",
    val descripcionPrenda : String = "",
    val precioPrenda : Double = 0.0,
    val fechaRegistro : Long = System.currentTimeMillis(),
    val estadoPago : EstadoPago = EstadoPago.pagado,
    val estadoPrenda : EstadoPrenda = EstadoPrenda.buenEstado
){
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