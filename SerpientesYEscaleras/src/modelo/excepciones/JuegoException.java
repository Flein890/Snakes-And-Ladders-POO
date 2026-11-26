package modelo.excepciones;

/**
 * QUÉ HACE: Representa la excepción base (padre) para todos los errores de reglas
 * de negocio propios del juego Serpientes y Escaleras.
 *
 * POR QUÉ SE HACE: Crear una jerarquía de excepciones personalizadas permite que el
 * Modelo informe incumplimientos de reglas sin depender de System.out.println (manteniendo
 * el desacoplamiento de MVC) y que el Controlador pueda capturar cualquier error del
 * juego de forma polimórfica mediante "catch (JuegoException e)".
 */
public class JuegoException extends Exception {

    public JuegoException(String mensaje) {
        // Llama al constructor de java.lang.Exception para almacenar el mensaje descriptivo
        super(mensaje);
    }
}