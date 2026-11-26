package modelo;

import java.util.Random;

/**
 * QUÉ HACE: Modela un dado estándar de N caras (6 por defecto) capaz de generar lanzamientos aleatorios.
 *
 * POR QUÉ SE HACE: Aísla la generación de números aleatorios en una clase independiente
 * con responsabilidad única (SRP), permitiendo reutilizarla o cambiar la cantidad de caras
 * mediante sobrecarga de constructores.
 */
public class Dado {

    private static final int CARAS = 6;
    private final Random random;

    public Dado() {
        this.random = new Random();
    }

    public int lanzar() {
        return random.nextInt(CARAS) + 1;
    }
}
