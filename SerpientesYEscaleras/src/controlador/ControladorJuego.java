package controlador;

import modelo.Juego;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import vista.VistaConsola;

/**
 * QUÉ HACE: Coordina el flujo de ejecución de la aplicación conectando la Vista con el Modelo.
 *
 * POR QUÉ SE HACE: Evita que la Vista manipule directamente al Modelo y viceversa.
 * Recibe las peticiones del usuario desde VistaConsola, invoca las operaciones en Juego,
 * y captura las excepciones de negocio para informárselas a la Vista.
 */
public class ControladorJuego {

    private final Juego modelo;
    private final VistaConsola vista;

    public ControladorJuego(Juego modelo, VistaConsola vista) {
        this.modelo = modelo;
        this.vista = vista;
        // Suscribimos la Vista como observadora del Modelo al momento de crear el Controlador
        this.modelo.agregarObservador(this.vista);
    }

    /**
     * QUÉ HACE: Ejecuta el ciclo completo de vida de una partida: configuración, inicio y turnos.
     * POR QUÉ SE HACE: Mantiene el método main() limpio y delega toda la orquestación de
     * la aplicación al Controlador.
     */
    public void ejecutar() {
        vista.mostrarBienvenida();
        configurarJugadores();
        buclePrincipalDeJuego();
    }

    /**
     * QUÉ HACE: Pide la cantidad de jugadores y sus nombres hasta que el Modelo valide que
     * existen entre 2 y 4 participantes y autorice el inicio de la partida.
     * POR QUÉ SE HACE: Delega la regla de "mínimo 2, máximo 4" al Modelo (Juego) y maneja
     * los reintentos de forma amigable si el usuario ingresa un valor inválido.
     */
    private void configurarJugadores() {
        int cantidadValida = 0;

        // Paso 1: Validar que la cantidad ingresada cumpla la regla [2..4] del Modelo
        while (cantidadValida == 0) {
            int cantidadIngresada = vista.pedirCantidadJugadores();
            try {
                modelo.validarCantidadJugadores(cantidadIngresada);
                cantidadValida = cantidadIngresada;
            } catch (CantidadJugadoresInvalidaException e) {
                vista.mostrarError(e.getMessage());
            }
        }

        // Paso 2: Cargar uno por uno los nombres de los jugadores validados
        int registrados = 0;
        while (registrados < cantidadValida) {
            String nombre = vista.pedirNombreJugador(registrados + 1);
            try {
                modelo.agregarJugador(nombre);
                registrados++;
            } catch (JuegoException e) {
                // Si el nombre estaba vacío o se excedió el límite, muestra el error y vuelve a pedirlo
                vista.mostrarError(e.getMessage());
            }
        }

        // Paso 3: Iniciar formalmente la partida en el Modelo (dispara onJuegoIniciado en la Vista)
        try {
            modelo.iniciar();
        } catch (CantidadJugadoresInvalidaException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    /**
     * QUÉ HACE: Ejecuta los turnos sucesivos hasta que el Modelo indique que hay un ganador.
     * POR QUÉ SE HACE: Mantiene el control del flujo de juego sin conocer detalles internos
     * de cómo se calcula el movimiento o cómo se imprime el resultado.
     */
    private void buclePrincipalDeJuego() {
        while (!modelo.isFinalizado()) {
            String nombreTurno = modelo.getJugadorActual().getNombre();
            vista.esperarEnterParaTirar(nombreTurno);
            try {
                modelo.jugarTurno();
            } catch (JuegoException e) {
                vista.mostrarError(e.getMessage());
            }
        }
    }
}