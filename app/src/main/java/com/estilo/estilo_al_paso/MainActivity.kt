package com.estilo.estilo_al_paso

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.estilo.estilo_al_paso.ui.fragment.ClientesFragment
import com.estilo.estilo_al_paso.ui.fragment.EnvioFragment
import com.estilo.estilo_al_paso.ui.fragment.HomeFragment
import com.estilo.estilo_al_paso.ui.fragment.RegisterPrendaFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        val rol = sessionManager.getRol()
        val nombre = sessionManager.getNombre()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNav = findViewById(R.id.bottom_navigation)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            android.R.string.ok, android.R.string.cancel
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        configurarMenuPorRol(rol)
        configurarNavHeader(nombre, rol)
        navigationView.setNavigationItemSelectedListener(this)
        configurarBottomNav(rol)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun configurarMenuPorRol(rol: String) {
        navigationView.menu.clear()
        if (rol == SessionManager.ROL_ADMIN) {
            navigationView.inflateMenu(R.menu.drawer_admin)
            bottomNav.menu.findItem(R.id.nav_clientes)?.isVisible = true
            bottomNav.menu.findItem(R.id.nav_envios)?.isVisible = true
        } else {
            navigationView.inflateMenu(R.menu.drawer_cliente)
            bottomNav.menu.findItem(R.id.nav_clientes)?.isVisible = false
            bottomNav.menu.findItem(R.id.nav_envios)?.isVisible = false
        }
    }

    private fun configurarNavHeader(nombre: String, rol: String) {
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.navHeaderNombre).text = nombre
        header.findViewById<TextView>(R.id.navHeaderRol).text =
            if (rol == SessionManager.ROL_ADMIN) "Administrador" else "Cliente"
    }

    private fun configurarBottomNav(rol: String) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_register -> { loadFragment(RegisterPrendaFragment()); true }
                R.id.nav_clientes -> {
                    if (rol == SessionManager.ROL_ADMIN) { loadFragment(ClientesFragment()); true }
                    else false
                }
                R.id.nav_envios -> {
                    if (rol == SessionManager.ROL_ADMIN) { loadFragment(EnvioFragment()); true }
                    else false
                }
                else -> false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_registrar_admin -> {
                startActivity(Intent(this, RegisterAdminActivity::class.java))
            }
            R.id.drawer_cerrar_sesion -> {
                confirmarCierreSesion()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun confirmarCierreSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas salir?")
            .setPositiveButton("Sí") { _, _ ->
                sessionManager.cerrarSesion()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}