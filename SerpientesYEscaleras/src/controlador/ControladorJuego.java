package controlador;

import modelo.Juego;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import vista.VistaConsola;

 // Coordina el flujo de la partida entre VistaConsola y Juego: configura
 // la cantidad y nombres de jugadores, inicia la partida y ejecuta el bucle de
 // turnos hasta que haya un ganador.


public class ControladorJuego {

    // Referencia inmutable al modelo (Juego); el controlador lo usa para ejecutar acciones y consultar estado
    private final Juego modelo;
    // Referencia inmutable a la vista (VistaConsola); el controlador la usa para pedir datos y mostrar errores
    private final VistaConsola vista;

    public ControladorJuego(Juego modelo, VistaConsola vista) {
        if (modelo == null || vista == null) {
            throw new IllegalArgumentException("El modelo y la vista no pueden ser nulos.");
        }
        this.modelo = modelo;
        this.vista = vista;
    }


    public void ejecutar() {
        try {
            // pide cantidad y nombres de jugadores y los registra en el modelo
            configurarJugadores();
            //le indica al modelo que valide lo cargado, cree el primer turno y marque la partida como iniciada
            modelo.iniciar();
            //ejecuta el ciclo de turnos hasta que el juego marque finalizado == true
            buclePrincipalDeJuego();

        } catch (JuegoException e) {
            vista.mostrarError(e.getMessage());
        }
    }


    private void configurarJugadores() {
        boolean cantidadValida = false;
        int cantidad = 0;

        while (!cantidadValida) { //si la cantidad de jugadores no es valida se solicita denuevo
            cantidad = vista.pedirCantidadJugadores();
            try {
                modelo.validarCantidadJugadores(cantidad);
                cantidadValida = true;

            } catch (CantidadJugadoresInvalidaException e) {
                vista.mostrarError(e.getMessage());
            }
        }

        int i = 1;
        while (i <= cantidad) {  //registramos el nombre de cada jugador yllamo al modelo para
                                 // que lo agregue a la lista de jugadores
            String nombre = vista.pedirNombreJugador(i);
            try {
                modelo.agregarJugador(nombre);
                i++;
            } catch (JuegoException e) {
                vista.mostrarError(e.getMessage());
            }
        }
    }


    private void buclePrincipalDeJuego() {
        while (!modelo.isFinalizado()) {//ejecuta los turnos esperando al ENTER, hasta que juego marque finalizado = true
            vista.esperarEnterParaTirar(modelo.getJugadorActual().getNombre());
            try {
                modelo.jugarTurno(); //el modelo lanza dado, aplica reglas y notifica a los observadores suscritos
            } catch (JuegoException e) {
                vista.mostrarError(e.getMessage());
                break;
            }
        }
    }
}