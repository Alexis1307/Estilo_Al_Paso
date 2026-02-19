package com.estilo.estilo_al_paso.ui.cliente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Cliente

class ClienteAdapter(
    private val onItemClick: ((Cliente) -> Unit)? = null
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    private var listaClientes = listOf<Cliente>()

    fun actualizarLista(nuevaLista: List<Cliente>) {
        listaClientes = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.bind(cliente)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(cliente)
        }
    }

    override fun getItemCount() = listaClientes.size

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNombre = itemView.findViewById<TextView>(R.id.nameCliente)
        private val tvTelefono = itemView.findViewById<TextView>(R.id.telefonoCliente)
        private val tvCiudad = itemView.findViewById<TextView>(R.id.ciudadCliente)
        private val tvEstado = itemView.findViewById<TextView>(R.id.estadoFinanciero)

        fun bind(cliente: Cliente) {

            tvNombre.text = cliente.nameCliente
            tvTelefono.text = cliente.telefonoCliente
            tvCiudad.text = " - ${cliente.ciudadCliente}"

            if (cliente.deudaTotal <= 0.0) {
                tvEstado.text = "PAGADO"
                tvEstado.setTextColor(
                    itemView.context.getColor(R.color.green)
                )
            } else {
                tvEstado.text = "DEUDA: S/ %.2f".format(cliente.deudaTotal)
                tvEstado.setTextColor(
                    itemView.context.getColor(R.color.red)
                )
            }
        }
    }
}
