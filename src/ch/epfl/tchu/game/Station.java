package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Station {

    private final int id;
    private final String name;

    /**
     * Construit une gare ayant le numéro d'identification et le nom donné
     *
     *
     * @param id
     *              numéro d'identification
     * @param name
     *              nom donné
     * @throws IllegalArgumentException
     *              si id est strictement négatif
     *              si la taille de name est négative
     */
    public Station(int id, String name){
        Preconditions.checkArgument(name.length() > 0);
        Preconditions.checkArgument(id >= 0);
        this.id = id;
        this.name = name;

    }

    /**
     * retourne le numéro d'identification de cette gare
     *
     * @return le numéro d'identification de cette gare
     */
    public int id() { return id; }

    /**
     * retourne le nom de cette gare
     *
     * @return le nom de cette gare
     */
    public String name() { return name; }

    /**
     * redéfini toString() pour retourner le nom de cette gare
     *
     * @return le nom de cette gare
     */
    @Override
    public String toString(){
        return name;
    }

}
