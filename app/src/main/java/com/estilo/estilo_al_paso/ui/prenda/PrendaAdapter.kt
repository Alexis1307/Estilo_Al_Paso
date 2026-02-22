package com.estilo.estilo_al_paso.ui.prenda

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Prenda

class PrendaAdapter(
    private val onEliminarClick: (Prenda) -> Unit
) : RecyclerView.Adapter<PrendaAdapter.PrendaViewHolder>() {

    private var listaPrendas: List<Prenda> = emptyList()

    fun submitList(nuevaLista: List<Prenda>) {
        listaPrendas = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prenda, parent, false)
        return PrendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {
        val prenda = listaPrendas[position]
        holder.bind(prenda)
    }

    override fun getItemCount(): Int = listaPrendas.size

    fun Long.toFechaLegible(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this))
    }

    inner class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDescripcion: TextView =
            itemView.findViewById(R.id.tvDescripcionPrenda)

        private val tvPrecio: TextView =
            itemView.findViewById(R.id.tvPrecioPrenda)

        private val tvEstadoPago: TextView =
            itemView.findViewById(R.id.tvEstadoPago)

        private val tvEstadoPrenda: TextView =
            itemView.findViewById(R.id.tvEstadoPrenda)

        private val btnEliminar: ImageButton =
            itemView.findViewById(R.id.btnEliminarPrenda)

        private val tvFechaCreacion: TextView =
            itemView.findViewById(R.id.tvFechaCreacion)

        fun bind(prenda: Prenda) {

            tvDescripcion.text = prenda.descripcionPrenda
            tvPrecio.text = "S/ %.2f".format(prenda.precioPrenda)

            tvEstadoPago.text = prenda.estadoPago.name.uppercase()
            tvEstadoPrenda.text = prenda.estadoPrenda.name.uppercase()

            tvFechaCreacion.text = prenda.fechaRegistro.toFechaLegible()

            if (prenda.estadoPago == Prenda.EstadoPago.pendiente) {
                tvPrecio.setTextColor(Color.parseColor("#E57373"))
                tvEstadoPago.setTextColor(Color.parseColor("#E57373"))
            } else {
                tvPrecio.setTextColor(Color.parseColor("#4CAF50"))
                tvEstadoPago.setTextColor(Color.parseColor("#4CAF50"))
            }

            btnEliminar.setOnClickListener {
                onEliminarClick(prenda)
            }
        }
    }
}