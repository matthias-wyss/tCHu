package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;

/**
 * Classe représentant l'état observable d'une partie de tCHu
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */

public final class ObservableGameState {
    private PlayerState ps;
    private PublicGameState publicGameState;
    private final PlayerId playerId;

    private final IntegerProperty remainingTickets, remainingCards;
    private final List<ObjectProperty<Card>> faceUpCards;
    private final Map<Route, ObjectProperty<PlayerId>> routes;

    private final Map<PlayerId, IntegerProperty> playersTicketsCount, playersCardsCount, playersCarCount, playersConstructionPoints;

    private final ObservableList<Ticket> playerTickets;
    private final Map<Card, IntegerProperty> playerCards;
    private final Map<Route, BooleanProperty> claimableRoutes;
    private final Set<List<Station>> claimedStations;

    /**
     * construit un état de jeu observable spécifique à un joueur
     * @param playerID identité du joueur
     */
    public ObservableGameState(PlayerId playerID){
        playerId = playerID;
        remainingTickets = new SimpleIntegerProperty();
        remainingCards  = new SimpleIntegerProperty();
        faceUpCards = createFaceUpCards();
        routes = createRoutes();

        playersTicketsCount = createPlayerIntPropertyMap();
        playersCardsCount = createPlayerIntPropertyMap();
        playersCarCount = createPlayerIntPropertyMap();
        playersConstructionPoints = createPlayerIntPropertyMap();

        playerTickets = FXCollections.observableArrayList();
        playerCards = createPlayerCards();
        claimableRoutes = createClaimableRoutes();
        claimedStations = new HashSet<>();

        }

    private Map<PlayerId, IntegerProperty> createPlayerIntPropertyMap(){
        Map<PlayerId, IntegerProperty> intProperty = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(p -> intProperty.put(p, new SimpleIntegerProperty()));
        return intProperty;
    }

    private List<ObjectProperty<Card>> createFaceUpCards(){
        List<ObjectProperty<Card>> faceUpCards = new ArrayList<>();
        for(int i = 0; i < Constants.FACE_UP_CARDS_COUNT; ++i){
            faceUpCards.add(new SimpleObjectProperty<>());
        }
        return faceUpCards;
    }

    private Map<Card, IntegerProperty> createPlayerCards(){
        Map<Card, IntegerProperty> playerCards = new EnumMap<>(Card.class);
        Card.ALL.forEach(c -> playerCards.put(c, new SimpleIntegerProperty()));
        return playerCards;
    }

    private Map<Route, BooleanProperty> createClaimableRoutes(){
        Map<Route, BooleanProperty> claimableRoutes = new HashMap<>();
        ChMap.routes().forEach(r -> claimableRoutes.put(r, new SimpleBooleanProperty()));
        return claimableRoutes;
    }

    private Map<Route, ObjectProperty<PlayerId>> createRoutes(){
        Map<Route, ObjectProperty<PlayerId>> routes = new HashMap<>();
        for(Route route : ChMap.routes()){
            routes.put(route, new SimpleObjectProperty<>());
        }
        return routes;
    }

    /**
     * Modifie chacune des propriétés de l'état de jeu observable à partir de
     * la partie publique du jeu et de l'état complet du joueur associé à l'instance
     * d'ObservableGameState
     * @param pGS la partie publique du jeu
     * @param pS l'état complet du joueur
     */
    public void setState(PublicGameState pGS, PlayerState pS) {
        ps = pS;
        publicGameState = pGS;
        remainingTickets.setValue((100*pGS.ticketsCount()/ChMap.tickets().size()));
        remainingCards.setValue(100*pGS.cardState().deckSize() / Constants.ALL_CARDS.size());
        for(int slot : Constants.FACE_UP_CARD_SLOTS){
            Card newCard = pGS.cardState().faceUpCard(slot);
            faceUpCards.get(slot).set(newCard);
        }
        for(PlayerId id : PlayerId.ALL){
            for(Route route : pGS.playerState(id).routes()){
                claimedStations.add(route.stations());
                routes.get(route).setValue(id);
            }
            playersTicketsCount.get(id).setValue(pGS.playerState(id).ticketCount());
            playersCardsCount.get(id).setValue(pGS.playerState(id).cardCount());
            playersCarCount.get(id).setValue(pGS.playerState(id).carCount());
            playersConstructionPoints.get(id).setValue(pGS.playerState(id).claimPoints());

        }

        for(Route route : routes.keySet()){
            claimableRoutes.get(route).set(pGS.currentPlayerId() == playerId &&
                    !claimedStations.contains(route.stations()) && pS.canClaimRoute(route));
        }


        playerTickets.setAll(pS.tickets().toList());
        for(Card card : Card.ALL){
            playerCards.get(card).set(pS.cards().countOf(card));
        }
    }


