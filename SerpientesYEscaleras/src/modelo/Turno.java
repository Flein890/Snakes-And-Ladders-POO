package modelo;

import java.util.List;


public class Turno {

    private Jugador jugadorActual;
    private int indiceJugador;
    private int seisesConsecutivos;
    private int posicionInicial = 1;

    public Turno(Jugador jugadorInicial, int indiceJugador) {
        if (jugadorInicial == null) {
            throw new IllegalArgumentException("El jugador inicial no puede ser nulo.");
        }
        this.jugadorActual = jugadorInicial;
        this.indiceJugador = indiceJugador;
        this.seisesConsecutivos = 0;


    }


    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public int getPosicionInicial() {
        return posicionInicial;
    }

    //registrar si saca 6, resetea si saca otro numero
    public void registrarTiro(int valorDado) {
        if (valorDado == 6) {
            this.seisesConsecutivos++;
        } else {
            this.seisesConsecutivos = 0;
        }
    }


    public boolean superoLimiteSeises() {
        return this.seisesConsecutivos >= 3;
    }

    //valida si saco 6 y no saco 6 tres veces
    public boolean tieneTiroExtra() {
        return this.seisesConsecutivos > 0 && this.seisesConsecutivos < 3;
    }

    //recorro la lista de jugadores de forma circular y asigno el nuevo jugadorActual
    public void avanzar(List<Jugador> jugadores) {
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalStateException("No hay jugadores para avanzar de turno.");
        }
        this.indiceJugador = (this.indiceJugador + 1) % jugadores.size(); //El operador módulo (% jugadores.size()) vuelve al índice 0 al llegar al final de la lista
        this.jugadorActual = jugadores.get(this.indiceJugador);
        this.seisesConsecutivos = 0;

    }
}