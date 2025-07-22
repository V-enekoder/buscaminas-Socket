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
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.game.core.Casilla
import com.example.myapplication.game.core.Jugador
import com.example.myapplication.game.core.Tablero
import com.example.myapplication.network.sockets.GameEventsListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity(), GameEventsListener {
  private var gameConfig: ConfiguracionTablero? = null
  private val CELL_SIZE_DP = 40 // Tamaño de cada celda en DP

  private lateinit var matrixGridLayout: GridLayout
  private lateinit var actionSpinner: Spinner
  private lateinit var rowEditText: EditText
  private lateinit var columnEditText: EditText
  private lateinit var sendMoveButton: Button

  private lateinit var tablero: Tablero

  private lateinit var cellViews: Array<Array<TextView>>

  private var juegoActivo = true // Para saber si el juego ha terminado
  private var toastActual: Toast? = null

  private var turnoActual = 1 // El juego siempre empieza en el turno 1
  private var miTurno = -1

  private var puntuacionOponente: Int = 0
  private var banderasOponente: Int = 0
  private var nombreOponente: String = ""

  private val cliente = MainActivity.Sockets.clienteU

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge() // Para el diseño Edge-to-Edge
    setContentView(R.layout.activity_game) // Carga el XML

    recuperarConfiguracion()
    if (gameConfig == null) {
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

    matrixGridLayout = findViewById(R.id.matrixGridLayout)
    actionSpinner = findViewById(R.id.actionSpinner)
    rowEditText = findViewById(R.id.rowEditText)
    columnEditText = findViewById(R.id.columnEditText)
    sendMoveButton = findViewById(R.id.sendMoveButton)

    val config = gameConfig!!
    
    tablero =
      Tablero(
        config.filas,
        config.columnas,
        config.posicionesMinas!!,
        cliente?.nombre ?: "Player",
        miTurno)
    juegoActivo = true

    // 2. Crear la VISTA inicial
    setupGameGrid() // Crea los TextViews
    actualizarTablero()
    setupSpinner()
    setupButtonListener()
  }

  private fun recuperarConfiguracion() {
    gameConfig =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          // Método moderno y seguro para Android 13 (API 33) y superior
          intent.getSerializableExtra("CONFIG", ConfiguracionTablero::class.java)
        } else {
          // Método antiguo (obsoleto) para versiones anteriores
          @Suppress("DEPRECATION")
          intent.getSerializableExtra("CONFIG") as? ConfiguracionTablero
        }
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

  private fun setupButtonListener() { // turno = 1
    sendMoveButton.setOnClickListener {
      if (!juegoActivo) {
        mostrarToast("El juego ha terminado.")
        return@setOnClickListener
      }

      val rowStr = rowEditText.text.toString()
      val colStr = columnEditText.text.toString()

      if (rowStr.isEmpty() || colStr.isEmpty()) {
        return@setOnClickListener
      }

      val casillaCandidata: Casilla? = tablero.getCasilla(rowStr.toInt(), colStr.toInt())

      if (casillaCandidata!!.isAbierta()) {
        mostrarToast("Casilla No Disponible.")
        return@setOnClickListener
      }


      // 3. Construye el mensaje con la jugada
      val action =
          when (actionSpinner.selectedItemPosition) {
            0 -> "ABRIR" // Usaremos strings para que sea más legible
            1 -> "MARCAR"
            2 -> "DESMARCAR"
            else -> ""
          }

      realizarJugada(action,rowStr.toInt(),colStr.toInt(),turnoActual)//turno = 2
      val miTurno = cliente?.getTurno() ?: -1 // Obtenemos el turno del cliente
      val puntuacion: Int = tablero.getJugador().puntuacion
      val casillas: Int = tablero.getJugador().casillasAbiertas
      val banderas: Int = tablero.getJugador().banderasCorrectas
      val nombre: String = tablero.getJugador().nombre
      val mensajeMovimiento = "JUGADA $miTurno $action ${rowStr}_${colStr} $puntuacion $miTurno $casillas $banderas $nombre"

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
      banderas: Int,
      nombre: String
  ) {
    if(turno == miTurno){
      return
    }
    runOnUiThread {
      if (!juegoActivo) return@runOnUiThread // No procesar si el juego ya terminó

      // --- Le dice al MODELO LOCAL qué hacer ---
      val resultadoJugada: Int =
          when (action) {
            "ABRIR" -> tablero.abrirCasilla(row, col, turno)
            "MARCAR" -> tablero.marcarCasilla(row, col, turno)
            "DESMARCAR" ->
                tablero.desmarcarCasilla(
                    row, col, turno) // No esta quitando la bandera
            else -> 0
          }

      // --- Pide a la VISTA que se actualice con los cambios del modelo local ---
      actualizarTablero()

      if (turno != miTurno) {
        puntuacionOponente = puntuacion
        banderasOponente = banderas
        nombreOponente = nombre
      }

      if (resultadoJugada == -1) {
        juegoActivo = false
        revelarTableroCompleto()
      }

      turnoActual = if (turno == 1) 2 else 1
      actualizarEstadoBoton()
      verificarEstadoDelJuego(ultimoTurno)
    }
  }

  private fun realizarJugada(
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
          "ABRIR" -> tablero.abrirCasilla(row, col, miTurno)
          "MARCAR" -> tablero.marcarCasilla(row, col, miTurno)
          "DESMARCAR" ->
            tablero.desmarcarCasilla(
              row, col, miTurno) // No esta quitando la bandera
          else -> 0
        }

      // --- Pide a la VISTA que se actualice con los cambios del modelo local ---
      actualizarTablero()

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
  private fun actualizarTablero() {
    val config = gameConfig!!
    for (r in 0 until config.filas) {
      for (c in 0 until config.columnas) {
        val casillaLogica = tablero.getCasilla(r, c)!!
        val cellView = cellViews[r][c]

        cellView.text = "" // Limpiar texto anterior
        cellView.setBackgroundColor(Color.DKGRAY) // Color por defecto de casilla oculta

        if (casillaLogica.isMarcada()) {
          cellView.text = "B" // Emoji de bandera
          cellView.setBackgroundColor(Color.GREEN)
        } else if (casillaLogica.isAbierta()) {
          // La casilla está abierta, mostrar su contenido
          cellView.setBackgroundColor(Color.WHITE)
          if (casillaLogica.isMina()) {
            cellView.text = "M" // Emoji de bomba
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
    val resultado = tablero.verificarResultado()

    // El juego termina si el resultado es 0 (derrota), 1 (victoria por banderas) o 2 (victoria por
    // casillas)
    if (resultado != 3) {
      juegoActivo = false
      sendMoveButton.isEnabled = false // Desactivar botón si lo tienes

      val puntuacion: Int = tablero.getJugador().puntuacion
      val banderas: Int = tablero.getJugador().banderasCorrectas

      // --- INICIO DEL DELAY USANDO COROUTINES ---
      lifecycleScope.launch {
        delay(2000) // Espera 2000 milisegundos (2 segundos)

        // Este código se ejecuta DESPUÉS de los 2 segundos

        // (El resto de tu código para preparar el Intent)
        val puntuacion: Int = tablero.getJugador().puntuacion
        val banderas: Int = tablero.getJugador().banderasCorrectas
        val datosJuego: String =
          tablero.getJugador().nombre +
                  ";$puntuacion" +
                  ";$resultado" +
                  ";$puntuacionOponente" +
                  ";$miTurno" +
                  ";$ultimoTurno"+
                  ";$banderas"+
                  ";$banderasOponente"+ ";$nombreOponente"

        // Creamos el Intent para iniciar EndActivity
        val intent = Intent(this@GameActivity, EndActivity::class.java)
        intent.putExtra(EndActivity.EXTRA_DATOS_JUEGO, datosJuego)

        // Iniciamos la nueva activity y cerramos la actual
        startActivity(intent)
        finish()
      }
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
        tablero.getCasilla(r, c)?.abrir()
      }
    }
    actualizarTablero() // Vuelve a dibujar el tablero con todo revelado
  }
}
