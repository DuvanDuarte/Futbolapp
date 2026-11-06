package com.example.futbolapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StandingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_standings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val idLeague = intent.getStringExtra("idLeague") ?: ""
        val liga = intent.getStringExtra("league") ?: ""
        val idTeam = intent.getStringExtra("idTeam") ?: ""
        title = "Posiciones - $liga"

        val tvEstado = findViewById<TextView>(R.id.tvEstadoT)
        val lvTabla = findViewById<ListView>(R.id.lvTabla)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        lvTabla.adapter = adapter

        tvEstado.text = "Cargando tabla..."

        lifecycleScope.launch(Dispatchers.IO) {              // fondo: red
            try {
                val tabla = SportsApi.tablaDeLiga(idLeague)

                withContext(Dispatchers.Main) {              // principal: pantalla
                    adapter.clear()
                    adapter.addAll(tabla.map {
                        val marca = if (it.idTeam == idTeam) "★ " else ""
                        "$marca${it.rank}. ${it.team}\n" +
                                "    Pts ${it.points} | PJ ${it.played} | G ${it.win} E ${it.draw} P ${it.loss} | DG ${it.goalDiff}"
                    })

                    if (tabla.isEmpty()) {
                        tvEstado.text = "No hay tabla de posiciones disponible para esta liga"
                    } else {
                        var texto = "Temporada ${tabla[0].season} - ${tabla.size} equipos"
                        if (tabla.none { it.idTeam == idTeam }) {
                            texto += "\nEl equipo no aparece en las filas que entrega la API gratuita."
                        }
                        tvEstado.text = texto
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvEstado.text = "Error de conexión: ${e.message}"
                }
            }
        }
    }
}