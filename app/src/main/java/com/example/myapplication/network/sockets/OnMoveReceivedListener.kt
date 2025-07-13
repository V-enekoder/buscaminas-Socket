package com.example.myapplication.network.sockets

import com.example.myapplication.game.core.Jugador

interface GameEventsListener {
  fun onMoveReceived(
      turno: Int,
      action: String,
      row: Int,
      col: Int,
      puntuacion: Int,
      ultimoTurno: Int
  )

  fun onGameEnd(jugador1: Jugador, jugador2: Jugador)

  fun showDebugToast(message: String)
}
