package com.estilo.estilo_al_paso.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estilo.estilo_al_paso.R
import com.estilo.estilo_al_paso.ui.envio.EnvioAdapter
import com.estilo.estilo_al_paso.ui.envio.EnvioViewModel


class EnvioFragment : Fragment(){

    private val viewModel: EnvioViewModel by viewModels()

    private lateinit var enviosAdapter: EnvioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_envios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvEnvios)
        enviosAdapter= EnvioAdapter(
            listaEnvio = emptyList(),
            onConfirmarClick = {id-> viewModel.confirmarEnvio(id)},
            onCancelarClick = {id->viewModel.cancerlarEnvio(id)}
        )

        rv.apply {
            adapter=enviosAdapter
            layoutManager= LinearLayoutManager(requireContext())
        }

        viewModel.envios.observe(viewLifecycleOwner){
            listaDeFirebase->enviosAdapter.actualizarLista(listaDeFirebase)
        }

        //BUSCADOR
        val etBuscar=view.findViewById<EditText>(R.id.etBuscarEnvio)

        etBuscar.addTextChangedListener { editable ->
            val texto = editable.toString()
            viewModel.filtrarEnvios(texto) //
        }


        viewModel.cargarEnvio()




    }

}