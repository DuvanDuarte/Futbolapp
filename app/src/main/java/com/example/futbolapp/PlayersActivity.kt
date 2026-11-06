package com.example.futbolapp

import android.content.Intent
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

class PlayersActivity : AppCompatActivity() {

    private val jugadores = mutableListOf<Player>()   // lo usaremos en el Paso 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_players)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val idTeam = intent.getStringExtra("idTeam") ?: ""
        val nombre = intent.getStringExtra("name") ?: ""
        title = "Jugadores - $nombre"

        val tvEstado = findViewById<TextView>(R.id.tvEstadoJ)
        val lvJugadores = findViewById<ListView>(R.id.lvJugadores)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        lvJugadores.adapter = adapter

        lvJugadores.setOnItemClickListener { _, _, position, _ ->
            val p = jugadores[position]
            val i = Intent(this, PlayerDetailActivity::class.java)
            i.putExtra("idPlayer", p.idPlayer)
            i.putExtra("name", p.name)
            startActivity(i)
        }

        tvEstado.text = "Cargando jugadores..."

        lifecycleScope.launch(Dispatchers.IO) {              // fondo: red
            try {
                val lista = SportsApi.jugadoresDeEquipo(idTeam)

                withContext(Dispatchers.Main) {              // principal: pantalla
                    jugadores.clear()
                    jugadores.addAll(lista)
                    adapter.clear()
                    adapter.addAll(lista.map {
                        val num = if (it.number.isEmpty()) "" else "#${it.number}  "
                        "$num${it.name} (${it.position})"
                    })
                    tvEstado.text = if (lista.isEmpty())
                        "No hay jugadores disponibles para este equipo"
                    else
                        "${lista.size} jugador(es)"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvEstado.text = "Error de conexión: ${e.message}"
                }
            }
        }
    }
}