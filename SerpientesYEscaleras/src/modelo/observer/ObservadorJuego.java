package modelo.observer;

import java.util.List;
import modelo.Jugador;

/**
 * QUÉ HACE: Define el contrato (interfaz) que debe implementar cualquier clase que
 * desee recibir notificaciones cuando cambie el estado del juego.
 *
 * POR QUÉ SE HACE:
 * 1. Desacoplamiento MVC: El Modelo (Juego) solo conoce esta interfaz, nunca conoce a
 *    VistaConsola. Así podrías cambiar la consola por una interfaz gráfica Swing o JavaFX
 *    sin modificar ni una línea del Modelo.
 */
public interface ObservadorJuego {

    // Se dispara cada vez que un nuevo jugador se incorpora válidamente a la partida
    void onJugadorAgregado(Jugador jugador, int totalActual);

    // Se dispara cuando la partida cumple las validaciones y comienza oficialmente
    void onJuegoIniciado(List<Jugador> jugadores);

    // Se dispara durante un turno cada vez que ocurre un suceso (tiro de dado, escalera, serpiente, rebote)
    void onEventoTurno(String descripcionEvento);

    // Se dispara al finalizar el movimiento de un turno para mostrar el estado actualizado de posiciones
    void onEstadoActualizado(List<Jugador> jugadores);

    // Se dispara cuando un jugador alcanza exactamente la casilla 100
    void onJuegoFinalizado(Jugador ganador);
}