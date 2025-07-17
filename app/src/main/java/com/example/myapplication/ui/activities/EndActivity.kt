package com.example.myapplication.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import android.widget.TableLayout

class EndActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_DATOS_JUEGO = "DATOS_JUEGO_COMPLETO"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_end)

    // 1. Enlazar los componentes del layout
    val tituloResultadoTextView: TextView = findViewById(R.id.tituloResultadoTextView)
    val mensajeDetalladoTextView: TextView = findViewById(R.id.mensajeDetalladoTextView)
    val jugarDeNuevoButton: Button = findViewById(R.id.jugarDeNuevoButton)

    // Nuevos TextViews para la tabla
    val miPuntuacionTextView: TextView = findViewById(R.id.miPuntuacionTextView)
    val misBanderasTextView: TextView = findViewById(R.id.misBanderasTextView)
    val oponentePuntuacionTextView: TextView = findViewById(R.id.oponentePuntuacionTextView)
    val oponenteBanderasTextView: TextView = findViewById(R.id.oponenteBanderasTextView)
    val statsTableLayout: TableLayout = findViewById(R.id.statsTableLayout) // Para ocultarla en caso de error

    // 2. Recibir el string combinado del Intent
    val datosJuego = intent.getStringExtra(EXTRA_DATOS_JUEGO)

    // 3. Procesar los datos
    if (datosJuego != null) {
      val partes = datosJuego.split(';')

      if (partes.size == 8) {
        // Hacemos visible la tabla por si estaba oculta
        statsTableLayout.visibility = View.VISIBLE

        // El nombre no se usa en la tabla, pero lo mantenemos por si lo necesitas en el futuro
        val nombre: String = partes[0]
        val puntuacion: Int = partes[1].toInt()
        val codigoResultado: Int? = partes[2].toIntOrNull()
        val puntuacionOponente: Int = partes[3].toInt()
        val miTurno: Int = partes[4].toInt()
        val ultimoTurno: Int = partes[5].toInt()
        val banderas: Int = partes[6].toInt()
        val banderasOponente: Int = partes[7].toInt()

        // 4. Determinar los mensajes basados en el código (esta lógica no cambia)
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
              mensajeDetallado = "¡Has ganado!"
            } else if (puntuacionOponente > puntuacion) {
              mensajeDetallado = "Has perdido"
            } else {
              mensajeDetallado = if (banderas > banderasOponente) {
                "¡Has ganado por banderas!"
              } else {
                "Has perdido por banderas"
              }
            }
          }
          2 -> {
            titulo = "¡Juego Terminado!"
            if (puntuacion > puntuacionOponente) {
              mensajeDetallado = "¡Has ganado por tiempo!"
            } else if (puntuacionOponente > puntuacion) {
              mensajeDetallado = "Has perdido por tiempo"
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

        // Rellenar la tabla con los datos
        miPuntuacionTextView.text = puntuacion.toString()
        misBanderasTextView.text = banderas.toString()
        oponentePuntuacionTextView.text = puntuacionOponente.toString()
        oponenteBanderasTextView.text = banderasOponente.toString()

      } else {
        // Si el formato es incorrecto, mostramos un error genérico y ocultamos la tabla
        tituloResultadoTextView.text = "Error"
        mensajeDetalladoTextView.text = "No se pudieron cargar los datos del resultado."
        statsTableLayout.visibility = View.GONE // Ocultamos la tabla para que no se vea vacía
        Log.e("EndActivity", "Formato de datos de juego incorrecto: $datosJuego")
      }
    } else {
      // Caso extra: si el intent no trae datos
      tituloResultadoTextView.text = "Error"
      mensajeDetalladoTextView.text = "No se recibieron datos del juego."
      statsTableLayout.visibility = View.GONE
      Log.e("EndActivity", "El extra DATOS_JUEGO_COMPLETO es nulo.")
    }
  }
}
