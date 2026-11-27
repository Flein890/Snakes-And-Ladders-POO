package vista;

import java.util.List;
import java.util.Scanner;
import modelo.Jugador;
import modelo.observer.ObservadorJuego;

/**
 * QUÉ HACE: Maneja la interacción por consola con el usuario (entrada de datos y
 * mensajes de error) e implementa ObservadorJuego para mostrar los eventos de la partida.
 *
 * POR QUÉ SE HACE: Mantiene la entrada/salida separada de la lógica de reglas en Juego
 * y reacciona automáticamente a los cambios notificados por el modelo.
 */
public class VistaConsola implements ObservadorJuego {

    private final Scanner scanner;

    public VistaConsola() {
        this.scanner = new Scanner(System.in);
    }

    public void mostrarBienvenida() {
        System.out.println("==========================================");
        System.out.println("      SERPIENTES Y ESCALERAS              ");
        System.out.println("==========================================");
    }

    /**
     * QUÉ HACE: Solicita y valida que el usuario elija un tablero de 64, 100 o 144 casillas.
     *
     * POR QUÉ SE HACE: Asegura que el controlador reciba un tamaño válido antes de crear
     * o iniciar el tablero de la partida.
     */
    public int pedirTamanoTablero() {
        while (true) {
            System.out.print("Elegí el tamaño del tablero (64, 100 o 144 casillas): ");
            String entrada = scanner.nextLine().trim();
            try {
                int tamano = Integer.parseInt(entrada);
                if (tamano == 64 || tamano == 100 || tamano == 144) {
                    return tamano;
                }
                mostrarError("Opción no válida. Elegí entre 64, 100 o 144.");
            } catch (NumberFormatException e) {
                mostrarError("Ingresá un número válido (64, 100 o 144).");
            }
        }
    }

    public int pedirCantidadJugadores() {
        while (true) {
            System.out.print("Ingresá la cantidad de jugadores (2 a 4): ");
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                mostrarError("Ingresá un número entero válido.");
            }
        }
    }

    public String pedirNombreJugador(int numeroJugador) {
        System.out.print("Nombre del jugador " + numeroJugador + ": ");
        return scanner.nextLine().trim();
    }

    public void esperarEnterParaTirar(String nombreJugador) {
        System.out.print("\nTurno de " + nombreJugador + ". Presioná ENTER para lanzar el dado...\n");
        scanner.nextLine();
    }

    public void mostrarError(String mensajeError) {
        System.out.println("[ERROR] " + mensajeError);
    }

    // --- IMPLEMENTACIÓN DE ObservadorJuego ---

    @Override
    public void onJugadorAgregado(Jugador jugador, int totalActual) {
        System.out.println("Jugador registrado: " + jugador.getNombre() + " (total: " + totalActual + ")");
    }

    @Override
    public void onJuegoIniciado(List<Jugador> jugadores) {
        System.out.println("\n¡Comienza la partida! Jugadores en carrera:");
        for (Jugador j : jugadores) {
            System.out.println(" - " + j.getNombre() + " (casilla " + j.getPosicion() + ")");
        }
    }

    @Override
    public void onEventoTurno(String descripcionEvento) {
        System.out.println("  > " + descripcionEvento);
    }

    @Override
    public void onEstadoActualizado(List<Jugador> jugadores) {
        System.out.print("  [Posiciones] ");
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            System.out.print(j.getNombre() + ": " + j.getPosicion());
            if (i < jugadores.size() - 1) {
                System.out.print(" | ");
            }
        }
        System.out.println();
    }

    @Override
    public void onJuegoFinalizado(Jugador ganador) {
        System.out.println("\n==========================================");
        System.out.println("  ¡FIN DEL JUEGO! Ganador: " + ganador.getNombre());
        System.out.println("==========================================");
    }
}