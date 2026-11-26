package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import modelo.excepciones.JuegoFinalizadoException;
import modelo.observer.ObservadorJuego;

/**
 * QUÉ HACE: Es el núcleo del Modelo. Controla todas las reglas originales de Serpientes
 * y Escaleras, valida los límites de jugadores (mínimo 2, máximo 4) y administra la lista
 * de observadores para notificar cada cambio de estado.
 *
 * POR QUÉ SE HACE:
 * - Centraliza las reglas de negocio en un único lugar (Model).
 * - Usa COMPOSICIÓN con Tablero y Dado (los crea él mismo y son exclusivos de esta partida).
 * - Usa AGREGACIÓN con Jugador (los jugadores existen como entidades propias en una lista).
 * - Implementa el rol de SUJETO (Observable) del patrón Observer.
 */
public class Juego {

    // Constantes que fijan las reglas de capacidad de jugadores
    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;

    private final Tablero tablero;
    private final Dado dado;
    private final List<Jugador> jugadores;
    private final List<ObservadorJuego> observadores;

    private int turnoActual;
    private int contadorSeis;
    private int posicionInicioRonda; // Guarda dónde estaba el jugador antes de sacar un 6
    private boolean iniciado;
    private boolean finalizado;
    private Jugador ganador;

    public Juego() {
        this.tablero = new Tablero();
        this.dado = new Dado();
        this.jugadores = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.turnoActual = 0;
        this.contadorSeis = 0;
        this.posicionInicioRonda = 0;
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

    /**
     * QUÉ HACE: Valida antes de cargar nombres que la cantidad elegida esté entre 2 y 4.
     * POR QUÉ SE HACE: Permite al Controlador validar tempranamente el número ingresado
     * por el usuario aplicando la regla de negocio directamente desde el Modelo.
     */
    public void validarCantidadJugadores(int cantidad) throws CantidadJugadoresInvalidaException {
        if (cantidad < MIN_JUGADORES || cantidad > MAX_JUGADORES) {
            throw new CantidadJugadoresInvalidaException(
                    "Cantidad inválida (" + cantidad + "). El juego requiere un mínimo de "
                            + MIN_JUGADORES + " y un máximo de " + MAX_JUGADORES + " jugadores."
            );
        }
    }

    /**
     * QUÉ HACE: Agrega un jugador a la partida verificando que no se supere el máximo de 4.
     * POR QUÉ SE HACE: Garantiza que nadie pueda agregar un 5to jugador ni modificar la lista
     * una vez iniciada la partida, notificando inmediatamente a la Vista mediante Observer.
     */
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
            // Traducimos el error de argumento en una excepción controlada del dominio
            throw new JuegoException(e.getMessage());
        }
    }

    /**
     * QUÉ HACE: Inicia la partida verificando que haya al menos 2 jugadores y máximo 4.
     * POR QUÉ SE HACE: Es la barrera final de validación antes de habilitar los turnos;
     * asegura que la partida jamás arranque con 0 o 1 jugador.
     */
    public void iniciar() throws CantidadJugadoresInvalidaException {
        validarCantidadJugadores(jugadores.size());
        this.iniciado = true;
        this.turnoActual = 0;
        this.contadorSeis = 0;
        this.posicionInicioRonda = jugadores.get(0).getPosicion();
        notificarJuegoIniciado();
    }

    // --- LÓGICA PRINCIPAL DE TURNOS Y REGLAS ORIGINALES ---

    /**
     * QUÉ HACE: Ejecuta un lanzamiento de dado para el jugador en turno y aplica todas
     * las reglas clásicas del juego.
     * POR QUÉ SE HACE: Encapsula toda la mecánica del turno en el Modelo y dispara
     * notificaciones granulares para que la Vista muestre paso a paso lo que sucede.
     */
    public void jugarTurno() throws JuegoException {
        if (!iniciado) {
            throw new JuegoException("La partida aún no ha sido iniciada.");
        }
        if (finalizado) {
            throw new JuegoFinalizadoException("El juego ya finalizó. El ganador fue " + ganador.getNombre() + ".");
        }

        Jugador jugadorActual = jugadores.get(turnoActual);

        // Si es el primer tiro de su turno (no viene de un 6 previo), guardamos su posición inicial
        if (contadorSeis == 0) {
            posicionInicioRonda = jugadorActual.getPosicion();
        }

        int tiro = dado.lanzar();
        notificarEvento(jugadorActual.getNombre() + " (casilla " + jugadorActual.getPosicion() + ") lanzó el dado y sacó: " + tiro);

        // REGLA ORIGINAL 1: Control de racha de seis (3 seis seguidos anulan el turno)
        if (tiro == 6) {
            contadorSeis++;
            if (contadorSeis == 3) {
                jugadorActual.setPosicion(posicionInicioRonda);
                notificarEvento("¡Tres 6 consecutivos! " + jugadorActual.getNombre()
                        + " pierde su turno y regresa a la casilla " + posicionInicioRonda + ".");
                pasarSiguienteTurno();
                notificarEstadoActualizado();
                return;
            }
        }

        // Procesamos el desplazamiento normal, rebotes en 100 y serpientes/escaleras
        procesarMovimiento(jugadorActual, tiro);
        notificarEstadoActualizado();

        // Si el jugador ganó en este movimiento, declaramos el fin del juego
        if (finalizado) {
            notificarJuegoFinalizado(ganador);
            return;
        }

        // REGLA ORIGINAL 2: Sacar 6 otorga un turno extra inmediato
        if (tiro == 6) {
            notificarEvento("¡Sacó 6! " + jugadorActual.getNombre() + " obtiene un tiro extra.");
        } else {
            pasarSiguienteTurno();
        }
    }

    /**
     * QUÉ HACE: Calcula la nueva casilla, aplica el rebote exacto si se pasa de 100 y
     * verifica si cayó en una escalera o serpiente.
     * POR QUÉ SE HACE: Divide la lógica en un método privado cohesivo para mantener
     * jugarTurno() legible y fácil de mantener.
     */
    private void procesarMovimiento(Jugador j, int tiro) {
        int meta = tablero.getTamano(); // 100
        int posicionTentativa = j.getPosicion() + tiro;

        // REGLA ORIGINAL 3: Para ganar debe llegarse exactamente a 100; si se pasa, rebota hacia atrás
        if (posicionTentativa > meta) {
            int exceso = posicionTentativa - meta;
            posicionTentativa = meta - exceso;
            notificarEvento("Se pasó de 100 por " + exceso + " lugar(es). Rebota hasta la casilla " + posicionTentativa + ".");
        }

        j.setPosicion(posicionTentativa);
        notificarEvento(j.getNombre() + " avanza a la casilla " + posicionTentativa + ".");

        // Verificamos si alcanzó exactamente la meta (100)
        if (posicionTentativa == meta) {
            this.finalizado = true;
            this.ganador = j;
            return;
        }

        // REGLA ORIGINAL 4: Verificar si la casilla tiene una escalera o una serpiente
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

            // Si una escalera llevara directamente a la casilla 100, también gana
            if (destino == meta) {
                this.finalizado = true;
                this.ganador = j;
            }
        }
    }

    /**
     * QUÉ HACE: Avanza el índice de turno de forma circular y reinicia el contador de 6.
     * POR QUÉ SE HACE: El operador módulo (% jugadores.size()) permite volver al jugador 0
     * automáticamente después del último jugador sin estructuras condicionales extra.
     */
    private void pasarSiguienteTurno() {
        this.contadorSeis = 0;
        this.turnoActual = (this.turnoActual + 1) % jugadores.size();
    }

    // --- CONSULTAS DE ESTADO (GETTERS) ---

    public Jugador getJugadorActual() {
        return jugadores.get(turnoActual);
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
        // Se envía una vista inmutable (unmodifiableList) para que la Vista no pueda alterar la lista real
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