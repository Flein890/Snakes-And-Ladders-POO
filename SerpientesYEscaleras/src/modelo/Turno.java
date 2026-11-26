package modelo;

import java.util.List;

/**
 * QUÉ HACE: Encapsula el estado del turno en curso: qué jugador está jugando, cuál es
 * su índice dentro de la partida, cuántos 6 consecutivos lleva y desde qué casilla
 * comenzó su racha de tiros.
 *
 * POR QUÉ SE HACE: Aplica el principio de Responsabilidad Única (SRP) y alta cohesión:
 * en lugar de que Juego tenga variables sueltas mezclando el estado de la partida con
 * la mecánica puntual del turno, Turno concentra y protege las reglas de racha de 6 y
 * rotación de turnos.
 */
public class Turno {

    private Jugador jugadorActual;
    private int indiceJugador;
    private int seisesConsecutivos;
    private int posicionInicial;

    public Turno(Jugador jugadorInicial, int indiceJugador) {
        if (jugadorInicial == null) {
            throw new IllegalArgumentException("El jugador inicial no puede ser nulo.");
        }
        this.jugadorActual = jugadorInicial;
        this.indiceJugador = indiceJugador;
        this.seisesConsecutivos = 0;
        // Guardamos dónde arranca antes de su primer tiro para poder restaurarlo si saca tres 6
        this.posicionInicial = jugadorInicial.getPosicion();
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public int getPosicionInicial() {
        return posicionInicial;
    }

    /**
     * QUÉ HACE: Actualiza la racha de seises según el valor obtenido en el dado.
     *
     * POR QUÉ SE HACE: Centraliza en Turno cómo evoluciona el contador de 6 cuando se
     * lanza el dado. Si sale 6, incrementa la racha; si sale otro número, la reinicia a 0.
     */
    public void registrarTiro(int valorDado) {
        if (valorDado == 6) {
            this.seisesConsecutivos++;
        } else {
            this.seisesConsecutivos = 0;
        }
    }

    /**
     * QUÉ HACE: Indica si el jugador alcanzó el límite de 3 seis consecutivos.
     *
     * POR QUÉ SE HACE: Permite que Juego consulte una condición de regla sin acceder
     * directamente al campo privado seisesConsecutivos.
     */
    public boolean superoLimiteSeises() {
        return this.seisesConsecutivos >= 3;
    }

    /**
     * QUÉ HACE: Indica si el jugador sacó un 6 válido (menos de 3 seguidos) y le
     * corresponde repetir tiro.
     *
     * POR QUÉ SE HACE: Hace explícita la regla de tiro extra en el dominio del turno,
     * evitando repetir comparaciones (seisesConsecutivos > 0 && seisesConsecutivos < 3) en Juego.
     */
    public boolean tieneTiroExtra() {
        return this.seisesConsecutivos > 0 && this.seisesConsecutivos < 3;
    }

    /**
     * QUÉ HACE: Pasa el turno al siguiente jugador de la lista de forma circular y
     * resetea los contadores del turno nuevo.
     *
     * POR QUÉ SE HACE: El operador módulo (% jugadores.size()) vuelve al índice 0 al
     * llegar al final de la lista, y registrar la nueva posicionInicial deja listo el
     * turno para el siguiente participante.
     */
    public void avanzar(List<Jugador> jugadores) {
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalStateException("No hay jugadores para avanzar de turno.");
        }
        this.indiceJugador = (this.indiceJugador + 1) % jugadores.size();
        this.jugadorActual = jugadores.get(this.indiceJugador);
        this.seisesConsecutivos = 0;
        this.posicionInicial = this.jugadorActual.getPosicion();
    }
}