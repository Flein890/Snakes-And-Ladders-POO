package modelo;

import java.util.Random;

//un dado tradicional para este juego debe tener 6 caras.

public class Dado {

    private static final int CARAS = 6; //es static por si alguna clase necesitara saber cuantas caras tiene un dado
    private final Random random;

    public Dado() {

        this.random = new Random(); //creo una instancia de random cada vez que se cree un dado
    }

    public int lanzar() {

        return random.nextInt(CARAS) + 1; // +1 porque sino genera un numero desde 0 a 5
    }
}
