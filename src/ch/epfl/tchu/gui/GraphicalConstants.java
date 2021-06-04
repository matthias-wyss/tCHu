package ch.epfl.tchu.gui;

/**
 * Classe contenant les constantes utiles à la partie graphique de tCHu
 *  @author Thibault Czarniak (327577)
 *  @author Matthias Wyss (329884)
 */
class GraphicalConstants {

    private GraphicalConstants() {}

    /**
     * Port par défaut de connection à distance pour les parties de tCHu
     */
    public static final int PORT = 5108;

    /**
     * Port par défaut pour l'observation de la partie du serveur
     */
    public static final int SPECTATOR_PORT_SERVER = 5109;

    /**
     * Port par défaut pour l'observation de la partie du client
     */
    public static final int SPECTATOR_PORT_CLIENT = 5110;

    /**
     * Facteur de la gauge pour les pioches
     */
    public static final int DECK_GAUGE_FACTOR = 50;

    /**
     * Largeur du rectangle extérieur pour les boutons pioche
     */
    public static final int DECK_OUTSIDE_RECTANGLE_WIDTH = 60;

    /**
     * Longueur du rectangle intérieur pour les boutons pioche
     */
    public static final int DECK_OUTSIDE_RECTANGLE_LENGTH = 90;

    /**
     * Largeur du rectangle intérieur pour les boutons pioche
     */
    public static final int DECK_INSIDE_RECTANGLE_WIDTH = 40;

    /**
     * Hauteur du rectangle intérieur pour les boutons pioche
     */
    public static final int DECK_INSIDE_RECTANGLE_LENGTH = 70;

    /**
     * Largeur du rectangle du bouton pioche
     */
    public static final int DECK_BUTTON_RECTANGLE_WIDTH = 50;

    /**
     * Hauteur du rectangle du bouton pioche
     */
    public static final int DECK_BUTTON_RECTANGLE_LENGTH = 5;

    /**
     * Rayon du cercle de couleur du joueur pour la vue des informations
     */
    public static final int PLAYER_INFO_CIRCLE_RADIUS = 5;

    /**
     * Largeur du rectangle d'un segment de route
     */
    public static final int SEGMENT_ROUTE_RECTANGLE_WIDTH = 12;

    /**
     * Hauteur du rectangle d'un segment de route
     */
    public static final int SEGMENT_ROUTE_RECTANGLE_LENGTH = 36;

    /**
     * Position du 1er cercle des wagons
     */
    public static final int CAR_CIRCLE_1_X = 12;

    /**
     * Position du 2ème cercle des wagon
     */
    public static final int CAR_CIRCLE_2_X = 24;

    /**
     * Postion y des cercles des wagons
     */
    public static final int CAR_CIRCLE_Y = 6;

    /**
     * Rayon des cercles des wagons
     */
    public static final int CAR_CIRCLE_RADIUS = 3;

    /**
     * Rayon des cercles des stations
     */
    public static final int STATION_CIRCLE_RADIUS = 6;

}
