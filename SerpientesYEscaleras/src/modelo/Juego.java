package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.CantidadJugadoresInvalidaException;
import modelo.excepciones.JuegoException;
import modelo.excepciones.JuegoFinalizadoException;
import modelo.observer.ObservadorJuego;


public class Juego {

    // Límites del dominio que el controlador consulta/respeta al validar o pedir cantidad de jugadores
    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;

    // Estado interno del modelo: el controlador no accede a estos campos directamente
    private final Tablero tablero;
    private final Dado dado;
    private final List<Jugador> jugadores;

    // Lista de observadores notificados automáticamente; el controlador no reenvía notificaciones a mano
    private final List<ObservadorJuego> observadores;

    // Estado del turno y ciclo de vida de la partida consultado por el controlador (ej. getJugadorActual(), isFinalizado())
    private Turno turno;
    private boolean iniciado;
    private boolean finalizado;
    private Jugador ganador;


    public Juego() {
        this(Tablero.TAMANO_DEFAULT);
    }

    public Juego(int tamanoTablero) {
        // Crea el tablero con el tamaño elegido; si es inválido, lanza excepción hacia quien lo invocó
        this.tablero = new Tablero(tamanoTablero);
        this.dado = new Dado();
        this.jugadores = new ArrayList<>();
        // Inicializa la lista de observadores a notificar cuando cambie el estado
        this.observadores = new ArrayList<>();
        // Aún no hay turno activo hasta que el controlador llame a iniciar()
        this.turno = null;
        // La partida arranca sin iniciar
        this.iniciado = false;
        // La partida arranca sin finalizar
        this.finalizado = false;
        this.ganador = null;
    }

    // --- GESTIÓN DE OBSERVADORES (PATRÓN OBSERVER) ---

    // Permite suscribir observadores (ej. VistaConsola) sin que el controlador tenga que tocar el modelo
    public void agregarObservador(ObservadorJuego observador) {
        // Evita nulos y duplicados dentro de la lista de observadores
        if (observador != null && !observadores.contains(observador)) {
            // Registra el observador para recibir futuras notificaciones de Juego
            observadores.add(observador);
        }
    }

    // Permite desuscribir un observador si el controlador o el flujo lo necesitan
    public void eliminarObservador(ObservadorJuego observador) {
        // Quita el observador de la lista para que deje de recibir avisos
        observadores.remove(observador);
    }

    // --- VALIDACIÓN Y CONFIGURACIÓN DE JUGADORES ---

