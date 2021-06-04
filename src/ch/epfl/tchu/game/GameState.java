package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Helper;

//---------- CONTIENT DU BONUS ----------

import java.util.*;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class GameState extends PublicGameState {

    private final Deck<Ticket> tickets;
    private final Map<PlayerId, PlayerState> playerStates;
    private final CardState cardState;

    private GameState(Deck<Ticket> ticketList, PlayerId currentPlayer, Map<PlayerId, PlayerState> playerStates, CardState cardState, PlayerId lastPlayer){
        super(ticketList.size(), cardState, currentPlayer, Map.copyOf(playerStates), lastPlayer);
        this.tickets = ticketList;
        this.playerStates = Map.copyOf(playerStates);
        this.cardState = cardState;
    }

    /**
     * retourne l'état initial d'une partie de tCHu dans laquelle la pioche des billets contient les billets donnés
     *                    et la pioche des cartes contient les cartes de Constants.ALL_CARDS,
     *                    sans les 8 (2×4) du dessus, distribuées aux joueurs;
     *                    ces pioches sont mélangées au moyen du générateur aléatoire donné,
     *                    qui est aussi utilisé pour choisir au hasard l'identité du premier joueur
     * @param tickets pioche des billets
     * @param rng générateur aléatoire
     * @return l'état initial d'une partie de tCHu
     */
    public static GameState initial(SortedBag<Ticket> tickets, Random rng){
        List<Card> cardList = Constants.ALL_CARDS.toList();
        Collections.shuffle(cardList, rng);
        Map<PlayerId, PlayerState> playerStates = new EnumMap<>(PlayerId.class);
        for(PlayerId id : PlayerId.ALL){
            List<Card> playerCards = new ArrayList<>();
            for(int i = 0; i < Constants.INITIAL_CARDS_COUNT; ++i){
                playerCards.add(cardList.get(0));
                cardList.remove(0);
            }
            playerStates.put(id, PlayerState.initial(SortedBag.of(playerCards)));
        }

        int chosenPlayerID = rng.nextInt(PlayerId.COUNT);
        PlayerId playerId = PlayerId.ALL.get(chosenPlayerID);
        Deck<Card> cardDeck = Deck.of(SortedBag.of(cardList), rng);
        Deck<Ticket> ticketDeck = Deck.of(tickets, rng);
        return new GameState(ticketDeck, playerId, playerStates,CardState.of(cardDeck), null);

    }

    /**
     * retourne l'état complet du joueur donné (privé et publique)
     * @param playerId identité du joueur
     * @return l'état complet du joueur donné (privé et publique)
     */
    @Override
    public PlayerState playerState(PlayerId playerId) {
        return playerStates.get(playerId);
    }

    /**
     * retourne l'état complet du joueur courant (privé et publique)
     * @return l'état complet du joueur courant (privé et publique)
     */
    @Override
    public PlayerState currentPlayerState() {
        return playerStates.get(currentPlayerId());
    }

    /**
     * retourne les count billets du sommet de la pioche
     * @param count nombre de billets
     * @return les count billets du sommet de la pioche
     * @throws IllegalArgumentException si count n'est pas compris entre 0 et la taille de la pioche (inclus)
     */
    public SortedBag<Ticket> topTickets(int count) {
        Preconditions.checkArgument(count >= 0 && count <= tickets.size());
        return tickets.topCards(count);
    }

    /**
     * retourne un état identique au récepteur, mais sans les count billets du sommet de la pioche
     * @param count nombre de billets
     * @return un état identique au récepteur, mais sans les count billets du sommet de la pioche
     * @throws IllegalArgumentException si count n'est pas compris entre 0 et la taille de la pioche (inclus)
     */
    public GameState withoutTopTickets(int count) {
        Preconditions.checkArgument(count >= 0 && count <= tickets.size());
        return new GameState(tickets.withoutTopCards(count), currentPlayerId(), playerStates, cardState, lastPlayer());
    }

    /**
     * retourne la carte au sommet de la pioche
     * @return la carte au sommet de la pioche
     * @throws IllegalArgumentException si la pioche est vide
     */
    public Card topCard() {
        return cardState.topDeckCard();
    }

    /**
     * retourne un état identique au récepteur mais sans la carte au sommet de la pioche
     * @return un état identique au récepteur mais sans la carte au sommet de la pioche
     * @throws IllegalArgumentException si la pioche est vide
     */
    public GameState withoutTopCard() {
        CardState newCardState = cardState.withoutTopDeckCard();
        return new GameState(tickets, currentPlayerId(), playerStates, newCardState, lastPlayer());
    }

    /**
     * retourne un état identique au récepteur mais avec les cartes données ajoutées à la défausse
     * @param discardedCards cartes à ajouter à la défausse
     * @return un état identique au récepteur mais avec les cartes données ajoutées à la défausse
     */
    public GameState withMoreDiscardedCards(SortedBag<Card> discardedCards) {
        CardState newCardState = cardState.withMoreDiscardedCards(discardedCards);
        return new GameState(tickets, currentPlayerId(), playerStates, newCardState, lastPlayer());
    }

    /**
     * retourne un état identique au récepteur sauf si la pioche de cartes est vide, auquel cas elle est recréée à partir de la défausse, mélangée au moyen du générateur aléatoire donné
     * @param rng générateur aléatoire
     * @return un état identique au récepteur sauf si la pioche de cartes est vide, auquel cas elle est recréée à partir de la défausse, mélangée au moyen du générateur aléatoire donné
     */
    public GameState withCardsDeckRecreatedIfNeeded(Random rng) {
        if(cardState.isDeckEmpty()) {
            CardState newCardState = cardState.withDeckRecreatedFromDiscards(rng);

            //----------BONUS----------
            Helper.playSound(Helper.Sound.SHUFFLE);
            //-------------------------

            return new GameState(tickets, currentPlayerId(), playerStates, newCardState, lastPlayer());
        } else {
            return this;
        }
    }

    /**
     * retourne un état identique au récepteur mais dans lequel les billets donnés ont été ajoutés à la main du joueur donné
     * @param playerId identité du joueur
     * @param chosenTickets billets à ajouter à la main du joueur
     * @return un état identique au récepteur mais dans lequel les billets donnés ont été ajoutés à la main du joueur donné
     * @throws IllegalArgumentException si le joueur en question possède déjà au moins un billet
     */
    public GameState withInitiallyChosenTickets(PlayerId playerId, SortedBag<Ticket> chosenTickets){
        PlayerState playerState = playerStates.get(playerId);
        Preconditions.checkArgument(playerState.ticketCount() == 0);
        playerState = playerState.withAddedTickets(chosenTickets);
        Map<PlayerId, PlayerState> newPlayerStates = new EnumMap<>(playerStates);
        newPlayerStates.put(playerId, playerState);
        return new GameState(tickets, currentPlayerId(), newPlayerStates, cardState, lastPlayer());
    }

    /**
     * retourne un état identique au récepteur, mais dans lequel le joueur courant a tiré les billets drawnTickets du sommet de la pioche,
     *                                          et choisi de garder ceux contenus dans chosenTicket
     * @param drawnTickets billets tirés par le joueur
     * @param chosenTickets billets gardés par le joueur
     * @return un état identique au récepteur, mais dans lequel le joueur courant a tiré les billets drawnTickets du sommet de la pioche, et choisi de garder ceux contenus dans chosenTicket
     * @throws IllegalArgumentException si l'ensemble des billets gardés n'est pas inclus dans celui des billets tirés
     */
    public GameState withChosenAdditionalTickets(SortedBag<Ticket> drawnTickets, SortedBag<Ticket> chosenTickets) {
        Preconditions.checkArgument(drawnTickets.toList().containsAll(chosenTickets.toList()));
        PlayerState newPlayerState = playerStates.get(currentPlayerId()).withAddedTickets(chosenTickets);
        Map<PlayerId, PlayerState> newPlayerStates = new EnumMap<>(playerStates);
        newPlayerStates.put(currentPlayerId(), newPlayerState);
        return new GameState(tickets.withoutTopCards(drawnTickets.size()), currentPlayerId(), newPlayerStates, cardState, lastPlayer());
    }

    /**
     * retourne un état identique au récepteur si ce n'est que la carte face retournée à l'emplacement donné
     * a été placée dans la main du joueur courant et remplacée par celle au sommet de la pioche
     * @param slot emplacement donné
     * @return un état identique au récepteur si ce n'est que la carte face retournée à l'emplacement donné a été placée dans la main du joueur courant, et remplacée par celle au sommet de la pioche
     * @throws IllegalArgumentException si le slot n'est pas compris entre 0 et 5
     */
    public GameState withDrawnFaceUpCard(int slot){
        Preconditions.checkArgument(Constants.FACE_UP_CARD_SLOTS.contains(slot));
        Card faceUpCard = cardState.faceUpCards().get(slot);
        PlayerState newPlayerState = playerStates.get(currentPlayerId()).withAddedCard(faceUpCard);
        Map<PlayerId, PlayerState> newPlayerStates = new EnumMap<>(playerStates);
        newPlayerStates.put(currentPlayerId(), newPlayerState);
        return new GameState(tickets, currentPlayerId(), newPlayerStates, cardState.withDrawnFaceUpCard(slot), lastPlayer());
    }

    /**
     * retourne un état identique au récepteur si ce n'est que la carte du sommet de la pioche a été placée dans la main du joueur courant
     * @return un état identique au récepteur si ce n'est que la carte du sommet de la pioche a été placée dans la main du joueur courant
     */
    public GameState withBlindlyDrawnCard(){
        PlayerState newPlayerState = playerStates.get(currentPlayerId()).withAddedCard(cardState.topDeckCard());
        Map<PlayerId, PlayerState> newPlayerStates = new EnumMap<>(playerStates);
        newPlayerStates.put(currentPlayerId(), newPlayerState);
        return new GameState(tickets, currentPlayerId(), newPlayerStates, cardState.withoutTopDeckCard(), lastPlayer());
    }

    /**
     * retourne un état identique au récepteur mais dans lequel le joueur courant s'est emparé de la route donnée au moyen des cartes données
     * @param route route dont le joueur s'est emparé
     * @param cards cartes dont le joueur s'est servi pour s'emparer de la route
     * @return un état identique au récepteur mais dans lequel le joueur courant s'est emparé de la route donnée au moyen des cartes données.
     */
    public GameState withClaimedRoute(Route route, SortedBag<Card> cards){
        PlayerState newPlayerState = playerStates.get(currentPlayerId()).withClaimedRoute(route, cards);
        Map<PlayerId, PlayerState> newPlayerStates = new EnumMap<>(playerStates);
        newPlayerStates.put(currentPlayerId(), newPlayerState);
        return new GameState(tickets, currentPlayerId(), newPlayerStates, cardState.withMoreDiscardedCards(cards), lastPlayer());
    }

    /**
     * retourne vrai ssi le dernier tour commence, c'est à dire si l'identité du dernier joueur est actuellement inconnue mais que le joueur courant n'a plus que deux wagons ou moins
     * @return vrai ssi le dernier tour commence, c'est à dire si l'identité du dernier joueur est actuellement inconnue mais que le joueur courant n'a plus que deux wagons ou moins
     */
    public boolean lastTurnBegins() {
        return lastPlayer() == null && currentPlayerState().carCount() <= 2;
    }

    /**
     * retourne un état identique au récepteur si ce n'est que le joueur courant est celui qui suit le joueur courant actuel
     *                    de plus, si lastTurnBegins retourne vrai, le joueur courant actuel devient le dernier joueur
     * @return un état identique au récepteur si ce n'est que le joueur courant est celui qui suit le joueur courant actuel
     *                    de plus, si lastTurnBegins retourne vrai, le joueur courant actuel devient le dernier joueur
     */
    public GameState forNextTurn(){
        PlayerId lastPlayer = lastTurnBegins() ? currentPlayerId() : lastPlayer();
        return new GameState(tickets, currentPlayerId().next(), playerStates, cardState, lastPlayer);
    }



}
