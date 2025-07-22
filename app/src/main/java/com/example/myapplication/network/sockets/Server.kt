package com.example.myapplication

//import com.example.myapplication.network.sockets.ClientHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import java.io.PrintWriter

class Server(private val listener: ClienteConectadoListener) : Runnable {
  private val port: Int = 5200
  private var serverSocket: ServerSocket? = null
  private val clientes: MutableList<PrintWriter> = mutableListOf()

  override fun run() {
    serverSocket = ServerSocket(port)
    try {
      while (true) {
        val socket: Socket = serverSocket!!.accept() // Acepta un nuevo cliente

        thread {
          gestionar(socket)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun gestionar(socket: Socket) {
    try {
      val dis = BufferedReader(InputStreamReader(socket.getInputStream()))
      val dos = PrintWriter(socket.getOutputStream(), true)

      synchronized(clientes) {
        clientes.add(dos)
      }
      listener.onClientCountChanged(clientes.size)

      while (socket.isConnected) {
        val mensaje = dis.readLine()
        if (mensaje != null) {
          broadcast(mensaje)
        } else {
          break
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      println("Cliente desconectado.")
      socket.close()
    }
  }

  private fun broadcast(msj: String) {
    synchronized(clientes) {
      for (clienteDos in clientes) {
        try {
          clienteDos.println(msj)
        } catch (e: Exception) {
        }
      }
    }
  }

  interface ClienteConectadoListener {
    fun onClientCountChanged(conexiones: Int)
  }
}