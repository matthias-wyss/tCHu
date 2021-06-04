package ch.epfl.tchu.game;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public interface StationConnectivity {
    /**
     * Permet de vérifier la connexion ou non de deux stations sur le réseau d'un joueur
     * @param s1 Station de départ
     * @param s2 Station d'arrivée
     * @return Vrai si les deux stations sont connectées par le jour, faux sinon
     */
    boolean connected(Station s1, Station s2);

}
