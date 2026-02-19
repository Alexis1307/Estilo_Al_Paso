package com.estilo.estilo_al_paso.ui.envio

import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.data.model.Envio



class EnvioAdapter (

    private var listaEnvio: List<Envio>,
    private val onConfirmarClick: (String) -> Unit,
    private val onCancelarClick: (String)-> Unit
): RecyclerView.Adapter<EnvioAdapter.EnvioViewHolder>(){
    inner class EnvioViewHolder(itemView: View):
            RecyclerView.ViewHolder(itemView){

                val tvNombreCliente= itemView.findViewById<TextView>(R.id.tvNombreCliente)
                val tvtipoEstado = itemView.findViewById<TextView>(R.id.tvTipoEstado)
                val btnconfirmar= itemView.findViewById<Button>(R.id.btnConfirmarEnvio)
                val btncancelar=  itemView.findViewById<Button>(R.id.btnCancelarEnvio)
                val tvUbicacion= itemView.findViewById<TextView>(R.id.tvUbicacion)
                val tvprendas= itemView.findViewById<TextView>(R.id.tvCantidadPrendas)
                val tvTipoEnvio=itemView.findViewById<TextView>(R.id.tvtipoEnvio)
                val tvDatosContacto=itemView.findViewById<TextView>(R.id.tvDatosContacto)



            }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnvioViewHolder {
           val view = LayoutInflater.from(parent.context)
               .inflate(R.layout.item_envio, parent,false)
         return EnvioViewHolder(view)

     }

     override fun getItemCount(): Int= listaEnvio.size

     override fun onBindViewHolder(holder: EnvioViewHolder, position: Int) {
         val envio= listaEnvio[position]

         holder.tvNombreCliente.text= envio.nombreCliente
         holder.tvtipoEstado.text=envio.estadoEnvio.name
         holder.tvUbicacion.text="Ubicacion:${envio.ciudadCliente} | ${envio.direccionCliente}"
         holder.tvprendas.text="Prendas:${envio.totalPrendas}"
         holder.tvTipoEnvio.text="Tipo: ${envio.tipoEnvio.name}"
         holder.tvDatosContacto.text="Dni:${envio.dniCliente} | Cel:${envio.telefonoCliente}"

         holder.btnconfirmar.setOnClickListener {
             onConfirmarClick(envio.idEnvio)
         }
         holder.btncancelar.setOnClickListener {
             onCancelarClick(envio.idEnvio)
         }

     }


    fun actualizarLista(nuevaLista:List<Envio>){
        this.listaEnvio=nuevaLista
        notifyDataSetChanged()
    }





}








