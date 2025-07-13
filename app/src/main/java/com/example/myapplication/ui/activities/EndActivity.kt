package com.example.myapplication.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class EndActivity : AppCompatActivity() {

  // (Buena práctica) Definir la clave del extra en un companion object
  companion object {
    const val EXTRA_DATOS_JUEGO = "DATOS_JUEGO_COMPLETO"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_end)

    // 1. Enlazar los componentes del layout
    val tituloResultadoTextView: TextView = findViewById(R.id.tituloResultadoTextView)
    val mensajeDetalladoTextView: TextView = findViewById(R.id.mensajeDetalladoTextView)
    val datosJugadorTextView: TextView = findViewById(R.id.datosJugadorTextView)
    val jugarDeNuevoButton: Button = findViewById(R.id.jugarDeNuevoButton)

    // 2. Recibir el string combinado del Intent
    val datosJuego = intent.getStringExtra(EXTRA_DATOS_JUEGO)

    // 3. Procesar los datos (¡la parte clave!)
    if (datosJuego != null) {
      // Partimos el string por el separador ";"
      // "Nombre;Puntuacion;resultado" -> ["Nombre", "Puntuacion", "resultado"]
      val partes = datosJuego.split(';')

      // Verificación de seguridad para evitar crashes si el string no tiene el formato esperado
      if (partes.size == 6) {
        val nombre = partes[0]
        val puntuacion: Int = partes[1].toInt()
        val codigoResultado = partes[2].toIntOrNull() // Usamos toIntOrNull() para más seguridad
        val puntuacionOponente: Int = partes[3].toInt()
        val miTurno: Int = partes[4].toInt()
        val ultimoTurno: Int = partes[5].toInt()
        // 4. Determinar los mensajes basados en el código
        val titulo: String
        val mensajeDetallado: String

        when (codigoResultado) {
          0 -> {
            if (miTurno == ultimoTurno) {
              titulo = "Perdiste"
              mensajeDetallado = "Explotaste una mina"
            } else {
              titulo = "Ganaste"
              mensajeDetallado = "El oponente explotó una mina"
            }
          }
          1 -> {
            titulo = "¡Juego terminado!"
            if (puntuacion > puntuacionOponente) {
              mensajeDetallado = "Has Ganado"
            } else if (puntuacionOponente > puntuacion) {
              mensajeDetallado = "Has perdido"
            } else {
              mensajeDetallado = "Empate"
            }

            // mensajeDetallado = "Se marcaron todas las minas"
          }
          2 -> {
            titulo = "¡Juego Terminado!"
            if (puntuacion > puntuacionOponente) {
              mensajeDetallado = "Has Ganado"
            } else if (puntuacionOponente > puntuacion) {
              mensajeDetallado = "Has perdido"
            } else {
              mensajeDetallado = "Empate"
            }
          }
          else -> {
            titulo = "Fin del Juego"
            mensajeDetallado = "Resultado desconocido"
          }
        }

        // 5. Actualizar la UI con los datos procesados
        tituloResultadoTextView.text = titulo
        mensajeDetalladoTextView.text = mensajeDetallado
        datosJugadorTextView.text =
            "Jugador: $nombre - Puntuación: $puntuacion - Adversario: $puntuacionOponente"
      } else {
        // Si el formato es incorrecto, mostramos un error genérico
        tituloResultadoTextView.text = "Error"
        mensajeDetalladoTextView.text = "No se pudieron cargar los datos del resultado."
        Log.e("EndActivity", "Formato de datos de juego incorrecto: $datosJuego")
      }
    }

    // 6. Configurar el botón para jugar de nuevo
    /*jugarDeNuevoButton.setOnClickListener {
      // Regresa a la pantalla del juego
      val intentJuego =
          Intent(
              this, GameActivity::class.java) // Asegúrate que GameActivity sea el nombre correcto
      startActivity(intentJuego)
      finish() // Cierra esta pantalla
    }*/
  }
}
