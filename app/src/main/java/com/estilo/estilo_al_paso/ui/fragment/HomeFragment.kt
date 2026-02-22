package com.estilo.estilo_al_paso.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.estilo.estilo_al_paso.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.estilo.estilo_al_paso.ui.home.EstadisticasViewModel

class HomeFragment : Fragment() {
    private lateinit var viewModel: EstadisticasViewModel

    private lateinit var tvPaquetesActivos: TextView
    private lateinit var tvPrendasVendidas: TextView
    private lateinit var tvMontoTotal: TextView
    private lateinit var tvMontoPendiente: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvPaquetesActivos = view.findViewById(R.id.tvPaquetesActivos)
        tvPrendasVendidas = view.findViewById(R.id.tvPrendasVendidas)
        tvMontoTotal = view.findViewById(R.id.tvMontoTotal)
        tvMontoPendiente = view.findViewById(R.id.tvMontoPendiente)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(EstadisticasViewModel::class.java)

        viewModel.estadisticas.observe(viewLifecycleOwner) { stats ->
            tvPaquetesActivos.text = stats.totalPaquetesActivos.toString()
            tvPrendasVendidas.text = stats.totalPrendasVendidas.toString()
            tvMontoTotal.text = "S/ ${stats.montoTotalVendido}"
            tvMontoPendiente.text = "S/ ${stats.montoTotalPendiente}"
        }
    }
}