package com.estilo.estilo_al_paso.ui.envio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Query
import com.estilo.estilo_al_paso.data.model.Envio
import com.estilo.estilo_al_paso.data.repository.EnvioRepository

class EnvioViewModel : ViewModel() {

    private val repository = EnvioRepository()

    private val _envios = MutableLiveData<List<Envio>>()
    val envios: LiveData<List<Envio>> = _envios
    private var listaCompleta: List<Envio> = listOf()

    fun cargarEnvio(){
    repository.obtenerEnviosProgramados(onResult =
        {lista->
            listaCompleta=lista
            _envios.value=lista
        }, onError = {

          }
          )
        }


    fun filtrarEnvios(query: String){
        val texto = query.lowercase()
        if (texto.isEmpty()){
            _envios.value=listaCompleta
        }else{
            val filtrarda=listaCompleta.filter{
                envio->envio.nombreCliente.lowercase().contains(texto) ||
                    envio.dniCliente.lowercase().contains(texto)
            }
            _envios.value=filtrarda
        }
    }


    fun confirmarEnvio(idEnvio: String){
        repository.completarEnvio(idEnvio, onSuccess = {cargarEnvio()}, onError = {})
    }

    fun cancerlarEnvio(idEnvio: String){
        repository.cancelarEnvio(idEnvio, onSuccess = {cargarEnvio()}, onError = {})
    }



}