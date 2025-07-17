package com.example.myapplication.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.network.sockets.Cliente
import kotlin.concurrent.thread

class ClientActivity : AppCompatActivity() {
  private lateinit var etNombreCliente: EditText
  private lateinit var etIpAddress: EditText
  private lateinit var btnConnect: Button
  private lateinit var client: Cliente
  private var direccionIP: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_client)

    // 1. Inicializamos las vistas usando findViewById
    etIpAddress = findViewById(R.id.etIpAddress)
    btnConnect = findViewById(R.id.btnConnect)
    etNombreCliente = findViewById(R.id.etNombreCliente)

    // 2. Configuramos el listener para el botón de conectar
    btnConnect.setOnClickListener { handleConnectButtonClick() }
  }

  private fun handleConnectButtonClick() {
    direccionIP = etIpAddress.text.toString().trim()
    val direccionIP = etIpAddress.text.toString().trim()
    val nombreJugador = etNombreCliente.text.toString().trim() // Obtener el nombre

    // 3. Validamos que AMBOS campos no estén vacíos
    if (nombreJugador.isEmpty()) {
      Toast.makeText(this, "Por favor, introduce tu nombre", Toast.LENGTH_SHORT).show()
      etNombreCliente.error = "Campo requerido"
      return
    }

    if (direccionIP.isEmpty()) {
      Toast.makeText(this, "Por favor, introduce una dirección IP", Toast.LENGTH_SHORT).show()
      etIpAddress.error = "Campo requerido"
      return
    }

    val message = "Conectando como '$nombreJugador' a la IP: $direccionIP"
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    // --- CAMBIO CLAVE: Pasar el nombre al constructor de Cliente ---
    // Debes asegurarte de que tu clase Cliente acepte este nuevo parámetro.
    client = Cliente(direccionIP, 2, nombreJugador)
    MainActivity.Sockets.clienteU = client
    client.setContext(this)
    thread { client.run() }
  }
}
