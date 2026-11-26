package modelo.excepciones;

/**
 * QUÉ HACE: Excepción que impide seguir tirando el dado una vez que ya existe un ganador.
 *
 * POR QUÉ SE HACE: Protege la integridad del estado del Modelo (invariante de clase),
 * evitando que una llamada errónea desde cualquier Controlador altere una partida terminada.
 */
public class JuegoFinalizadoException extends JuegoException {

    public JuegoFinalizadoException(String mensaje) {
        super(mensaje);
    }
}