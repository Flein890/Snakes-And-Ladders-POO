package vista;

import java.util.List;
import java.util.Scanner;
import modelo.Jugador;
import modelo.observer.ObservadorJuego;

/**
 * QUÉ HACE: Gestiona toda la interacción con el usuario por consola (lectura de teclado
 * e impresión de mensajes) e implementa ObservadorJuego para reaccionar a los cambios del Modelo.
 *
 * POR QUÉ SE HACE: En MVC, la Vista es exclusivamente responsable de la presentación y
 * captura de datos crudos. No calcula posiciones ni valida reglas de negocio; simplemente
 * refleja en pantalla lo que el Modelo le notifica.
 */
public class VistaConsola implements ObservadorJuego {

    private final Scanner scanner;

    public VistaConsola() {
        this.scanner = new Scanner(System.in);
    }

    // --- MÉTODOS DE ENTRADA DE DATOS (INVOCADOS POR EL CONTROLADOR) ---

    public void mostrarBienvenida() {
        System.out.println("====================================================");
        System.out.println("         BIENVENIDO A SERPIENTES Y ESCALERAS        ");
        System.out.println("====================================================");
    }

    /**
     * QUÉ HACE: Solicita por teclado la cantidad de jugadores y valida que sea un número entero.
     * POR QUÉ SE HACE: La validación de formato de entrada (que no escriban letras) es
     * responsabilidad de la Vista, mientras que la validación de rango [2..4] la hará el Modelo.
     */
    public int pedirCantidadJugadores() {
        while (true) {
            System.out.print("Ingrese la cantidad de jugadores (2 a 4): ");
            String entrada = scanner.nextLine();
            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                mostrarError("Por favor, ingrese un número entero válido.");
            }
        }
    }

    public String pedirNombreJugador(int numeroJugador) {
        System.out.print("Ingrese el nombre del Jugador " + numeroJugador + ": ");
        return scanner.nextLine();
    }

    public void esperarEnterParaTirar(String nombreJugador) {
        System.out.print("\nTurno de [" + nombreJugador + "]. Presione ENTER para lanzar el dado...");
        scanner.nextLine();
    }

    public void mostrarError(String mensajeError) {
        System.out.println("[ERROR] " + mensajeError);
    }

    // --- CALLBACKS DEL PATRÓN OBSERVER (INVOCADOS AUTOMÁTICAMENTE POR EL MODELO) ---

    @Override
    public void onJugadorAgregado(Jugador jugador, int totalActual) {
        System.out.println(" -> Jugador registrado: " + jugador.getNombre() + " (Total: " + totalActual + ")");
    }

    @Override
    public void onJuegoIniciado(List<Jugador> jugadores) {
        System.out.println("\n----------------------------------------------------");
        System.out.println("¡PARTIDA INICIADA! Participantes en orden de turno:");
        for (int i = 0; i < jugadores.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + jugadores.get(i).getNombre() + " (Posición inicial: 0)");
        }
        System.out.println("----------------------------------------------------");
    }

    @Override
    public void onEventoTurno(String descripcionEvento) {
        System.out.println("  * " + descripcionEvento);
    }

    @Override
    public void onEstadoActualizado(List<Jugador> jugadores) {
        System.out.print("  [Posiciones actuales] ");
        for (Jugador j : jugadores) {
            System.out.print(j.getNombre() + ": " + j.getPosicion() + " | ");
        }
        System.out.println();
    }

    @Override
    public void onJuegoFinalizado(Jugador ganador) {
        System.out.println("\n====================================================");
        System.out.println("  ¡FELICITACIONES " + ganador.getNombre().toUpperCase() + "! GANASTE LA PARTIDA  ");
        System.out.println("====================================================");
    }
}