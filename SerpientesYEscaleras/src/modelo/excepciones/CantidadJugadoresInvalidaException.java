package modelo.excepciones;

/**
 * QUÉ HACE: Excepción específica que se lanza cuando se intenta configurar o iniciar
 * una partida con menos de 2 jugadores o más de 4 jugadores.
 *
 * POR QUÉ SE HACE: Especializa a JuegoException mediante herencia para identificar con
 * precisión errores relacionados con el límite de participantes exigido por las reglas.
 */
public class CantidadJugadoresInvalidaException extends JuegoException {

    public CantidadJugadoresInvalidaException(String mensaje) {
        super(mensaje);
    }
}