    // El controlador llama a este metodo para validar la cantidad ingresada por el usuario antes de registrar nombres
    public void validarCantidadJugadores(int cantidad) throws CantidadJugadoresInvalidaException {
        if (cantidad < MIN_JUGADORES || cantidad > MAX_JUGADORES) {
            // Lanza excepción checked del dominio para que el controlador la capture, muestre el error y reintente
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
        // Si se supera el cupo máximo, lanza CantidadJugadoresInvalidaException para que el controlador la maneje
        if (jugadores.size() >= MAX_JUGADORES) {
            throw new CantidadJugadoresInvalidaException(
                    "No se pueden agregar más jugadores. El máximo permitido es " + MAX_JUGADORES + "."
            );
        }
        try {
            // Crea la entidad Jugador con el nombre recibido desde el controlador
            Jugador nuevo = new Jugador(nombre);
            // Agrega el jugador a la partida
            jugadores.add(nuevo);
            // Notifica a los observadores (ej. VistaConsola) que se agregó un jugador, sin intervención del controlador
            notificarJugadorAgregado(nuevo);
        } catch (IllegalArgumentException e) {
            throw new JuegoException(e.getMessage());
        }
    }


    public void iniciar() throws CantidadJugadoresInvalidaException {
        // Revalida que haya entre 2 y 4 jugadores antes de arrancar (protege aunque no se haya llamado antes)
        validarCantidadJugadores(jugadores.size());
        // Crea el primer turno apuntando al jugador en el índice 0
        this.turno = new Turno(jugadores.get(0), 0);
        // Marca la partida como iniciada para habilitar jugarTurno()
        this.iniciado = true;
        // Avisa a los observadores que la partida comenzó; el controlador no imprime esto directamente
        notificarJuegoIniciado();
    }

    // --- LÓGICA PRINCIPAL DE TURNOS Y REGLAS ORIGINALES ---

    // El controlador invoca jugarTurno() en cada iteración del bucle principal después de que el usuario presiona ENTER
    public void jugarTurno() throws JuegoException {
        // Si el controlador llama a jugarTurno() antes de iniciar(), corta con JuegoException
        if (!iniciado) {
            throw new JuegoException("La partida aún no ha sido iniciada.");
        }
        // Si la partida ya terminó, lanza JuegoFinalizadoException (subclase de JuegoException) al controlador
        if (finalizado) {
            throw new JuegoFinalizadoException("El juego ya finalizó. El ganador fue " + ganador.getNombre() + ".");
        }

        // Obtiene el jugador al que le corresponde actuar según el Turno actual
        Jugador jugadorActual = turno.getJugadorActual();
        // Lanza el dado y obtiene el valor del tiro
        int tiro = dado.lanzar();

        // Notifica el evento del lanzamiento a los observadores; el controlador no arma este mensaje
        notificarEvento(jugadorActual.getNombre() + " (casilla " + jugadorActual.getPosicion()
                + ") lanzó el dado y sacó: " + tiro);

        // Registra el tiro en Turno para actualizar la racha de seises consecutivos
        turno.registrarTiro(tiro);

        // Evalúa si el jugador superó el límite de seises consecutivos
        if (turno.superoLimiteSeises()) {
            // Recupera la posición donde empezó el turno este jugador
            int posInicial = turno.getPosicionInicial();
            // Devuelve al jugador a su posición inicial del turno
            jugadorActual.setPosicion(posInicial);
            // Informa por medio de los observadores la penalización por triple seis
            notificarEvento("¡Tres 6 consecutivos! " + jugadorActual.getNombre()
                    + " pierde su turno y regresa a la casilla " + posInicial + ".");
            // Pasa el turno al siguiente jugador
            turno.avanzar(jugadores);
            // Notifica a los observadores las posiciones actualizadas
            notificarEstadoActualizado();
            // Termina el turno anticipadamente; el controlador seguirá su bucle consultando modelo.isFinalizado()
            return;
        }

        // Aplica el desplazamiento, rebote, serpientes/escaleras y chequeo de victoria
        procesarMovimiento(jugadorActual, tiro);
        // Notifica a los observadores el estado actualizado tras moverse
        notificarEstadoActualizado();

        // Si alguien ganó en este movimiento
        if (finalizado) {
            // Notifica a los observadores quién ganó; luego el controlador verá isFinalizado() == true y saldrá del bucle
            notificarJuegoFinalizado(ganador);
            return;
        }

        // Si el jugador no ganó y tiene tiro extra por sacar 6, mantiene el mismo turno para la próxima llamada del controlador
        if (turno.tieneTiroExtra()) {
            notificarEvento("¡Sacó 6! " + jugadorActual.getNombre() + " obtiene un tiro extra.");
        } else {
            // Si no tiene tiro extra, rota el turno al siguiente jugador para el próximo ciclo del controlador
            turno.avanzar(jugadores);
        }
    }

    // Método interno de reglas del modelo: el controlador no lo llama directamente
    private void procesarMovimiento(Jugador j, int tiro) {
        // Toma el tamaño del tablero (64, 100 o 144) como casilla meta
        int meta = tablero.getTamano(); // 64, 100 o 144 según el tablero elegido
        // Calcula la posición tentativa sumando el tiro a la posición actual
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

        // Si cayó exactamente en la meta, marca fin de partida y registra ganador
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