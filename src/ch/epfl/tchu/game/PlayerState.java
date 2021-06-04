package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class PlayerState extends PublicPlayerState {

    private final SortedBag<Ticket> tickets;
    private final SortedBag<Card> cards;

    /**
     * construit l'état privé d'un joueur possédant les billets donnés, les cartes données et s'étant emparé des routes données
     * @param tickets liste des tickets du joueur
     * @param cards liste des cartes du joueur
     * @param routes liste des routes en possession du joueur
     */
    public PlayerState(SortedBag<Ticket> tickets, SortedBag<Card> cards, List<Route> routes) {
        super(tickets.size(), cards.size(), routes);
        this.tickets = tickets;
        this.cards = cards;
    }

    /**
     * retourne l'état initial d'un joueur auquel les cartes initiales données ont été distribuées, ne possèdant encore aucun billet, et ne s'étant emparé d'aucune route
     * @param initialCards liste des cartes initiales du joueur
     * @return l'état initial d'un joueur auquel les cartes initiales données ont été distribuées, ne possèdant encore aucun billet, et ne s'étant emparé d'aucune route
     * @throws IllegalArgumentException si le nombre de cartes initiales ne vaut pas 4
     */
    public static PlayerState initial(SortedBag<Card> initialCards){
        Preconditions.checkArgument(initialCards.size() == 4);
        return new PlayerState(SortedBag.of(), initialCards, List.of());
    }

    /**
     * retourne les billets du joueur
     * @return les billets du joueur
     */
    public SortedBag<Ticket> tickets() {
        return tickets;
    }

    /**
     * retourne un état identique au récepteur, si ce n'est que le joueur possède en plus les billets donnés
     * @param newTickets nouveaux billets
     * @return un état identique au récepteur, si ce n'est que le joueur possède en plus les billets donnés
     */
    public PlayerState withAddedTickets(SortedBag<Ticket> newTickets) {
        SortedBag<Ticket> ticketsUnion = tickets.union(newTickets);
        return new PlayerState(ticketsUnion, cards, super.routes());
    }

    /**
     * retourne les cartes wagon/locomotive du joueur
     * @return les cartes wagon/locomotive du joueur,
     */
    public SortedBag<Card> cards() {
        return cards;
    }

    /**
     * retourne un état identique au récepteur, si ce n'est que le joueur possède en plus la carte donnée
     * @param card nouvelle carte
     * @return un état identique au récepteur, si ce n'est que le joueur possède en plus la carte donnée
     */
    public PlayerState withAddedCard(Card card){
        SortedBag<Card> cardsUnion = cards.union(SortedBag.of(1, card));
        return new PlayerState(tickets, cardsUnion, super.routes());
    }


    /**
     * retourne vrai si le joueur peut s'emparer de la route donnée, c'est à dire s'il lui reste assez de wagons et s'il possède les cartes nécessaires
     * @param route route dont le joueur souhaite s'emparer
     * @return vrai si le joueur peut s'emparer de la route donnée, c'est à dire s'il lui reste assez de wagons et s'il possède les cartes nécessaires
     */
    public boolean canClaimRoute(Route route) {
        if(super.carCount() >= route.length()) {
            return route.possibleClaimCards().stream().anyMatch(cards::contains);
        }
        return false;
    }

    /**
     * retourne la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour prendre possession de la route donnée, triée par ordre croissant de nombre de carte locomotive, puis par couleur
     * @param route route dont le joueur souhaite s'emparer
     * @return la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour prendre possession de la route donnée, triée par ordre croissant de nombre de carte locomotive, puis par couleur
     * @throws IllegalArgumentException si le joueur n'a pas assez de wagons pour s'emparer de la route
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route){
        Preconditions.checkArgument(super.carCount() >= route.length());
        return route.possibleClaimCards().stream().filter(cards::contains).collect(Collectors.toList());

    }

    /**
     * retourne la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour s'emparer d'un tunnel,
     *                 trié par ordre croissant du nombre de cartes locomotives,
     *                 sachant qu'il a initialement posé les cartes initialCards,
     *                 que les 3 cartes tirées du sommet de la pioche sont drawnCards,
     *                 et que ces dernières forcent le joueur à poser encore additionalCardsCount cartes
     * @param additionalCardsCount nombre de cartes additionelles que le joueur doit poser
     * @param initialCards cartes initialement poser par le joueur
     * @return la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour s'emparer d'un tunnel
     * @throws IllegalArgumentException si le nombre de cartes additionnelles n'est pas compris entre 1 et 3 (inclus),
     *                                  si l'ensemble des cartes initiales est vide ou contient plus de 2 types de cartes différents,
     *                                  ou si l'ensemble des cartes tirées ne contient pas exactement 3 cartes
     */
    public List<SortedBag<Card>> possibleAdditionalCards(int additionalCardsCount, SortedBag<Card> initialCards){
        Preconditions.checkArgument(additionalCardsCount >= 1 && additionalCardsCount <=3 && !initialCards.isEmpty() && initialCards.toSet().size() <=2);
        List<Card> cardsCopy = (cards.difference(initialCards)).toList();
        SortedBag.Builder<Card> usableCardsBuilder = new SortedBag.Builder<>();
        cardsCopy.stream().filter(card -> card == Card.LOCOMOTIVE || initialCards.contains(card)).forEach(usableCardsBuilder::add);
        SortedBag<Card> possibleCards = usableCardsBuilder.build();
        if (possibleCards.size() < additionalCardsCount) {
            return List.of();
        }
        List<SortedBag<Card>> options = new ArrayList<>(possibleCards.subsetsOfSize(additionalCardsCount));
        options.sort(Comparator.comparingInt(cs -> cs.countOf(Card.LOCOMOTIVE)));
        return options;

    }

    /**
     * retourne un état identique au récepteur, si ce n'est que le joueur s'est de plus emparé de la route donnée au moyen des cartes données
     * @param route route dont le joueur s'est emparé
     * @param claimCards cartes que le joueur à jouer pour s'emparer de la route
     * @return un état identique au récepteur, si ce n'est que le joueur s'est de plus emparé de la route donnée au moyen des cartes données
     */
    public PlayerState withClaimedRoute(Route route, SortedBag<Card> claimCards) {
        List<Route> routesUnion = new ArrayList<>(super.routes());
        routesUnion.add(route);
        SortedBag<Card> newCards = cards.difference(claimCards);
        return new PlayerState(tickets, newCards, routesUnion);
    }

    /**
     * retourne le nombre de points (éventuellement négatif) obtenus par le joueur grâce à ses billets
     * @return le nombre de points(éventuellement négatif) obtenus par le joueur grâce à ses billets
     */
    public int ticketPoints() {
        int points = 0;
        StationPartition partition = computeStationPartition();
        for(Ticket ticket : this.tickets) {
            points += ticket.points(partition);
        }

        return points;
    }

    /**
     * retourne la totalité des points obtenus par le joueur à la fin de la partie (points retournés par les méthodes claimPoints et ticketPoints)
     * @return la totalité des points obtenus par le joueur à la fin de la partie (points retournés par les méthodes claimPoints et ticketPoints)
     */
    public int finalPoints() {
        return claimPoints() + ticketPoints();
    }


    private StationPartition computeStationPartition() {
        int maxId = 0;
        for(Route route : super.routes()) {
            if (route.station1().id() >= maxId) {
                maxId = route.station1().id();
            }
            if (route.station2().id() >= maxId) {
                maxId = route.station2().id();
            }

        }
        StationPartition.Builder partitionBuilder = new StationPartition.Builder(maxId + 1);
        for(Route route1 : super.routes()) {
            partitionBuilder.connect(route1.station1(), route1.station2());
            for(Route route2 : super.routes()) {
                partitionBuilder.connect(route2.station1(), route2.station2());
                if(!route1.id().equals(route2.id()) && route1.stations().stream().anyMatch(e -> route2.stations().contains(e))) {
                    partitionBuilder.connect(route1.station1(), route2.station1());
                }
            }
        }
        return partitionBuilder.build();
    }

}
