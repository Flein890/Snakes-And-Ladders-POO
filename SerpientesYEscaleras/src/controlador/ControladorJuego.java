package controlador;

import modelo.Juego;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import vista.VistaConsola;

/**
 * QUÉ HACE: Coordina el flujo de la partida entre VistaConsola y Juego: configura
 * la cantidad y nombres de jugadores, inicia la partida y ejecuta el bucle de
 * turnos hasta que haya un ganador.
 *
 * POR QUÉ SE HACE: Separa la orquestación del flujo de usuario y el manejo de
 * excepciones del dominio (JuegoException) tanto de las reglas internas del modelo
 * como de la presentación en consola.
 */
public class ControladorJuego {

    private final Juego modelo;
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
            configurarJugadores();
            modelo.iniciar();
            buclePrincipalDeJuego();
        } catch (JuegoException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    /**
     * QUÉ HACE: Solicita una cantidad válida de jugadores (2 a 4) y registra cada
     * nombre en el modelo, reintentando ante entradas inválidas.
     *
     * POR QUÉ SE HACE: Atrapa CantidadJugadoresInvalidaException y JuegoException
     * para informar el error por la vista sin cortar el flujo de configuración.
     */
    private void configurarJugadores() {
        boolean cantidadValida = false;
        int cantidad = 0;

        while (!cantidadValida) {
            cantidad = vista.pedirCantidadJugadores();
            try {
                modelo.validarCantidadJugadores(cantidad);
                cantidadValida = true;
            } catch (CantidadJugadoresInvalidaException e) {
                vista.mostrarError(e.getMessage());
            }
        }

        int i = 1;
        while (i <= cantidad) {
            String nombre = vista.pedirNombreJugador(i);
            try {
                modelo.agregarJugador(nombre);
                i++;
            } catch (JuegoException e) {
                vista.mostrarError(e.getMessage());
            }
        }
    }

    /**
     * QUÉ HACE: Ejecuta los turnos de forma secuencial esperando ENTER de cada
     * jugador hasta que el juego marque finalizado == true.
     *
     * POR QUÉ SE HACE: Mantiene el bucle de interacción en el controlador mientras
     * delega cada tiro y sus reglas a modelo.jugarTurno().
     */
    private void buclePrincipalDeJuego() {
        while (!modelo.isFinalizado()) {
            vista.esperarEnterParaTirar(modelo.getJugadorActual().getNombre());
            try {
                modelo.jugarTurno();
            } catch (JuegoException e) {
                vista.mostrarError(e.getMessage());
                break;
            }
        }
    }
}