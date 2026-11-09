package com.example.futbolapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private val equipos = mutableListOf<Team>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val etEquipo = findViewById<EditText>(R.id.etEquipo)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val lvEquipos = findViewById<ListView>(R.id.lvEquipos)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        lvEquipos.adapter = adapter

        lvEquipos.setOnItemClickListener { _, _, position, _ ->
            val t = equipos[position]
            val i = Intent(this, TeamActivity::class.java)
            i.putExtra("idTeam", t.idTeam)
            i.putExtra("name", t.name)
            i.putExtra("idLeague", t.idLeague)
            i.putExtra("league", t.league)
            startActivity(i)
        }

        btnBuscar.setOnClickListener {
            val nombre = etEquipo.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escriba el nombre de un equipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvEstado.text = "Buscando..."

            lifecycleScope.launch(Dispatchers.IO) {          // hilo de fondo: red
                try {
                    val resultado = SportsApi.buscarEquipos(nombre)

                    withContext(Dispatchers.Main) {          // hilo principal: pantalla
                        equipos.clear()
                        equipos.addAll(resultado)
                        adapter.clear()
                        adapter.addAll(resultado.map { "${it.name} (${it.league})" })
                        tvEstado.text = if (resultado.isEmpty())
                            "No se encontraron equipos para \"$nombre\""
                        else
                            "${resultado.size} equipo(s) encontrado(s)"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvEstado.text = "Error de conexión: ${e.message}"
                    }
                }
            }
        }
    }
}