package ch.epfl.tchu.game;

import java.util.List;

/**
 * Enumération des différentes cartes du jeu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public enum Card {

    BLACK(Color.BLACK),
    VIOLET(Color.VIOLET),
    BLUE(Color.BLUE),
    GREEN(Color.GREEN),
    YELLOW(Color.YELLOW),
    ORANGE(Color.ORANGE),
    RED(Color.RED),
    WHITE(Color.WHITE),
    LOCOMOTIVE(null);

    /**
     * Liste de toutes les valeurs possibles de Card
     */
    public static final List<Card> ALL = List.of(Card.values());
    /**
     * Correspond au nombre de cartes différentes possibles
     */
    public static final int COUNT = ALL.size();
    /**
     * Liste de toutes les cartes réservées aux wagons
     */
    public static final List<Card> CARS = List.of(BLACK, VIOLET, BLUE, GREEN, YELLOW, ORANGE, RED, WHITE);

    private final Color associatedColor;
    Card(Color color) {
        this.associatedColor = color;
    }

    /**
     * Permet d'obtenir l'équivalent en carte d'une couleur
     * @param color Couleur dont on souhaite obtenir la carte
     * @return Retourne la carte correspondant à la couleur en paramètre
     */
    public static Card of(Color color){
        switch(color) {
            case BLACK:
                return BLACK;
            case VIOLET:
                return VIOLET;
            case BLUE:
                return BLUE;
            case GREEN:
                return GREEN;
            case YELLOW:
                return YELLOW;
            case ORANGE:
                return ORANGE;
            case RED:
                return RED;
            case WHITE:
                return WHITE;
            default:
                return null;
        }

    }

    /**
     * Donne la couleur associée à la carte
     * @return La couleur (Color) de la carte
     */
    public Color color() {
        return associatedColor;
    }
}
