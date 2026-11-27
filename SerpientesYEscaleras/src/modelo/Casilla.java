package modelo;

/**
 Encapsula tanto las casillas normales como las serpientes y escaleras
 * en una única abstracción cohesiva. Si numero == destino, es una casilla normal; si
 * destino > numero es una escalera; y si destino < numero es una serpiente.
 */
public class Casilla {

    // Atributos privados para garantizar el encapsulamiento
    private final int numero;       //si numero == destino, es una casilla normal
    private int destino;            //si destino > numero es una escalera; y si destino < numero es una serpiente.


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

    //este metodo evita que el Juego tenga que comparar si la casilla tiene o no efecto
    public boolean tieneEfecto() {
        return this.numero != this.destino; //si la casilla no apunta a si misma
    }
}