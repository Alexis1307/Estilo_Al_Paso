package com.estilo.estilo_al_paso.data.remote

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}

