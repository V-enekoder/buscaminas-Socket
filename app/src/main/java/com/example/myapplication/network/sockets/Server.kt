package com.example.myapplication

import com.example.myapplication.network.sockets.ClientHandler
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class Server(private val listener: ClientHandler.ClienteConectadoListener) : Runnable {
  private val port: Int = 5200
  private var serverSocket: ServerSocket? = null

  override fun run() {
    serverSocket = ServerSocket(port)
    try {
      while (true) {
        val socket: Socket = serverSocket!!.accept()
        val cliente = ClientHandler(socket)
        ClientHandler.clientes.add(cliente)

        listener.onClientCountChanged(ClientHandler.clientes.size)

        thread { cliente.run() }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
