package com.example.myapplication.network.sockets

import java.net.NetworkInterface

class Direccion {
  fun getIPAddress(): String {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      while (interfaces.hasMoreElements()) {
        val networkInterface = interfaces.nextElement()
        val addresses = networkInterface.inetAddresses
        while (addresses.hasMoreElements()) {
          val inetAddress = addresses.nextElement()
          if (!inetAddress.isLoopbackAddress && !inetAddress.hostAddress.contains(':')) {
            return inetAddress.hostAddress
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return "No se encontró IP"
  }
}
