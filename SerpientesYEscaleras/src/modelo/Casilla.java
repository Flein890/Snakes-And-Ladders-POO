package modelo;

/**
 * QUÉ HACE: Representa cada uno de los 100 espacios del tablero, almacenando su número
 * y el número de casilla destino al que envía al jugador si cae en ella.
 *
 * POR QUÉ SE HACE: Encapsula tanto las casillas normales como las serpientes y escaleras
 * en una única abstracción cohesiva. Si numero == destino, es una casilla normal; si
 * destino > numero es una escalera; y si destino < numero es una serpiente.
 */
public class Casilla {

    // Atributos privados para garantizar el encapsulamiento
    private final int numero;
    private int destino;

    public Casilla(int numero) {
        this.numero = numero;
        // Por defecto, toda casilla se apunta a sí misma (no tiene efecto especial)
        this.destino = numero;
    }

    public int getNumero() {
        return numero;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        // Permite transformar esta casilla en la base de una escalera o cabeza de serpiente
        this.destino = destino;
    }

    /**
     * QUÉ HACE: Indica si caer en esta casilla provoca un desplazamiento automático.
     * POR QUÉ SE HACE: Evita que la clase Juego tenga que comparar números directamente,
     * delegando la responsabilidad de saber si hay efecto a la propia Casilla (principio Expert).
     */
    public boolean tieneEfecto() {
        return this.numero != this.destino;
    }
}