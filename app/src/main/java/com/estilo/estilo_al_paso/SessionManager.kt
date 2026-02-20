package com.estilo.estilo_al_paso

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("estilo_session", Context.MODE_PRIVATE)

    companion object {
        const val ROL_ADMIN = "admin"
        const val ROL_CLIENTE = "cliente"
    }

    fun guardarSesion(userId: String, nombre: String, rol: String) {
        prefs.edit()
            .putString("userId", userId)
            .putString("nombre", nombre)
            .putString("rol", rol)
            .apply()
    }

    fun getRol(): String = prefs.getString("rol", "") ?: ""
    fun getNombre(): String = prefs.getString("nombre", "") ?: ""
    fun getUserId(): String = prefs.getString("userId", "") ?: ""
    fun isLoggedIn(): Boolean = prefs.getString("userId", "").isNullOrEmpty().not()
    fun cerrarSesion() = prefs.edit().clear().apply()
}