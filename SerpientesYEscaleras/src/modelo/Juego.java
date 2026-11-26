package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import modelo.excepciones.JuegoFinalizadoException;
import modelo.observer.ObservadorJuego;

/**
 * QUÉ HACE: Coordina la partida completa de Serpientes y Escaleras, valida la cantidad
 * de jugadores (mínimo 2, máximo 4), delega el estado del turno actual a Turno y notifica
 * los cambios a los observadores registrados.
 *
 * POR QUÉ SE HACE: Mantiene el rol de Sujeto Observable del patrón Observer y orquesta
 * las entidades del Modelo sin cargar con los detalles internos de la racha de seises ni
 * del índice de turno, que ahora viven en Turno.
 */
public class Juego {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;

    private final Tablero tablero;
    private final Dado dado;
    private final List<Jugador> jugadores;
    private final List<ObservadorJuego> observadores;

    // Reemplaza a turnoActual, contadorSeis y posicionInicioRonda
    private Turno turno;
    private boolean iniciado;
    private boolean finalizado;
    private Jugador ganador;

    public Juego() {
        this.tablero = new Tablero();
        this.dado = new Dado();
        this.jugadores = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.turno = null;
        this.iniciado = false;
        this.finalizado = false;
        this.ganador = null;
    }

    // --- GESTIÓN DE OBSERVADORES (PATRÓN OBSERVER) ---

