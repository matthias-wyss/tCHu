package ch.epfl.tchu.game;

import java.util.List;

/**
 * Enumération des différentes couleurs qui colorent les cartes wagon et les routes
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public enum Color {

    BLACK,
    VIOLET,
    BLUE,
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    WHITE;

    /**
     * Liste contenant toutes les valeurs de l'énumération
     */
    public static final List<Color> ALL = List.of(Color.values());

    /**
     * Entier contenant le nombre de couleurs de l'énumération
     */
    public static final int COUNT = ALL.size();

}
