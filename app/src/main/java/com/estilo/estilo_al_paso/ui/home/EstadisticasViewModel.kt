package com.estilo.estilo_al_paso.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.estilo.estilo_al_paso.data.model.EstadisticasGenerales
import com.google.firebase.firestore.FirebaseFirestore

class EstadisticasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val statsRef = db.collection("estadisticas_global").document("resumen")

    private val _estadisticas = MutableLiveData<EstadisticasGenerales>()
    val estadisticas: LiveData<EstadisticasGenerales> = _estadisticas

    init {
        escucharEstadisticas()
    }

    private fun escucharEstadisticas() {
        statsRef.addSnapshotListener { snapshot, error ->

            if (error != null) {
                Log.e("ESTADISTICAS", "Error: ", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val stats = snapshot.toObject(EstadisticasGenerales::class.java)
                _estadisticas.value = stats
            } else {
                Log.d("ESTADISTICAS", "El documento no existe")
            }
        }
    }
}