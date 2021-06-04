package ch.epfl.tchu;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Lance une Exception si le booléen fourni en argument est faux
     * @param shouldBeTrue expression booléenne que l'on souhaite vérifier
     * @throws IllegalArgumentException si l'expression booléenne est fausse
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
