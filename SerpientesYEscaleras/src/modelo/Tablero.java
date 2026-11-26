package modelo;

/**
 * QUÉ HACE: Administra la estructura de 100 casillas y configura las conexiones de
 * serpientes y escaleras.
 *
 * POR QUÉ SE HACE: Separa la topología del tablero de las reglas de turnos (Juego).
 * Mantiene una relación de COMPOSICIÓN con Casilla (el Tablero crea y posee sus 100
 * casillas; si se destruye el tablero, se destruyen sus casillas).
 */
public class Tablero { //NO uso herencia porque un TABLERO no es una CASILLA, un tablero tiene casillas

    // Constante estática (compartida por toda la clase) con el tamaño oficial del tablero
    public static final int TAMANO_DEFAULT = 100;

    // Arreglo de tamaño fijo porque un tablero clásico siempre tiene exactamente 100 casillas
    private final Casilla[] casillas = new Casilla[TAMANO_DEFAULT];;

    public Tablero() {

        // Inicializamos cada posición del arreglo con su objeto Casilla (numeradas del 1 al 100)
        for (int i = 0; i < TAMANO_DEFAULT; i++) {
            this.casillas[i] = new Casilla(i + 1);
        }
        configurarSerpientesYEscaleras();
    }

    /**
     * QUÉ HACE: Define las posiciones clásicas de escaleras (suben) y serpientes (bajan).
     * POR QUÉ SE HACE: Es privado porque forma parte de la inicialización interna del tablero
     * y garantiza que ningún destino encadene con otro origen (evitando bucles infinitos)
     * ni que la casilla 100 tenga una serpiente.
     */
    private void configurarSerpientesYEscaleras() {
        // Escaleras clásicas (origen < destino)
        agregarConexion(2, 38);
        agregarConexion(7, 14);
        agregarConexion(8, 31);
        agregarConexion(15, 26);
        agregarConexion(21, 42);
        agregarConexion(28, 84);
        agregarConexion(36, 44);
        agregarConexion(51, 67);
        agregarConexion(71, 91);
        agregarConexion(78, 98);
        agregarConexion(87, 94);

        // Serpientes clásicas (origen > destino)
        agregarConexion(16, 6);
        agregarConexion(46, 25);
        agregarConexion(49, 11);
        agregarConexion(62, 19);
        agregarConexion(64, 60);
        agregarConexion(74, 53);
        agregarConexion(89, 68);
        agregarConexion(92, 88);
        agregarConexion(95, 75);
        agregarConexion(99, 80);
    }

    /**
     * QUÉ HACE: Vincula una casilla de origen con una de destino validando los límites del tablero.
     * POR QUÉ SE HACE: Evita errores de índice fuera de rango (ArrayIndexOutOfBoundsException)
     * y protege la casilla 100 para que siempre sea la meta final.
     */
    public void agregarConexion(int origen, int destino) {
        //valido que sea un origen o destino valido
        if (origen < 1 || origen >= TAMANO_DEFAULT || destino < 1 || destino > TAMANO_DEFAULT) {
            throw new IllegalArgumentException("Conexión fuera de los límites válidos del tablero.");
        }
        // se resta 1 porque las casillas van de 1-100 pero los índices del array van de 0-99
        casillas[origen - 1].setDestino(destino);
    }

    public Casilla getCasilla(int numero) {
        if (numero < 1 || numero > TAMANO_DEFAULT) {
            throw new IllegalArgumentException("Número de casilla inválido: " + numero);
        }
        return casillas[numero - 1];
    }

    public int getTamano() {
        return TAMANO_DEFAULT;
    }
}