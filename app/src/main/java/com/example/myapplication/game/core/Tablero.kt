package com.example.myapplication.game.core

class Tablero(
    private val filas: Int,
    private val columnas: Int,
    // private val numeroMinas: Int,
    private val posicionesMinas: List<Pair<Int, Int>>,
    private val nombre: String,
    private val turno: Int
) {
  private var jugadas: Int = 0
  private var juegoTerminado: Boolean = false
  private var victoria: Boolean = false
  private val tablero: Array<Array<Casilla>>
  private var jugador: Jugador = Jugador(nombre)
  val numeroMinas = posicionesMinas.size

  init {
    if (numeroMinas > filas * columnas) {
      throw IllegalArgumentException("El número de minas no puede exceder el total de casillas.")
    }
    if (numeroMinas < 0 || filas <= 0 || columnas <= 0) {
      throw IllegalArgumentException("Dimensiones y número de minas deben ser positivos.")
    }

    tablero = Array(filas) { r -> Array(columnas) { c -> Casilla(r, c) } }
    asignarMinas()
    actualizarMinasAlrededor()
  }

  private fun asignarMinas() {

    for (pos in posicionesMinas) {
      val fila = pos.first
      val columna = pos.second
      // Suponiendo que tienes una matriz `casillas`
      tablero[fila][columna].setMina(true)
    }
  }

  private fun actualizarMinasAlrededor() {
    for (r in 0 until filas) {
      for (c in 0 until columnas) {
        if (tablero[r][c].isMina()) {
          aumentarContadorAdyacentes(r, c)
        }
      }
    }
  }

  private fun aumentarContadorAdyacentes(filaMina: Int, columnaMina: Int) {
    for (i in (filaMina - 1)..(filaMina + 1)) {
      for (j in (columnaMina - 1)..(columnaMina + 1)) {
        if (estaDentroDeLimites(i, j) &&
            (i != filaMina || j != columnaMina) &&
            !tablero[i][j].isMina()) {
          tablero[i][j].incrementarMinasAlrededor()
        }
      }
    }
  }

  private fun estaDentroDeLimites(fila: Int, columna: Int): Boolean {
    return fila >= 0 && fila < filas && columna >= 0 && columna < columnas
  }

  /*
   * -1 = Perdiate
   *  0 = No válido
   * 1 = Sigue la partida
   * */

  fun abrirCasilla(fila: Int, columna: Int, turnoJugada: Int): Int {
    if (!estaDentroDeLimites(fila, columna)) {
      return 0
    }
    val casilla = tablero[fila][columna]

    jugadas++
    casilla.abrir()

    val aumentarPuntuacion: Boolean = !casilla.isMina() && turnoJugada == turno
    if (aumentarPuntuacion) {
      jugador.aumentarPuntuacion()
    }
    if (casilla.isMina()) {
      juegoTerminado = true
      victoria = false
      return -1
    }

    if (casilla.getMinasAlrededor() == 0) {
      abrirAlrededorRecursivo(casilla, aumentarPuntuacion)
    }

    return 1
  }

  private fun abrirAlrededorRecursivo(casillaOriginal: Casilla, aumentarPuntuacion: Boolean) {
    if (casillaOriginal.getMinasAlrededor() == 0) {
      val casillasAdyacentes = obtenerCasillasAdyacentesParaAbrir(casillaOriginal)
      for (adyacente in casillasAdyacentes) {
        if (!adyacente.isAbierta() && !adyacente.isMarcada()) {
          adyacente.abrir()
          if (aumentarPuntuacion) {
            jugador.aumentarPuntuacion()
          }

          if (adyacente.getMinasAlrededor() == 0) {
            abrirAlrededorRecursivo(adyacente, aumentarPuntuacion)
          }
        }
      }
    }
  }

  private fun obtenerCasillasAdyacentesParaAbrir(casillaBase: Casilla): List<Casilla> {
    val alrededor = mutableListOf<Casilla>()
    val filaBase = casillaBase.getX()
    val columnaBase = casillaBase.getY()

    for (i in (filaBase - 1)..(filaBase + 1)) {
      for (j in (columnaBase - 1)..(columnaBase + 1)) {
        if (estaDentroDeLimites(i, j) && (i != filaBase || j != columnaBase)) {
          val casillaAdyacente = tablero[i][j]
          if (!casillaAdyacente.isMina()) {
            alrededor.add(casillaAdyacente)
          }
        }
      }
    }
    return alrededor
  }

  fun marcarCasilla(fila: Int, columna: Int, turnoJugada: Int): Int {
    val casilla = tablero[fila][columna]
    if (!casilla.isAbierta()) {
      casilla.marcar()
    }

    if (casilla.isMina() && turnoJugada == turno) {
      jugador.aumentarPuntuacion()
    }

    return 1
  }

  fun desmarcarCasilla(fila: Int, columna: Int, turnoJugada: Int): Int {
    val casilla = tablero[fila][columna]
    if (casilla.isMarcada()) {
      casilla.desmarcar()
    }

    if (casilla.isMina() && turnoJugada == turno) {
      jugador.reducirPuntuacion()
    }

    return 1
  }

  private fun seguir_partida(): Boolean {
    for (r in 0 until filas) {
      for (c in 0 until columnas) {
        val casilla = tablero[r][c]
        if (!casilla.isMina() && !casilla.isAbierta()) {
          return false
        }
        if (casilla.isMina() && !casilla.isMarcada()) {
          return false
        }
      }
    }
    return true
  }

  fun getFilas(): Int = filas

  fun getColumnas(): Int = columnas

  fun getMinas(): Int = numeroMinas

  fun getJugadas(): Int = jugadas

  fun getTablero(): Array<Array<Casilla>> {
    return tablero
  }

  fun getJugador(): Jugador {
    return this.jugador
  }

  fun setJugadas(nuevasJugadas: Int) {
    if (nuevasJugadas >= 0) { // Ejemplo de validación
      this.jugadas = nuevasJugadas
    }
  }

  fun getCasilla(fila: Int, columna: Int): Casilla? {
    if (fila in 0 until filas && columna in 0 until columnas) {
      return tablero[fila][columna]
    }
    return null
  }

  /*fun seguirPartida(): Boolean {
    var casillasAbiertas = 0
    var minasMarcadasCorrectamente = 0

    for (r in 0 until filas) {
      for (c in 0 until columnas) {
        val casilla = tablero[r][c]

        if (casilla.isAbierta() && casilla.isMina()) {
          return false // La partida no debe continuar (0 en C++)
        }

        // Contar casillas abiertas (que no sean la mina explotada, ya que habríamos retornado)
        if (casilla.isAbierta()) {
          casillasAbiertas++
        }

        // Contar minas correctamente marcadas
        // Una casilla puede estar marcada y ser una mina, independientemente de si está abierta o
        // no.
        // Sin embargo, si estaba abierta y era mina, ya se retornó false.
        // Así que aquí solo cuentan las minas marcadas que no están (fatalmente) abiertas.
        if (casilla.isMarcada() && casilla.isMina()) {
          minasMarcadasCorrectamente++
        }
      }
    }

    // Si se completa el bucle, no se ha abierto ninguna mina.
    // Ahora se verifican las condiciones de victoria.

    // Condición de victoria 1: Todas las minas están correctamente marcadas.
    if (minasMarcadasCorrectamente == numeroMinas) {
      return false // La partida no debe continuar (0 en C++)
    }

    // Condición de victoria 2: Todas las casillas seguras están abiertas.
    val totalCasillasSeguras = (filas * columnas) - numeroMinas
    if (casillasAbiertas == totalCasillasSeguras) {
      return false // La partida no debe continuar (0 en C++)
    }

    return true // La partida debe continuar (1 en C++)
  }*/

  fun verificarResultado(): Int {
    var casillasAbiertas = 0
    var minasMarcadasCorrectamente = 0 // Casillas que son minas y están marcadas

    for (r in 0 until filas) {
      for (c in 0 until columnas) {
        val casilla = tablero[r][c]

        // Prioridad 1: Verificar si se ha abierto una mina.
        if (casilla.isAbierta() && casilla.isMina()) {
          return 0 // Resultado 0: Perdió (mina explotada)
        }

        // Contar casillas abiertas (si no es una mina abierta)
        if (casilla.isAbierta()) {
          casillasAbiertas++
        }

        // Contar minas correctamente marcadas
        if (casilla.isMarcada() && casilla.isMina()) {
          minasMarcadasCorrectamente++
        }
      }
    }

    // Si llegamos aquí, no se ha abierto ninguna mina.
    // Verificar condiciones de victoria.

    // Condición de victoria: Todas las minas están correctamente marcadas.
    if (minasMarcadasCorrectamente == numeroMinas) {
      return 1 // Resultado 1: Ganó (marcó todas las minas)
    }

    // Condición de victoria: Todas las casillas seguras (sin minas) están abiertas.
    // Esto se comprueba después de la condición de marcar todas las minas,
    // siguiendo la lógica del `else if` en C++.
    val totalCasillasSeguras = (filas * columnas) - numeroMinas
    if (casillasAbiertas == totalCasillasSeguras) {
      return 2 // Resultado 2: Ganó (abrió todas las seguras)
    }

    // Si ninguna de las condiciones anteriores se cumple.
    return 3 // Resultado 3: Partida en progreso / otro estado
  }
}
