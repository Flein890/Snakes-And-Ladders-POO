package modelo;

/**
 * QUÉ HACE: Representa a cada participante de la partida, guardando su nombre y su posición actual.
 *
 * POR QUÉ SE HACE: Mantiene el estado individual de cada jugador encapsulado. Inicia en
 * la posición 0 (fuera del tablero) para que, al sacar un 1 en el primer tiro, avance a
 * la casilla 1, tal como establecen las reglas originales del juego de mesa.
 */
public class Jugador {

    private final String nombre;
    private int posicion;

    public Jugador(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío.");
        }
        this.nombre = nombre.trim();
        // Todos los jugadores comienzan en la casilla 1
        this.posicion = 1;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }
}