package com.example.futbolapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val idPlayer = intent.getStringExtra("idPlayer") ?: ""
        val nombre = intent.getStringExtra("name") ?: ""
        title = nombre

        val ivFoto = findViewById<ImageView>(R.id.ivFoto)
        val tvNombre = findViewById<TextView>(R.id.tvNombreJ)
        val tvDatos = findViewById<TextView>(R.id.tvDatosJ)
        val tvEstado = findViewById<TextView>(R.id.tvEstadoF)

        tvNombre.text = nombre
        tvEstado.text = "Cargando foto..."

        lifecycleScope.launch(Dispatchers.IO) {              // fondo: dos descargas
            try {
                val detalle = SportsApi.detalleJugador(idPlayer)
                val foto = if (detalle != null && detalle.photoUrl.isNotEmpty())
                    SportsApi.descargarImagen(detalle.photoUrl)
                else null

                withContext(Dispatchers.Main) {              // principal: pantalla
                    if (detalle == null) {
                        tvEstado.text = "No se encontró información de este jugador"
                        return@withContext
                    }
                    tvNombre.text = detalle.name
                    val num = if (detalle.number.isEmpty()) "" else "  |  #${detalle.number}"
                    tvDatos.text = "${detalle.position}  |  ${detalle.nationality}$num"

                    if (foto != null) {
                        ivFoto.setImageBitmap(foto)
                        tvEstado.text = ""
                    } else {
                        ivFoto.setImageResource(android.R.drawable.ic_menu_gallery)
                        tvEstado.text = "Este jugador no tiene foto disponible"
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