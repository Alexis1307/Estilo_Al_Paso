package com.estilo.estilo_al_paso

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.estilo.estilo_al_paso.ui.fragment.ClientesFragment
import com.estilo.estilo_al_paso.ui.fragment.HomeFragment
import com.estilo.estilo_al_paso.ui.fragment.RegisterFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_register -> {
                    loadFragment(RegisterFragment())
                    true
                }

                R.id.nav_clientes -> {
                    loadFragment(ClientesFragment())
                    true
                }

               /* R.id.nav_envios -> {
                    loadFragment(EnviosFragment())
                    true
                }*/

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}