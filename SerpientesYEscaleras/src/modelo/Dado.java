package modelo;

import java.util.Random;


public class Dado {

    private static final int CARAS = 6;
    private final Random random;

    public Dado() {

        this.random = new Random(); //creo una instancia de random cada vez que se cree un dado
    }

    public int lanzar() {

        return random.nextInt(CARAS) + 1; // +1 porque sino genera un numero desde 0 a 5
    }
}
