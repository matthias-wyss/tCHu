package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public class PublicPlayerState {

    private final int ticketCount;
    private final int cardCount;
    private final List<Route> routes;
    private final int carCount;
    private final int constructionPoints;

    /**
     * construit l'état public d'un joueur possédant le nombre de billets et de cartes donnés, et s'étant emparé des routes données
     * @param ticketCount nombre de billets du joueur
     * @param cardCount nombre de cartes du joueur
     * @param routes liste des routes possédées par le joueur
     * @throws IllegalArgumentException si le nombre de billets ou le nombre de cartes est strictement négatif
     */
    public PublicPlayerState(int ticketCount, int cardCount, List<Route> routes) {
        Preconditions.checkArgument(ticketCount >= 0 && cardCount >= 0);
        this.ticketCount = ticketCount;
        this.cardCount = cardCount;
        this.routes = List.copyOf(routes);
        this.carCount = computeCars();
        this.constructionPoints = computePoints();
    }

    /**
     * retourne le nombre de billets que possède le joueur
     * @return le nombre de billets que possède le joueur
     */
    public int ticketCount(){
        return ticketCount;
    }

    /**
     * retourne le nombre de cartes que possède le joueur
     * @return le nombre de cartes que possède le joueur
     */
    public int cardCount(){
        return cardCount;
    }

    private int computeCars(){
        int totalLength = 0;
        for(Route route : routes){
            totalLength += route.length();
        }
        return Constants.INITIAL_CAR_COUNT - totalLength;
    }

    /**
     * retourne les routes dont le joueur s'est emparé
     * @return les routes dont le joueur s'est emparé
     */
    public List<Route> routes(){
        return routes;
    }

    /**
     * retourne le nombre de wagons que possède le joueur
     * @return le nombre de wagons que possède le joueur
     */
    public int carCount(){
        return carCount;
    }

    /**
     * retourne le nombre de points de construction obtenus par le joueur
     * @return le nombre de points de construction obtenus par le joueur
     */
    public int claimPoints(){
        return constructionPoints;
    }

    private int computePoints() {
        int result = 0;
        for(Route route : routes) {
            result += route.claimPoints();
        }
        return result;
    }

}
