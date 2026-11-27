package modelo;

/**
 * QUÉ HACE: Representa el tablero de juego con una cantidad válida de casillas
 * (64, 100 o 144) y configura las serpientes y escaleras acordes al tamaño elegido.
 *
 * POR QUÉ SE HACE: Centraliza la validación del tamaño y garantiza que ninguna
 * conexión (origen o destino) quede fuera de los límites del tablero o apunte a sí misma.
 */
public class Tablero {

    public static final int TAMANO_64 = 64;
    public static final int TAMANO_100 = 100;
    public static final int TAMANO_144 = 144;
    public static final int TAMANO_DEFAULT = TAMANO_100;

    private final int tamano;
    private final Casilla[] casillas;

    public Tablero() {
        this(TAMANO_DEFAULT);
    }

    public Tablero(int tamano) {
        validarTamano(tamano);
        this.tamano = tamano;
        this.casillas = new Casilla[tamano];

        for (int i = 0; i < tamano; i++) {
            this.casillas[i] = new Casilla(i + 1);
        }

        configurarSerpientesYEscaleras();
    }




    private void validarTamano(int tamano) {
        if (tamano != TAMANO_64 && tamano != TAMANO_100 && tamano != TAMANO_144) {
            throw new IllegalArgumentException(
                    "Tamaño de tablero inválido (" + tamano + "). Debe ser 64, 100 o 144."
            );
        }
    }

    /**
     * QUÉ HACE: Carga escaleras (suben) y serpientes (bajan) adaptadas al tamaño
     * del tablero (64, 100 o 144).
     *
     * POR QUÉ SE HACE: Evita que coordenadas pensadas para 100 casillas rompan un
     * tablero de 64 o dejen gran parte de uno de 144 sin efectos. Nunca se coloca
     * efecto en la casilla 1 ni en la última casilla (tamano).
     */
    private void configurarSerpientesYEscaleras() {
        switch (tamano) {
            case TAMANO_64 -> {
                // Escaleras (8x8)
                agregarConexion(3, 22);
                agregarConexion(8, 26);
                agregarConexion(20, 29);
                agregarConexion(28, 55);
                agregarConexion(36, 57);

                // Serpientes (8x8)
                agregarConexion(17, 4);
                agregarConexion(19, 7);
                agregarConexion(21, 9);
                agregarConexion(43, 34);
                agregarConexion(62, 18);
            }
            case TAMANO_144 -> {
                // Escaleras (12x12)
                agregarConexion(4, 25);
                agregarConexion(13, 46);
                agregarConexion(33, 49);
                agregarConexion(42, 63);
                agregarConexion(50, 69);
                agregarConexion(62, 81);
                agregarConexion(74, 92);
                agregarConexion(88, 115);
                agregarConexion(102, 128);

                // Serpientes (12x12)
                agregarConexion(27, 5);
                agregarConexion(40, 3);
                agregarConexion(43, 18);
                agregarConexion(54, 31);
                agregarConexion(66, 45);
                agregarConexion(89, 53);
                agregarConexion(109, 76);
                agregarConexion(124, 95);
                agregarConexion(138, 104);
            }
            case TAMANO_100 -> {
                // Escaleras (10x10 - estándar)
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

                // Serpientes (10x10 - estándar)
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
        }
    }

    public void agregarConexion(int origen, int destino) {
        if (origen <= 1 || origen >= tamano) {
            throw new IllegalArgumentException(
                    "El origen " + origen + " debe estar entre 2 y " + (tamano - 1) + "."
            );
        }
        if (destino < 1 || destino > tamano || destino == origen) {
            throw new IllegalArgumentException(
                    "El destino " + destino + " no es válido para un tablero de " + tamano + " casillas."
            );
        }
        getCasilla(origen).setDestino(destino); //le aplico la conexion a la casilla en particular
    }

    public Casilla getCasilla(int numero) {
        if (numero < 1 || numero > tamano) {
            throw new IllegalArgumentException(
                    "Número de casilla fuera de rango (" + numero + "). Rango válido: 1.." + tamano + "."
            );
        }
        return casillas[numero - 1];
    }

    public int getTamano() {
        return tamano;
    }
}