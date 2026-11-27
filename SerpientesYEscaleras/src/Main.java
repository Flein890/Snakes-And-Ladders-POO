import controlador.ControladorJuego;
import modelo.Juego;
import vista.VistaConsola;

/**
 * QUÉ HACE: Punto de entrada de la aplicación. Solicita el tamaño del tablero,
 * construye el modelo y la vista, los conecta mediante el patrón Observer e
 * inicia el controlador.
 *
 **/
public class Main {

    public static void main(String[] args) {
        VistaConsola vista = new VistaConsola();
        vista.mostrarBienvenida();

        int tamanoTablero = vista.pedirTamanoTablero();
        Juego modelo = new Juego(tamanoTablero);
        modelo.agregarObservador(vista);

        ControladorJuego controlador = new ControladorJuego(modelo, vista);
        controlador.ejecutar();
    }
}