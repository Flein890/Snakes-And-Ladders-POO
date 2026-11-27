package modelo;

public class Jugador {

    private final String nombre;
    private int posicion;
    private boolean bloqueadoPorSeises;

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

    public boolean isBloqueadoPorSeises() {
        return bloqueadoPorSeises;
    }

    public void setBloqueadoPorSeises(boolean bloqueadoPorSeises) {
        this.bloqueadoPorSeises = bloqueadoPorSeises;
    }
}