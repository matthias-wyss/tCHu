package ch.epfl.tchu.game;

import java.util.List;

/**
 * Enumération des joueurs du jeu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public enum PlayerId {

    PLAYER_1,
    PLAYER_2;

    /**
     * Liste de tous les joueurs
     */
    public static final List<PlayerId> ALL = List.of(PlayerId.values());

    /**
     * Correspond au nombre de joueurs
     */
    public static final int COUNT = ALL.size();

    /**
     * retourne l'identité du joueur qui suit celui auquel on l'applique
     * @return l'identité du joueur qui suit celui auquel on l'applique
     */
    public PlayerId next(){
        return this.equals(PLAYER_1) ? PLAYER_2 : PLAYER_1;
    }

}