    /**
     * accesseur de la propriété contenant le pourcentage de billets restant dans la pioche
     * @return la propriété contenant le pourcentage de billets restant dans la pioche
     */
    public ReadOnlyIntegerProperty getTicketsRatio() {
        return remainingTickets;
    }

    /**
     * accesseur de la propriété contenant le pourcentage de cartes restant dans la pioche
     * @return la propriété contenant le pourcentage de cartes restant dans la pioche
     */
    public ReadOnlyIntegerProperty getCardsRatio() {
        return remainingCards;
    }

    /**
     * accesseur d'une liste des 5 propriétés contenant, pour chaque emplacement, la carte face visible qu'il contient
     * @return la liste des 5 propriétés contenant, pour chaque emplacement, la carte face visible qu'il contient
     */
    public List<ReadOnlyObjectProperty<Card>> getFaceUpCards() {
        ObservableList<ReadOnlyObjectProperty<Card>> faceUpCardsList = FXCollections.observableArrayList(faceUpCards);
        return FXCollections.unmodifiableObservableList(faceUpCardsList);
    }

    /**
     * pour savoir si le joueur peut s'emparer de la route en argument
     * @param route route dont le joueur souhaite savoir s'il peut s'en emparer
     * @return un boolean indiquant si le joueur peut s'emparer de la route
     */
    public ReadOnlyBooleanProperty isClaimable(Route route){
        return claimableRoutes.get(route);
    }

    /**
     * accesseur de l'ensemble des routes aquises par le joueur
     * @return l'ensemble des routes aquises par le joueur
     */
    public ObservableMap<Route, ReadOnlyObjectProperty<PlayerId>> getRoutes() {
        ObservableMap<Route, ReadOnlyObjectProperty<PlayerId>> observableRoutes = FXCollections.observableMap(new HashMap<>(routes));
        return FXCollections.unmodifiableObservableMap(observableRoutes);
    }

    /**
     * accesseur du nombre de billets que possède le joueur
     * @param id identité du joueur
     * @return le nombre de billets que possède le joueur
     */
    public ReadOnlyIntegerProperty getPlayerTicketsCount(PlayerId id) {
        return playersTicketsCount.get(id);
    }

    /**
     * accesseur du nombre de cartes que possède le joueur
     * @param id identité du joueur
     * @return le nombre de cartes que possède le joueur
     */
    public ReadOnlyIntegerProperty getPlayerCardsCount(PlayerId id) {
        return playersCardsCount.get(id);
    }

    /**
     * accesseur du nombre de wagons que possède le joueur
     * @param id identité du joueur
     * @return le nombre de wagons que possède le joueur
     */
    public ReadOnlyIntegerProperty getPlayerCarCount(PlayerId id) {
        return playersCarCount.get(id);
    }

    /**
     * accesseur du nombre de points de construction que possède le joueur
     * @param id identité du joueur
     * @return le nombre de points de construction que possède le joueur
     */
    public ReadOnlyIntegerProperty getPlayerConstructionPoints(PlayerId id) {
        return (playersConstructionPoints.get(id));
    }

    /**
     * accesseur de la liste des tickets du joueur
     * @return la liste des tickets du joueur
     */
    public ObservableList<Ticket> getPlayerTickets() {
        return FXCollections.unmodifiableObservableList(playerTickets);
    }

    /**
     * accesseur de la liste des cartes du joueur
     * @return la liste des cartes du joueur
     */
    public ObservableMap<Card, ReadOnlyIntegerProperty> getPlayerCards() {
        ObservableMap<Card, ReadOnlyIntegerProperty> readOnlyPlayerCards = FXCollections.observableMap(new EnumMap<>(playerCards));
        return FXCollections.unmodifiableObservableMap(readOnlyPlayerCards);
    }

    /**
     * pour savoir si le joueur peut piocher des billets
     * @return un boolean indiquant si le joueur peut piocher des billets
     */
    public ReadOnlyBooleanProperty canDrawTickets() {
        return new SimpleBooleanProperty(publicGameState.canDrawTickets());
    }

    /**
     * pour savoir si le joueur peut piocher des cartes
     * @return un boolean indiquant si le joueur peut piocher des cartes
     */
    public ReadOnlyBooleanProperty canDrawCards() {
        return new SimpleBooleanProperty(publicGameState.canDrawCards());
    }

    /**
     * pour savoir la liste des cartes que le joueur peut utiliser pour s'emparer de la route passer un argument
     * @param route route pour laquelle on veut savoir la liste des cartes possibles à jouer
     * @return la liste des cartes que le joueur peut utiliser pour s'emparer de la route passer un argument
     */
    public ObservableList<SortedBag<Card>> possibleClaimCards(Route route) {
        return FXCollections.observableArrayList(ps.possibleClaimCards(route));
    }

}