    public void agregarObservador(ObservadorJuego observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void eliminarObservador(ObservadorJuego observador) {
        observadores.remove(observador);
    }

    // --- VALIDACIÓN Y CONFIGURACIÓN DE JUGADORES ---

    public void validarCantidadJugadores(int cantidad) throws CantidadJugadoresInvalidaException {
        if (cantidad < MIN_JUGADORES || cantidad > MAX_JUGADORES) {
            throw new CantidadJugadoresInvalidaException(
                    "Cantidad inválida (" + cantidad + "). El juego requiere un mínimo de "
                            + MIN_JUGADORES + " y un máximo de " + MAX_JUGADORES + " jugadores."
            );
        }
    }

    public void agregarJugador(String nombre) throws JuegoException {
        if (iniciado) {
            throw new JuegoException("No se pueden agregar jugadores una vez iniciada la partida.");
        }
        if (jugadores.size() >= MAX_JUGADORES) {
            throw new CantidadJugadoresInvalidaException(
                    "No se pueden agregar más jugadores. El máximo permitido es " + MAX_JUGADORES + "."
            );
        }
        try {
            Jugador nuevo = new Jugador(nombre);
            jugadores.add(nuevo);
            notificarJugadorAgregado(nuevo);
        } catch (IllegalArgumentException e) {
            throw new JuegoException(e.getMessage());
        }
    }

    /**
     * QUÉ HACE: Valida la cantidad de jugadores, instancia el primer Turno y marca la
     * partida como iniciada.
     *
     * POR QUÉ SE HACE: Garantiza que el objeto Turno exista únicamente cuando ya hay al
     * menos un jugador válido y la partida está lista para comenzar.
     */
    public void iniciar() throws CantidadJugadoresInvalidaException {
        validarCantidadJugadores(jugadores.size());
        this.turno = new Turno(jugadores.get(0), 0);
        this.iniciado = true;
        notificarJuegoIniciado();
    }

    // --- LÓGICA PRINCIPAL DE TURNOS Y REGLAS ORIGINALES ---

    /**
     * QUÉ HACE: Ejecuta un lanzamiento del dado para el jugador en turno, aplica las
     * reglas originales y actualiza el Turno.
     *
     * POR QUÉ SE HACE: Delega a Turno el registro de la racha de 6, la comprobación del
     * límite de 3 seises, la concesión de tiro extra y el avance circular al siguiente jugador.
     */
    public void jugarTurno() throws JuegoException {
        if (!iniciado) {
            throw new JuegoException("La partida aún no ha sido iniciada.");
        }
        if (finalizado) {
            throw new JuegoFinalizadoException("El juego ya finalizó. El ganador fue " + ganador.getNombre() + ".");
        }

        Jugador jugadorActual = turno.getJugadorActual();
        int tiro = dado.lanzar();

        notificarEvento(jugadorActual.getNombre() + " (casilla " + jugadorActual.getPosicion()
                + ") lanzó el dado y sacó: " + tiro);

        // Registramos el tiro en Turno para actualizar la racha de seises consecutivos
        turno.registrarTiro(tiro);

        // REGLA ORIGINAL 1: Tres 6 seguidos anulan el turno y devuelven a la posición inicial del turno
        if (turno.superoLimiteSeises()) {
            int posInicial = turno.getPosicionInicial();
            jugadorActual.setPosicion(posInicial);
            notificarEvento("¡Tres 6 consecutivos! " + jugadorActual.getNombre()
                    + " pierde su turno y regresa a la casilla " + posInicial + ".");
            turno.avanzar(jugadores);
            notificarEstadoActualizado();
            return;
        }

        // Procesamos avance, rebote exacto en 100 y efectos de serpientes o escaleras
        procesarMovimiento(jugadorActual, tiro);
        notificarEstadoActualizado();

        if (finalizado) {
            notificarJuegoFinalizado(ganador);
            return;
        }

        // REGLA ORIGINAL 2: Sacar 6 (sin llegar a 3 seguidos) otorga un tiro extra inmediato
        if (turno.tieneTiroExtra()) {
            notificarEvento("¡Sacó 6! " + jugadorActual.getNombre() + " obtiene un tiro extra.");
        } else {
            turno.avanzar(jugadores);
        }
    }

    private void procesarMovimiento(Jugador j, int tiro) {
        int meta = tablero.getTamano(); // 100
        int posicionTentativa = j.getPosicion() + tiro;

        // REGLA ORIGINAL 3: Para ganar se debe llegar exactamente a 100; si se pasa, rebota
        if (posicionTentativa > meta) {
            int exceso = posicionTentativa - meta;
            posicionTentativa = meta - exceso;
            notificarEvento("Se pasó de 100 por " + exceso + " lugar(es). Rebota hasta la casilla "
                    + posicionTentativa + ".");
        }

        j.setPosicion(posicionTentativa);
        notificarEvento(j.getNombre() + " avanza a la casilla " + posicionTentativa + ".");

        if (posicionTentativa == meta) {
            this.finalizado = true;
            this.ganador = j;
            return;
        }

        // REGLA ORIGINAL 4: Verificar escalera (sube) o serpiente (baja)
        Casilla casillaActual = tablero.getCasilla(posicionTentativa);
        if (casillaActual.tieneEfecto()) {
            int destino = casillaActual.getDestino();
            if (destino > posicionTentativa) {
                notificarEvento("¡ESCALERA! " + j.getNombre() + " sube desde la casilla "
                        + posicionTentativa + " hasta la " + destino + ".");
            } else {
                notificarEvento("¡SERPIENTE! " + j.getNombre() + " resbala desde la casilla "
                        + posicionTentativa + " hasta la " + destino + ".");
            }
            j.setPosicion(destino);

            if (destino == meta) {
                this.finalizado = true;
                this.ganador = j;
            }
        }
    }

    // --- CONSULTAS DE ESTADO (GETTERS) ---

    public Jugador getJugadorActual() {
        return turno != null ? turno.getJugadorActual() : null;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public Jugador getGanador() {
        return ganador;
    }

    // --- MÉTODOS PRIVADOS DE NOTIFICACIÓN (PATRÓN OBSERVER) ---

    private void notificarJugadorAgregado(Jugador jugador) {
        for (ObservadorJuego obs : observadores) {
            obs.onJugadorAgregado(jugador, jugadores.size());
        }
    }

    private void notificarJuegoIniciado() {
        List<Jugador> listaSegura = Collections.unmodifiableList(jugadores);
        for (ObservadorJuego obs : observadores) {
            obs.onJuegoIniciado(listaSegura);
        }
    }

    private void notificarEvento(String mensaje) {
        for (ObservadorJuego obs : observadores) {
            obs.onEventoTurno(mensaje);
        }
    }

    private void notificarEstadoActualizado() {
        List<Jugador> listaSegura = Collections.unmodifiableList(jugadores);
        for (ObservadorJuego obs : observadores) {
            obs.onEstadoActualizado(listaSegura);
        }
    }

    private void notificarJuegoFinalizado(Jugador ganador) {
        for (ObservadorJuego obs : observadores) {
            obs.onJuegoFinalizado(ganador);
        }
    }
}