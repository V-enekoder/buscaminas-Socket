package com.example.myapplication.network.sockets

fun main() {
  /*
      val ui = Visual()
  <<<<<<< HEAD
      val miTablero = Tablero(filas = 8, columnas = 8, numeroMinas = 10)
      val direccion = Direccion()


      println("Desea iniciar como servidor?")
      println("1. Si   2. No")
      var option = readLine()
      val direccionIP = direccion.getIPAddress()

      if(option?.toIntOrNull() == 1 ){
          println("Ahcaraay sos server")
          println("Direccion IP: $direccionIP")
          thread { Server().run() }

      }else{
          println("Ahcaraaay sos cliente")
      }
      thread { Cliente(direccionIP).run() }
  =======
  >>>>>>> 02bd376da59edaff8379240662eb5f33b566e870

      val opcion = ui.menu()

      when(opcion){
          1 ->{

              val (filas,columnas,minas) = ui.configurarPartida()

              val miTablero = Tablero(filas,columnas,minas)


              while(true){
                  ui.clearScreen()
                  ui.mostrarTablero(miTablero)
                  val accion = ui.seleccionarAccion()

                  if(accion == 4){
                      break
                  }
                  val (fila,columna) = ui.ingresarJugada(miTablero.getFilas(),miTablero.getColumnas())

                  val casilla = miTablero.getCasilla(fila, columna)!!

                  when(accion){
                      1->{
                          if(!casilla.isAbierta() && casilla.isDisponible()){
                              miTablero.abrirCasilla(fila, columna)
                          }
                      }
                      2->{
                          if(!casilla.isMarcada() && casilla.isDisponible()){
                              miTablero.marcarCasilla(fila,columna)
                          }
                      }
                      3->{
                          if(casilla.isMarcada()){
                              miTablero.desmarcarCasilla(fila,columna)
                          }
                      }
                      else->{
                          println("Error: Acción desconocida")
                      }
                  }
              }
          }
      }
      ui.clearScreen()
      print("Juego terminado")
      ]
       */
}

// Gana el que tenga mas puntos, si hay empate, gana el que mas banderas tenga. una bandera por
// turno las banderas correctas suman puntos
// Debe hhaber 3 niveles y personalizado
