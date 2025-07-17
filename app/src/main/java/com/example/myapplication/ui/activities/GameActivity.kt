package com.example.myapplication.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import com.example.myapplication.R
import com.example.myapplication.game.core.Casilla
import com.example.myapplication.game.core.Jugador
import com.example.myapplication.game.core.Tablero
import com.example.myapplication.network.sockets.GameEventsListener

class GameActivity : AppCompatActivity(), GameEventsListener {
  private var gameConfig: ConfiguracionTablero? = null
  private val CELL_SIZE_DP = 40 // Tamaño de cada celda en DP

  private lateinit var matrixGridLayout: GridLayout
  private lateinit var actionSpinner: Spinner
  private lateinit var rowEditText: EditText
  private lateinit var columnEditText: EditText
  private lateinit var sendMoveButton: Button

  private lateinit var tableroLogico: Tablero

  private lateinit var cellViews: Array<Array<TextView>>

  private var juegoActivo = true // Para saber si el juego ha terminado
  private var toastActual: Toast? = null

  private var turnoActual = 1 // El juego siempre empieza en el turno 1
  private var miTurno = -1

  private var puntuacionOponente: Int = 0
  private var banderasOponente: Int = 0

  private val cliente = MainActivity.Sockets.clienteU

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge() // Para el diseño Edge-to-Edge
    setContentView(R.layout.activity_game) // Carga el XML

    recuperarConfiguracion()
    if (gameConfig == null) {
      Toast.makeText(
              this, "Error: No se pudo cargar la configuración del juego.", Toast.LENGTH_LONG)
          .show()
      finish()
      return
    }

    val mainContainer = findViewById<View>(R.id.main_container)
    ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    // Obtenemos nuestro número de turno desde nuestra instancia de cliente
    miTurno = cliente?.getTurno() ?: -1
    if (miTurno == -1) {
      mostrarToast("Error: No se pudo identificar el turno del jugador.")
      finish()
      return
    }

