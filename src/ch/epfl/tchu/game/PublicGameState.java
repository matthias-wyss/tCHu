package ch.epfl.tchu.game;
import ch.epfl.tchu.Preconditions;

import java.util.*;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public class PublicGameState {

    private final int ticketsCount;
    private final PublicCardState cardState;
    private final PlayerId currentPlayerId;
    private final Map<PlayerId, PublicPlayerState> playerState;
    private final PlayerId lastPlayer;


    /**
     * construit l'état public d'une partie
     * @param ticketsCount taille de la pioche de billets
     * @param cardState état public des cartes
     * @param currentPlayerId joueur courant
     * @param playerState état public des joueurs
     * @param lastPlayer identité du dernier joueur
     */
    public PublicGameState(int ticketsCount, PublicCardState cardState, PlayerId currentPlayerId, Map<PlayerId, PublicPlayerState> playerState, PlayerId lastPlayer) {
        Preconditions.checkArgument(ticketsCount >= 0 && playerState.size() == 2);
        this.ticketsCount = ticketsCount;
        this.cardState = Objects.requireNonNull(cardState);
        this.currentPlayerId = Objects.requireNonNull(currentPlayerId);
        this.playerState = Map.copyOf(playerState);
        this.lastPlayer = lastPlayer;
    }

    /**
     * retourne la taille de la pioche de billets
     * @return la taille de la pioche de billets
     */
    public int ticketsCount(){
        return ticketsCount;
    }

    /**
     * retourne vrai ssi il est possible de tirer des billets, c'est à dire si la pioche n'est pas vide
     * @return vrai ssi il est possible de tirer des billets, c'est à dire si la pioche n'est pas vide
     */
    public boolean canDrawTickets() {
        return ticketsCount!=0;
    }

    /**
     * retourne la partie publique de l'état des cartes wagon/locomotive
     * @return la partie publique de l'état des cartes wagon/locomotive
     */
    public PublicCardState cardState(){
        return cardState;
    }

    /**
     * retourne vrai s'il est possible de tirer des cartes, c'est à dire si la pioche et la défausse contiennent entre elles au moins 5 cartes
     * @return vrai s'il est possible de tirer des cartes, c'est à dire si la pioche et la défausse contiennent entre elles au moins 5 cartes
     */
    public boolean canDrawCards() {
        return (cardState.discardsSize() + cardState.deckSize())>=5;
    }

    /**
     * retourne l'identité du joueur actuel
     * @return l'identité du joueur actuel,
     */
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    /**
     * retourne la partie publique de l'état du joueur d'identité donnée
     * @param playerId identité du joueur
     * @return la partie publique de l'état du joueur d'identité donnée
     */
    public PublicPlayerState playerState(PlayerId playerId) {
        return playerState.get(playerId);
    }

    /**
     * retourne la partie publique de l'état du joueur courant
     * @return la partie publique de l'état du joueur courant
     */
    public PublicPlayerState currentPlayerState(){
        return playerState.get(currentPlayerId);
    }

    /**
     * retourne la totalité des routes dont l'un ou l'autre des joueurs s'est emparé
     * @return la totalité des routes dont l'un ou l'autre des joueurs s'est emparé
     */
    public List<Route> claimedRoutes(){
        List<Route> totalRoutes = new ArrayList<>();
        PlayerId.ALL.forEach(id -> totalRoutes.addAll(playerState.get(id).routes()));
        return totalRoutes;
    }

    /**
     * retourne l'identité du dernier joueur, ou null si elle n'est pas encore connue car le dernier tour n'a pas commencé
     * @return l'identité du dernier joueur, ou null si elle n'est pas encore connue car le dernier tour n'a pas commencé
     */
    public PlayerId lastPlayer() {
        return lastPlayer;
    }

}
