package com.example.myapplication.network.sockets

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientHandler(sock: Socket) : Runnable {
  companion object {
    var clientes: MutableList<ClientHandler> = mutableListOf()
  }

  private var socket = sock
  private var clientSocket: Socket? = null
  private var dis: BufferedReader? = null
  private var dos: PrintWriter? = null

  override fun run() {
    clientSocket = socket
    dis = BufferedReader(InputStreamReader(socket!!.getInputStream()))
    dos = PrintWriter(socket!!.getOutputStream(), true)

    // clientes.add(this)

    while (socket.isConnected) {
      try {
        var mensaje: String = dis!!.readLine()
        enviarMensaje(mensaje)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun enviarMensaje(msj: String) {
    for (cliente in clientes) {
      try {
        cliente.dos?.println(msj)
      } catch (e: Exception) {}
    }
  }

  interface ClienteConectadoListener {
    fun onClientCountChanged(conexiones: Int)
  }
}
