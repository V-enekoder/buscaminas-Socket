package com.example.myapplication.game.core

import java.io.Serializable

class Jugador(nombreInicial: String) : Serializable {

  var nombre: String = nombreInicial
  var puntuacion: Int = 0
    private set

  var casillasAbiertas: Int = 0
  var banderasCorrectas: Int = 0

  constructor(nombreInicial: String, puntuacionInicial: Int) : this(nombreInicial) {
    if (puntuacionInicial >= 0) {
      this.puntuacion = puntuacionInicial
    } else {
      println("La puntuación inicial no puede ser negativa. Se establece a 0.")
      this.puntuacion = 0
    }
  }

  fun aumentarPuntuacion() {
    this.puntuacion++
  }

  fun reducirPuntuacion() {
    this.puntuacion--
  }

  fun aumentarCasillasAbiertas(){
    this.casillasAbiertas++
  }

  fun aumentarBanderasPuestas(){
    this.banderasCorrectas++
  }

  fun reducirBanderasPuestas(){
    this.banderasCorrectas--
  }



  override fun toString(): String {
    return "$nombre;$puntuacion)"
  }
}
