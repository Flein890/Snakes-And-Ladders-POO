import controlador.ControladorJuego;
import modelo.Juego;
import vista.VistaConsola;

/**
 * QUÉ HACE: Punto de arranque de la aplicación Java. Instancia las tres capas de MVC
 * (Modelo, Vista y Controlador), las vincula e inicia la ejecución.
 *
 * POR QUÉ SE HACE: Separa la creación e inyección de dependencias del funcionamiento
 * interno de cada capa, manteniendo el acoplamiento al mínimo.
 */
public class Main {
    public static void main(String[] args) {
        Juego modelo = new Juego();
        VistaConsola vista = new VistaConsola();
        ControladorJuego controlador = new ControladorJuego(modelo, vista);

        controlador.ejecutar();
    }
}