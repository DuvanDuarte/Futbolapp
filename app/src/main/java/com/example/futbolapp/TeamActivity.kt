package com.example.futbolapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Datos que envió MainActivity dentro del Intent
        val idTeam = intent.getStringExtra("idTeam") ?: ""
        val nombre = intent.getStringExtra("name") ?: ""
        val idLeague = intent.getStringExtra("idLeague") ?: ""
        val liga = intent.getStringExtra("league") ?: ""

        findViewById<TextView>(R.id.tvNombre).text = nombre
        findViewById<TextView>(R.id.tvLiga).text = liga

        findViewById<Button>(R.id.btnJugadores).setOnClickListener {
            val i = Intent(this, PlayersActivity::class.java)
            i.putExtra("idTeam", idTeam)
            i.putExtra("name", nombre)
            startActivity(i)
        }

        findViewById<Button>(R.id.btnTabla).setOnClickListener {
            if (idLeague.isEmpty()) {
                Toast.makeText(this, "Este equipo no tiene liga registrada", Toast.LENGTH_SHORT).show()
            } else {
                val i = Intent(this, StandingsActivity::class.java)
                i.putExtra("idLeague", idLeague)
                i.putExtra("league", liga)
                i.putExtra("idTeam", idTeam)
                startActivity(i)
            }
        }
    }
}