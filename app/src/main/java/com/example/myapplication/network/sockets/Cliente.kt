package com.example.myapplication.network.sockets

import android.content.Context
import android.content.Intent
import com.example.myapplication.game.core.Jugador
import com.example.myapplication.ui.activities.ConfiguracionTablero
import com.example.myapplication.ui.activities.GameActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Cliente(dir: String, private val turno: Int, var nombre: String) : Runnable {
  private var direccionIP = dir
  private var socket: Socket? = null
  private var dis: BufferedReader? = null
  private var dos: PrintWriter? = null
  private var context: Context? = null
  private var moveListener: GameEventsListener? = null

  override fun run() {
    try {
      socket = Socket(direccionIP, 5200)
      dis = BufferedReader(InputStreamReader(socket!!.getInputStream()))
      dos = PrintWriter(socket!!.getOutputStream(), true)
      while (true) {
        recibirMensaje()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun recibirMensaje() {
    try {
      var mensajeRecibido: String
      while (socket?.isConnected == true) {
        mensajeRecibido = dis?.readLine().toString()
        descifrarMensaje(mensajeRecibido)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun descifrarMensaje(msj: String) {
    val partes = msj.split(" ")
    val type = partes[0]

    when (type) {
      "CONFIG" -> {
        val config = getconf(msj)
        if (config != null) {
          // Lanzar GameActivity con esa config
          val intent = Intent(context, GameActivity::class.java)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
          intent.putExtra("CONFIG", config)
          context?.startActivity(intent)
        }
      }
      "JUGADA" -> {
        try {
          val coords = partes[3].split("_")
          moveListener?.onMoveReceived(partes[1].toInt(),partes[2], coords[0].toInt(), coords[1].toInt(),
            partes[4].toInt(), partes[5].toInt(), partes[6].toInt(), partes[7].toInt(),partes[8])
        } catch (e: Exception) {
          println("Error al interpretar mensaje MOVE: $msj")
          e.printStackTrace()
        }
      }
    }
  }

  fun getconf(msj: String): ConfiguracionTablero? {
    return try {
      // Mensaje esperado: "GAME_CONFIG F_C_M F1-C1_F2-C2_..."
      val partes = msj.removePrefix("CONFIG ").split(" ")

      val configBasica = partes[0].split("_")
      val filas = configBasica[0].toInt()
      val columnas = configBasica[1].toInt()
      val minas = configBasica[2].toInt()
      val nombre: String = configBasica[3]

      val posicionesStr = partes[1] // "0-1_2-3_..."
      val listaPosiciones =
        posicionesStr.split("_").map { pos ->
          val coords = pos.split("-")
          Pair(coords[0].toInt(), coords[1].toInt())
        }

      // Creamos el objeto de configuración con la lista de minas
      ConfiguracionTablero(filas, columnas, minas, listaPosiciones, nombre)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun setMoveListener(listener: GameEventsListener?) {
    this.moveListener = listener
  }


  fun enviarMensaje(msj: String) {
    try {
      dos?.println(msj)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun setContext(contexto: Context) {
    context = contexto
  }

  fun getTurno(): Int {
    return turno
  }
}
