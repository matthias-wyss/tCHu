package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Route {


    /**
     * énumération des différents types de route
     */
    public enum Level {
        OVERGROUND,
        UNDERGROUND
    }

    private final String id;
    private final Station station1;
    private final Station station2;
    private final int length;
    private final Level level;
    private final Color color;

    /**
     * Construit une route
     *
     * @param id identifiant de la route
     * @param station1 station de départ de la route
     * @param station2 station d'arrivée de la route
     * @param length longueur de la route
     * @param level niveau de la route (tunnel ou pas)
     * @param color couleur de la route
     * @throws IllegalArgumentException si la station de départ est la même que celle d'arrivée ou si la dépasse les limites de taille
     * @throws NullPointerException si l'identifiant de la route est nul
     */
    public Route(String id, Station station1, Station station2, int length, Level level, Color color) {
        Preconditions.checkArgument(!station1.equals(station2) && length <= Constants.MAX_ROUTE_LENGTH  && length >= Constants.MIN_ROUTE_LENGTH);
        this.id = Objects.requireNonNull(id);
        this.station1 = Objects.requireNonNull(station1);
        this.station2 = Objects.requireNonNull(station2);
        this.length = length;
        this.level = Objects.requireNonNull(level);
        this.color = color;
    }

    /**
     * retourne l'identifiant de la route
     *
     * @return l'identifiant de la route
     */
    public String id() {
        return id;
    }

    /**
     * retourn la station de départ de la route
     *
     * @return la station de départ de la route
     */
    public Station station1() {
        return station1;
    }

    /**
     * retourne la station d'arrivée de la route
     *
     * @return la station d'arrivée de la route
     */
    public Station station2() {
        return station2;
    }

    /**
     * retourne la taille de la route
     *
     * @return la taille de la route
     */
    public int length(){
        return length;
    }

    /**
     * retourne le niveau de la route
     *
     * @return le niveau de la route
     */
    public Level level() {
        return level;
    }

    /**
     * retourne la couleur de la route
     *
     * @return la couleur de la route
     */
    public Color color(){
        return color;
    }

    /**
     * retourne une liste contenant la station de départ et la station d'arrivée de la route
     *
     * @return une liste contenant la station de départ et la station d'arrivée de la route
     */
    public List<Station> stations(){
        return List.of(station1, station2);
    }

    /**
     * retourne la station opposée de la en fournissant la station de départ ou d'arrivée
     *
     * @param station station de départ ou d'arrivée de la route, celle opposée à celle qu'on souhaite obtenir
     * @return la station opposée de la en fournissant la station de départ ou d'arrivée
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument(station.equals(station1) || station.equals(station2));
        return station.id() == station1.id() ? station2 : station1;
    }

    /**
     * retourne une liste des decks cartes que le joueur peut jouer pour s'emparer de la route, triée par ordre croissant de nombre de carte locomotive, puis par couleur
     *
     * route sans tunnel de couleur : un deck de la taille de la route et de la même couleur que la route
     * route sans tunnel grise : un deck de la taille de la route et de n'importe quelle couleur, sauf les cartes locomotive, et toutes les cartes doivent être de la même couleur
     * route avec tunnel de couleur : un deck de la taille de la route pouvant contenir des cartes locomotive et des cartes de la même couleur que la route
     * route avec tunnel grise : un deck de la taille de la route pouvant contenir des cartes locomotive et des cartes colorées, mais s'il y a des cartes de colorées elles doivent être toutes de la même couleur
     *
     * @return une liste des decks cartes que le joueur peut jouer pour s'emparer de la route, triée par ordre croissant de nombre de carte locomotive, puis par couleur
     */
    public List<SortedBag<Card>> possibleClaimCards() {

        SortedBag<Card> CardSortedBag;
        List<SortedBag<Card>> CardSortedBagList = new ArrayList<>();

        if (this.level() == Level.OVERGROUND) {
            if (this.color() == null) {
                for (Card card : Card.values()) {
                    if (card != Card.LOCOMOTIVE) {
                        CardSortedBag = SortedBag.of(this.length(), card);
                        CardSortedBagList.add(CardSortedBag);
                    }
                }
            } else {
                CardSortedBag = SortedBag.of(this.length(), Card.of(this.color()));
                CardSortedBagList.add(CardSortedBag);
            }
        } else if (this.level() == Level.UNDERGROUND) {
            int cardCount = this.length();
            int locomotiveCount = 0;
            if (this.color() == null){
                for (int i = 0; i < this.length(); ++i) {
                    for (Card card : Card.values()) {
                        if (card != Card.LOCOMOTIVE) {
                            CardSortedBag = SortedBag.of(cardCount, card, locomotiveCount, Card.LOCOMOTIVE);
                            CardSortedBagList.add(CardSortedBag);
                        }
                    }
                    --cardCount;
                    ++locomotiveCount;
                }
                CardSortedBag = SortedBag.of(this.length(), Card.LOCOMOTIVE);
                CardSortedBagList.add(CardSortedBag);
            } else {
                for (int i = 0; i <= this.length(); ++i) {
                    CardSortedBag = SortedBag.of(cardCount, Card.of(this.color()), locomotiveCount, Card.LOCOMOTIVE);
                    CardSortedBagList.add(CardSortedBag);
                    --cardCount;
                    ++locomotiveCount;
                }
            }
        }
        return CardSortedBagList;
    }

    /**
     * retourne le nombre de cartes additionnelles que le joueur va devoir jouer pour s'emparer de la route
     *
     * une carte additionnelle doit être jouée pour chacune des cartes piochées qui sont soit de la même couleur que une des cartes jouées, soit une carte locomotive
     *
     * @param claimCards les cartes que le joueur a posé pour tenter de s'emparer de la route
     * @param drawnCards les cartes que le joueur a pioché
     * @return le nombre de cartes additionnelles que le joueur va devoir jouer pour s'emparer de la route
     * @throws IllegalArgumentException si la route n'est pas un tunnel ou si la taille de la liste des cartes que le joueur a pioché n'est pas égale à 3
     */
    public int additionalClaimCardsCount(SortedBag<Card> claimCards, SortedBag<Card> drawnCards) {
        Preconditions.checkArgument(this.level() == Level.UNDERGROUND && drawnCards.size() == 3);
            int count;
            if (notOnlyLocomotive(claimCards)) {
                count = (int) drawnCards.stream().filter(card -> card.equals(Card.LOCOMOTIVE) || card.color().equals(this.color)).count();
            }
            else {
                count = (int) drawnCards.stream().filter(card -> card.equals(Card.LOCOMOTIVE)).count();
            }
            return count;
    }

    private boolean notOnlyLocomotive(SortedBag<Card> cards) {
        return cards.stream().anyMatch(card -> !card.equals(Card.LOCOMOTIVE));
    }

    /**
     * retourne le nombre de points de construction qu'un joueur obtient lorsqu'il s'empare de la route
     *
     * @return le nombre de points de construction qu'un joueur obtient lorsqu'il s'empare de la route
     */
    public int claimPoints(){
        return Constants.ROUTE_CLAIM_POINTS.get(length);
    }

}

