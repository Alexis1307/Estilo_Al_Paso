package com.estilo.estilo_al_paso

import org.mindrot.jbcrypt.BCrypt

object BCryptHelper {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verificarPassword(password: String, hashed: String): Boolean {
        return BCrypt.checkpw(password, hashed)
    }
}