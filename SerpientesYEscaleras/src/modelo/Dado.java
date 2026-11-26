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

    private final int caras;
    private final Random random;

    // Constructor por defecto: crea un dado clásico de 6 caras
    public Dado() {
        // Reutiliza el constructor parametrizado con this(6) para no duplicar código
        this(6);
    }

    // Constructor sobrecargado: permite instanciar dados de distinta cantidad de caras
    public Dado(int caras) {
        if (caras < 2) {
            throw new IllegalArgumentException("El dado debe tener al menos 2 caras.");
        }
        this.caras = caras;
        this.random = new Random();
    }

    /**
     * QUÉ HACE: Devuelve un número entero aleatorio entre 1 y la cantidad de caras (inclusive).
     * POR QUÉ SE HACE: nextInt(caras) genera valores de 0 a (caras - 1), por lo que sumar 1
     * ajusta el rango exacto al de un dado físico [1..6].
     */
    public int lanzar() {
        return random.nextInt(caras) + 1;
    }
}