package com.example.myapplication.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.Server
import com.example.myapplication.network.sockets.ClientHandler
import com.example.myapplication.network.sockets.Cliente
import com.example.myapplication.network.sockets.Direccion
import kotlin.concurrent.thread
import android.widget.Button
import android.widget.EditText

class ServerActivity : AppCompatActivity(), ClientHandler.ClienteConectadoListener {

  private val direccion = Direccion()
  private val server = Server(this)
  private lateinit var client: Cliente

  // Declarar las vistas como propiedades de la clase para acceder a ellas fácilmente
  private lateinit var tvIpAddress: TextView
  private lateinit var nombreEditText: EditText
  private lateinit var configurarButton: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_server)

    // 1. Enlazar todas las vistas
    tvIpAddress = findViewById(R.id.tvIpAddress)
    nombreEditText = findViewById(R.id.etNombreServidor)
    configurarButton = findViewById(R.id.btnConfigurarPartida)

    // 2. Estado inicial de la UI
    // El botón empieza desactivado y con un texto que indica que se está esperando.
    configurarButton.isEnabled = false
    configurarButton.text = "Esperando oponente..."

    // 3. Obtener y mostrar la IP
    val ipAddress = direccion.getIPAddress()
    if (ipAddress != null) {
      tvIpAddress.text = ipAddress
    } else {
      tvIpAddress.text = "IP no disponible. ¿Estás en una red WiFi?"
    }

    // 4. Configurar el cliente y servidor (sin cambios)
    client = Cliente(ipAddress, 1, "")
    MainActivity.Sockets.serverU = server
    MainActivity.Sockets.clienteU = client
    thread { server.run() }
    thread { client.run() }

    // 5. Configurar el listener del botón (se ejecutará cuando se active)
    setupButtonListener()
  }

  override fun onClientCountChanged(conexiones: Int) {
    // Esta función se llama desde otro hilo (el del servidor),
    // por lo que cualquier cambio en la UI debe hacerse en el hilo principal.
    runOnUiThread {
      if (conexiones == 2) {
        // Hay 2 jugadores, ¡es hora de activar el botón!
        configurarButton.isEnabled = true
        configurarButton.text = "Configurar Partida"
      } else {
        // Si un cliente se desconecta, volvemos a desactivarlo
        configurarButton.isEnabled = false
        configurarButton.text = "Esperando oponente..."
      }
    }
  }

  private fun setupButtonListener() {
    configurarButton.setOnClickListener {
      val nombreJugador = nombreEditText.text.toString().trim()

      if (nombreJugador.isEmpty()) {
        nombreEditText.error = "El nombre no puede estar vacío"
        return@setOnClickListener
      }

      client.nombre = nombreJugador

      // Navegar a la siguiente pantalla y pasar el nombre
      navigateToGameConfiguration(nombreJugador)
    }
  }

  private fun navigateToGameConfiguration(nombreJugador: String) {
    val intent = Intent(this, GameConfigurationActivity::class.java)
    // Añadimos el nombre del jugador como un "extra" al intent.
    // Es una buena práctica usar una constante para la clave del extra.
    intent.putExtra("NOMBRE_JUGADOR_SERVIDOR", nombreJugador)
    startActivity(intent)
    finish() // Cierra esta actividad para que el usuario no pueda volver atrás
  }
}
