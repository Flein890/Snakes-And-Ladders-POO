package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import modelo.excepciones.JuegoFinalizadoException;
import modelo.observer.ObservadorJuego;


public class Juego {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;

    // Estado interno del modelo: el controlador no accede a estos campos directamente
    private final Tablero tablero;
    private final Dado dado;
    private final List<Jugador> jugadores;

    private final List<ObservadorJuego> observadores;

    // Estado del turno y ciclo de vida de la partida consultado por el controlador (ej. getJugadorActual(), isFinalizado())
    private Turno turno;
    private boolean iniciado;
    private boolean finalizado;
    private Jugador ganador;


    public Juego() {
        this(Tablero.TAMANO_DEFAULT); //llamo al otro constructor pasandole el tamaño
    }

    public Juego(int tamanoTablero) {
        this.tablero = new Tablero(tamanoTablero);
        this.dado = new Dado();
        this.jugadores = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.turno = null; // Aún no hay turno activo hasta que el controlador llame a iniciar()
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
        // Si el controlador llama a agregarJugador() después de iniciar(), bloquea la acción con JuegoException
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
            notificarJugadorAgregado(nuevo); //notifica a los observadores que se agrego un jugador
        } catch (IllegalArgumentException e) {
            throw new JuegoException(e.getMessage());
        }
    }


    public void iniciar() throws CantidadJugadoresInvalidaException {
        validarCantidadJugadores(jugadores.size());
        this.turno = new Turno(jugadores.get(0), 0); // Crea el primer turno apuntando al jugador en el índice 0
        this.iniciado = true;
        notificarJuegoIniciado();// Avisa a los observadores que la partida comenzó
    }


    // --- LÓGICA PRINCIPAL DE TURNOS Y REGLAS ORIGINALES ---

    // El controlador invoca jugarTurno() en cada iteración del bucle principal después de que el usuario presiona ENTER
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

        if (jugadorActual.isBloqueadoPorSeises()) {
            if (tiro == 6) {
                jugadorActual.setBloqueadoPorSeises(false);
                notificarEvento("¡Sacó 6! " + jugadorActual.getNombre()
                        + " se desbloquea y puede moverse.");
            } else {
                notificarEvento(jugadorActual.getNombre()
                        + " sigue bloqueado (necesita sacar un 6 para moverse).");
                turno.avanzar(jugadores);
                notificarEstadoActualizado();
                return;
            }
        }

        turno.registrarTiro(tiro);

        if (turno.superoLimiteSeises()) {
            int posInicial = turno.getPosicionInicial(); // o 1 si dejaste posicionInicial en 1
            jugadorActual.setPosicion(posInicial);
            jugadorActual.setBloqueadoPorSeises(true);
            notificarEvento("¡Tres 6 consecutivos! " + jugadorActual.getNombre()
                    + " vuelve a la casilla " + posInicial
                    + " y queda bloqueado hasta sacar otro 6.");
            turno.avanzar(jugadores);
            notificarEstadoActualizado();
            return;
        }

        procesarMovimiento(jugadorActual, tiro); //asignar la posicion al jugador y el efecto si es que tiene (escalera/serpiente)
        notificarEstadoActualizado();

        if (finalizado) {
            notificarJuegoFinalizado(ganador);
            return;
        }

        if (turno.tieneTiroExtra()) {
            notificarEvento("¡Sacó 6! " + jugadorActual.getNombre() + " obtiene un tiro extra.");
        } else {
            turno.avanzar(jugadores);
        }
    }




    private void procesarMovimiento(Jugador j, int tiro) {

        int meta = tablero.getTamano(); // 64, 100 o 144 según el tablero elegido

        int posicionTentativa = j.getPosicion() + tiro;

        // Si se pasa de la casilla meta, aplica la regla de rebote hacia atrás
        if (posicionTentativa > meta) {
            // Calcula cuántas casillas se excedió de la meta
            int exceso = posicionTentativa - meta;
            // Retrocede desde la meta la cantidad excedida
            posicionTentativa = meta - exceso;
            // Notifica el rebote a los observadores
            notificarEvento("Se pasó de " + meta + " por " + exceso + " lugar(es). Rebota hasta la casilla "
                    + posicionTentativa + ".");
        }

        // Actualiza la posición del jugador tras el avance/rebote
        j.setPosicion(posicionTentativa);
        // Notifica el avance a los observadores
        notificarEvento(j.getNombre() + " avanza a la casilla " + posicionTentativa + ".");

        if (posicionTentativa == meta) {
            this.finalizado = true;
            this.ganador = j;
            return;
        }

        // Consulta la casilla donde quedó parado para ver si tiene escalera o serpiente
        Casilla casillaActual = tablero.getCasilla(posicionTentativa);
        if (casillaActual.tieneEfecto()) {
            // Obtiene la casilla destino de la conexión
            int destino = casillaActual.getDestino();
            // Si el destino es mayor que la posición actual, es una escalera (sube)
            if (destino > posicionTentativa) {
                notificarEvento("¡ESCALERA! " + j.getNombre() + " sube desde la casilla "
                        + posicionTentativa + " hasta la " + destino + ".");
            } else {
                // Si el destino es menor, es una serpiente (baja)
                notificarEvento("¡SERPIENTE! " + j.getNombre() + " resbala desde la casilla "
                        + posicionTentativa + " hasta la " + destino + ".");
            }
            // Mueve al jugador a la casilla destino de la escalera o serpiente
            j.setPosicion(destino);

            // Si la escalera llevó justo a la casilla meta, gana la partida
            if (destino == meta) {
                this.finalizado = true;
                this.ganador = j;
            }
        }
    }

    // --- CONSULTAS DE ESTADO ---

    public int getTamanoTablero() {
        return tablero.getTamano();
    }

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
        for (ObservadorJuego obs : observadores) {
            obs.onEstadoActualizado(jugadores);
        }
    }

    private void notificarJuegoFinalizado(Jugador ganador) {
        for (ObservadorJuego obs : observadores) {
            obs.onJuegoFinalizado(ganador);
        }
    }
}