    runOnUiThread { cliente?.setMoveListener(this) }
    inicializarVistas()
    iniciarNuevoJuego()
    setupSpinner()
    setupButtonListener()
  }

  private fun recuperarConfiguracion() {
    gameConfig =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          // Método moderno y seguro para Android 13 (API 33) y superior
          intent.getSerializableExtra("GAME_CONFIG", ConfiguracionTablero::class.java)
        } else {
          // Método antiguo (obsoleto) para versiones anteriores
          @Suppress("DEPRECATION")
          intent.getSerializableExtra("GAME_CONFIG") as? ConfiguracionTablero
        }

    // Log para depuración
    if (gameConfig == null) {
      Log.e("GameActivity", "¡ERROR! No se recibió la configuración del juego en el Intent.")
    }
  }

  private fun inicializarVistas() {
    matrixGridLayout = findViewById(R.id.matrixGridLayout)
    actionSpinner = findViewById(R.id.actionSpinner)
    rowEditText = findViewById(R.id.rowEditText)
    columnEditText = findViewById(R.id.columnEditText)
    sendMoveButton = findViewById(R.id.sendMoveButton)
  }

  private fun iniciarNuevoJuego() {
    val config = gameConfig!!

    // 1. Crear la instancia del MODELO
    tableroLogico =
        Tablero(
            config.filas,
            config.columnas,
            config.posicionesMinas!!,
            "Jugador ${cliente?.getTurno()}",
            miTurno)
    juegoActivo = true

    // 2. Crear la VISTA inicial
    setupGameGrid() // Crea los TextViews
    actualizarVistaTablero() // Dibuja el estado inicial del tablero (todo oculto)
  }

  private fun setupGameGrid() {
    val config = gameConfig!!
    matrixGridLayout.removeAllViews() // Limpiar el tablero si se reinicia el juego
    matrixGridLayout.rowCount = config.filas
    matrixGridLayout.columnCount = config.columnas
    cellViews = Array(config.filas) { Array(config.columnas) { TextView(this) } }
    val cellSizePx = (CELL_SIZE_DP * resources.displayMetrics.density).toInt()

    for (row in 0 until config.filas) {
      for (col in 0 until config.columnas) {
        val cellView =
            TextView(this).apply {
              layoutParams =
                  GridLayout.LayoutParams().apply {
                    width = cellSizePx
                    height = cellSizePx
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                    setMargins(2, 2, 2, 2)
                  }
              gravity = Gravity.CENTER
              textSize = 18f
              setOnClickListener {
                rowEditText.setText(row.toString())
                columnEditText.setText(col.toString())
              }
            }
        cellViews[row][col] = cellView
        matrixGridLayout.addView(cellView)
      }
    }
  }

  private fun setupSpinner() {
    ArrayAdapter.createFromResource(
            this, R.array.move_actions, android.R.layout.simple_spinner_item)
        .also { adapter ->
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
          actionSpinner.adapter = adapter
        }
  }

  private fun setupButtonListener() { // Aqui se manda la jugada
    sendMoveButton.setOnClickListener {
      if (!juegoActivo) {
        mostrarToast("El juego ha terminado.")
        return@setOnClickListener
      }

      val rowStr = rowEditText.text.toString()
      val colStr = columnEditText.text.toString()

      if (rowStr.isEmpty() || colStr.isEmpty()) {
        mostrarToast("Coordenadas inválidas.")
        return@setOnClickListener
      }

      val casillaCandidata: Casilla? = tableroLogico.getCasilla(rowStr.toInt(), colStr.toInt())

      if (!casillaCandidata!!.isDisponible()) {
        mostrarToast("Casilla No Disponible.")
        return@setOnClickListener
      }


      // 3. Construye el mensaje con la jugada
      val action =
          when (actionSpinner.selectedItemPosition) {
            0 -> "REVEAL" // Usaremos strings para que sea más legible
            1 -> "FLAG"
            2 -> "UNFLAG"
            else -> ""
          }

      realizarJugada(action,rowStr.toInt(),colStr.toInt(),turnoActual)
      val miTurno = cliente?.getTurno() ?: -1 // Obtenemos el turno del cliente
      val puntuacion: Int = tableroLogico.getJugador().puntuacion
      val casillas: Int = tableroLogico.getJugador().casillasAbiertas
      val banderas: Int = tableroLogico.getJugador().banderasCorrectas

      val mensajeMovimiento = "MOVE $miTurno $action ${rowStr}_${colStr} $puntuacion $turnoActual $casillas $banderas"


      // 4. Envía la jugada al servidor (que la reenviará a todos)
      Thread { cliente?.enviarMensaje(mensajeMovimiento) }.start()
    }
  }

  override fun onMoveReceived(
      turno: Int,
      action: String,
      row: Int,
      col: Int,
      puntuacion: Int,
      ultimoTurno: Int,
      casillas: Int,
      banderas: Int
  ) {
    if(turno == miTurno){
      return
    }
    runOnUiThread {
      if (!juegoActivo) return@runOnUiThread // No procesar si el juego ya terminó

      // --- Le dice al MODELO LOCAL qué hacer ---
      val resultadoJugada: Int =
          when (action) {
            "REVEAL" -> tableroLogico.abrirCasilla(row, col, turno)
            "FLAG" -> tableroLogico.marcarCasilla(row, col, turno)
            "UNFLAG" ->
                tableroLogico.desmarcarCasilla(
                    row, col, turno) // No esta quitando la bandera
            else -> 0
          }

      // --- Pide a la VISTA que se actualice con los cambios del modelo local ---
      actualizarVistaTablero()

      if (resultadoJugada == -1) {
        juegoActivo = false
        revelarTableroCompleto()
      }

      if (turno != miTurno) {
        puntuacionOponente = puntuacion
        banderasOponente = banderas
      }

      turnoActual = if (turno == 1) 2 else 1

      // 2. Actualizamos el estado del botón basándonos en el nuevo turno.
      actualizarEstadoBoton()

      // --- Comprueba el estado del juego después de la jugada ---
      verificarEstadoDelJuego(ultimoTurno)
    }
  }

  fun realizarJugada(
    action: String,
    row: Int,
    col: Int,
    ultimoTurno: Int
  ) {
    runOnUiThread {
      if (!juegoActivo) return@runOnUiThread // No procesar si el juego ya terminó

      // --- Le dice al MODELO LOCAL qué hacer ---
      val resultadoJugada: Int =
        when (action) {
          "REVEAL" -> tableroLogico.abrirCasilla(row, col, miTurno)
          "FLAG" -> tableroLogico.marcarCasilla(row, col, miTurno)
          "UNFLAG" ->
            tableroLogico.desmarcarCasilla(
              row, col, miTurno) // No esta quitando la bandera
          else -> 0
        }

      // --- Pide a la VISTA que se actualice con los cambios del modelo local ---
      actualizarVistaTablero()

      if (resultadoJugada == -1) {
        juegoActivo = false
        revelarTableroCompleto()
      }

      turnoActual = if (miTurno == 1) 2 else 1

      // 2. Actualizamos el estado del botón basándonos en el nuevo turno.
      actualizarEstadoBoton()

      // --- Comprueba el estado del juego después de la jugada ---
      verificarEstadoDelJuego(ultimoTurno)
    }
  }
  private fun actualizarEstadoBoton() {
    // La condición es simple: ¿El turno actual es mi turno?
    if (turnoActual == miTurno) {
      // ¡Es mi turno!
      sendMoveButton.isEnabled = true
      sendMoveButton.text = "ENVIAR JUGADA" // Texto normal
      // Opcional: cambiar el color para que sea más obvio
      sendMoveButton.setBackgroundColor("#FF6200EE".toColorInt()) // Color principal
    } else {
      // No es mi turno, tengo que esperar.
      sendMoveButton.isEnabled = false
      sendMoveButton.text = "ESPERANDO AL OPONENTE..." // Informar al usuario
      // Opcional: cambiar el color a un gris
      sendMoveButton.setBackgroundColor(Color.GRAY)
    }
  }

  // --- VISTA: Función clave para sincronizar la UI con el estado del Modelo ---
  private fun actualizarVistaTablero() {
    val config = gameConfig!!
    for (r in 0 until config.filas) {
      for (c in 0 until config.columnas) {
        val casillaLogica = tableroLogico.getCasilla(r, c)!!
        val cellView = cellViews[r][c]

        cellView.text = "" // Limpiar texto anterior
        cellView.setBackgroundColor(Color.DKGRAY) // Color por defecto de casilla oculta

        if (casillaLogica.isMarcada()) {
          cellView.text = "🚩" // Emoji de bandera
          cellView.setBackgroundColor(Color.CYAN)
        } else if (casillaLogica.isAbierta()) {
          // La casilla está abierta, mostrar su contenido
          cellView.setBackgroundColor(Color.LTGRAY)
          if (casillaLogica.isMina()) {
            // cellView.text = "M"
            cellView.text = "💣" // Emoji de bomba
            cellView.setBackgroundColor(Color.RED)
          } else if (casillaLogica.getMinasAlrededor() > 0) {
            cellView.text = casillaLogica.getMinasAlrededor().toString()
          } else {
            // Casilla vacía y abierta, no mostrar nada
            cellView.text = ""
          }
        }
      }
    }
  }

  private fun verificarEstadoDelJuego(ultimoTurno: Int) {
    val resultado = tableroLogico.verificarResultado()

    // El juego termina si el resultado es 0 (derrota), 1 (victoria por banderas) o 2 (victoria por
    // casillas)
    if (resultado != 3) {
      juegoActivo = false
      sendMoveButton.isEnabled = false // Desactivar botón si lo tienes

      // Asumimos que getJugador().toString() devuelve "Nombre;Puntuacion"
      // Creamos el string combinado: "Nombre;Puntuacion;resultado"
      val puntuacion: Int = tableroLogico.getJugador().puntuacion
      val banderas: Int = tableroLogico.getJugador().banderasCorrectas

      val datosJuego: String =
          tableroLogico.getJugador().nombre +
              ";$puntuacion" +
              ";$resultado" +
              ";$puntuacionOponente" +
              ";$miTurno" +
              ";$ultimoTurno"+
                  ";$banderas"+
                  ";$banderasOponente"

      // Creamos el Intent para iniciar EndActivity
      val intent = Intent(this, EndActivity::class.java)

      // Añadimos el string combinado como un extra.
      // Usaremos una clave constante para evitar errores.
      intent.putExtra(EndActivity.EXTRA_DATOS_JUEGO, datosJuego)

      // Iniciamos la nueva activity
      startActivity(intent)

      // Cerramos la activity del juego
      finish()
    }
  }

  private fun mostrarToast(mensaje: String, duracion: Int = Toast.LENGTH_SHORT) {
    // Paso 2: Cancelar el Toast anterior si existe
    toastActual?.cancel()

    // Paso 3: Crear el nuevo Toast, mostrarlo y guardarlo en la variable
    toastActual = Toast.makeText(this, mensaje, duracion)
    toastActual?.show()
  }

  private fun revelarTableroCompleto() {
    val config = gameConfig!!
    for (r in 0 until config.filas) {
      for (c in 0 until config.columnas) {
        tableroLogico.getCasilla(r, c)?.abrir()
      }
    }
    actualizarVistaTablero() // Vuelve a dibujar el tablero con todo revelado
  }
}
