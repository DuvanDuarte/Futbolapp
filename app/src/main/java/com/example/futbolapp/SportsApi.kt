package com.example.futbolapp

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object SportsApi {
    private const val BASE = "https://www.thesportsdb.com/api/v1/json/123/"

    // Lee un campo de texto; si el JSON trae null devuelve "" (no la palabra "null")
    private fun JSONObject.str(key: String): String =
        if (isNull(key)) "" else getString(key)

    fun buscarEquipos(nombre: String): List<Team> {
        val q = URLEncoder.encode(nombre, "UTF-8")
        val respuesta = get(BASE + "searchteams.php?t=$q")
        val root = JSONObject(respuesta)

        if (root.isNull("teams")) return emptyList()   // caso {"teams":null}

        val arr = root.getJSONArray("teams")
        val lista = mutableListOf<Team>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                Team(
                    idTeam = o.str("idTeam"),
                    name = o.str("strTeam"),
                    idLeague = o.str("idLeague"),
                    league = o.str("strLeague"),
                    badgeUrl = o.str("strBadge")
                )
            )
        }
        return lista
    }

    fun jugadoresDeEquipo(idTeam: String): List<Player> {
        val respuesta = get(BASE + "lookup_all_players.php?id=$idTeam")
        val root = JSONObject(respuesta)

        if (root.isNull("player")) return emptyList()   // caso {"player":null}

        val arr = root.getJSONArray("player")
        val lista = mutableListOf<Player>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                Player(
                    idPlayer = o.str("idPlayer"),
                    name = o.str("strPlayer"),
                    position = o.str("strPosition"),
                    number = o.str("strNumber"),
                    nationality = o.str("strNationality")
                )
            )
        }
        return lista
    }

    fun tablaDeLiga(idLeague: String): List<Standing> {
        val respuesta = get(BASE + "lookuptable.php?l=$idLeague")
        val root = JSONObject(respuesta)

        if (root.isNull("table")) return emptyList()   // caso {"table":null}

        val arr = root.getJSONArray("table")
        val lista = mutableListOf<Standing>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                Standing(
                    idTeam = o.str("idTeam"),
                    rank = o.str("intRank"),
                    team = o.str("strTeam"),
                    played = o.str("intPlayed"),
                    win = o.str("intWin"),
                    draw = o.str("intDraw"),
                    loss = o.str("intLoss"),
                    goalDiff = o.str("intGoalDifference"),
                    points = o.str("intPoints"),
                    season = o.str("strSeason")
                )
            )
        }
        return lista
    }

    fun detalleJugador(idPlayer: String): PlayerDetail? {
        val respuesta = get(BASE + "lookupplayer.php?id=$idPlayer")
        val root = JSONObject(respuesta)

        // OJO: aquí la clave es "players" (plural), distinta a la de la lista
        if (root.isNull("players")) return null
        val arr = root.getJSONArray("players")
        if (arr.length() == 0) return null

        val o = arr.getJSONObject(0)
        // Primera foto que no esté vacía; si no hay ninguna, queda ""
        val foto = listOf("strThumb", "strCutout", "strRender", "strCartoon")
            .map { o.str(it) }
            .firstOrNull { it.isNotEmpty() } ?: ""

        return PlayerDetail(
            name = o.str("strPlayer"),
            position = o.str("strPosition"),
            nationality = o.str("strNationality"),
            number = o.str("strNumber"),
            photoUrl = foto
        )
    }

    // Descarga bytes de la imagen y los convierte en Bitmap
    fun descargarImagen(urlStr: String): Bitmap? {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            if (conn.responseCode !in 200..299) return null
            return conn.inputStream.use { BitmapFactory.decodeStream(it) }
        } finally {
            conn.disconnect()
        }
    }

    private fun get(urlStr: String): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val code = conn.responseCode
            if (code == 429) throw Exception("Demasiadas solicitudes a la API (429). Espere un minuto e intente de nuevo.")
            if (code !in 200..299) throw Exception("Error HTTP $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}