package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;

import java.util.List;
import java.util.Map;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public interface Player {

    /**
     * énumération des trois types d'actions qu'un joueur de tCHu peut effectuer durant un tour
     */
    enum TurnKind{
        DRAW_TICKETS, DRAW_CARDS, CLAIM_ROUTE, QUIT, PLAY_AGAIN;

        /**
         * liste immuable de tous les membres de l'énumération
         */
        public static final List<TurnKind> ALL = List.of(TurnKind.values());
    }

    /**
     * est appelée au début de la partie pour communiquer au joueur sa propre identité ownId, ainsi que les noms des différents joueurs, le sien inclus, qui se trouvent dans playerNames
     * @param ownId propre identité du joueur
     * @param playerNames noms des différents joueurs
     */
    void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames);

    /**
     * Est appelée chaque fois qu'une information doit être communiquée au joueur courant de la partie
     * @param info String, information que l'on souhaite communiquer au joueur courant
     */
    void receiveInfo(String info);

    /**
     * est appelée chaque fois que l'état du jeu a changé, pour informer le joueur de la composante publique de ce nouvel état, newState, ainsi que de son propre état, ownState
     * @param newState nouvel état public de jeu
     * @param ownState propre état privé du joueur
     */
    void updateState(PublicGameState newState, PlayerState ownState);

    /**
     * Est appelée au début de la partie pour communiquer au joueur les cinq billets qu'on lui distribue
     * @param tickets Les 5 billets distribués au joueur
     */
    void setInitialTicketChoice(SortedBag<Ticket> tickets);

    /**
     * Est appelée au début de la partie pour demander au joueur quels billets on lui a initalement distribué
     * au moyen de <code>setInitialTicketChoice()</code>
     * @return Les billets initialement distribués au joueur
     */
    SortedBag<Ticket> chooseInitialTickets();

    /**
     * est appelée au début du tour d'un joueur, pour savoir quel type d'action il désire effectuer durant ce tour
     * @return le type d'action que le joueur souhaite effectuer
     */
    TurnKind nextTurn();

    /**
     * Est appelée lorsque le joueur a décidé de tirer des billets supplémentaires en cours de partie
     * @param options Liste des billets que le joueur peut choisir
     * @return Liste des billets que le joueur a choisi
     */
    SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options);

    /**
     * Est appelée lorsque le joueur a décidé de tirer des cartes wagon / locomotive afin de savoir d'où il désire les tirer
     * @return (0,4) inclus s'il tire les cartes depuis les cartes face visible
     * <code>Constants.DECK_SLOT</code> s'il tire les cartes depuis la pioche
     */
    int drawSlot();

    /**
     * est appelée lorsque le joueur a décidé de tenter de s'emparer d'une route, afin de savoir de quelle route il s'agit
     * @return la route dont le joueur tente de s'emparer
     */
    Route claimedRoute();

    /**
     * est appelée lorsque le joueur a décidé de tenter de s'emparer d'une route, afin de savoir quelle(s) carte(s) il désire initialement utiliser pour cela
     * @return les cartes que le joueur désire initialement utiliser
     */
    SortedBag<Card> initialClaimCards();

    /**
     * est appelée lorsque le joueur a décidé de tenter de s'emparer d'un tunnel et que des cartes additionnelles sont nécessaires,
     * afin de savoir quelle(s) carte(s) il désire utiliser pour cela, les possibilités lui étant passées en argument
     * si le multiensemble retourné est vide, cela signifie que le joueur ne désire pas (ou ne peut pas) choisir l'une de ces possibilités
     * @param options différentes possibilités de groupe de cartes supplémentaires à poser pour s'emparer de la route
     * @return la possibilité choisie, ou une liste vide si le joueur ne pouvait ou de voulait pas jouer de cartes supplémentaires
     */
    SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options);

    //----------BONUS----------
    void addSpectator(Spectator spectator);

    Player.TurnKind displayEndScreen(String endMessage);

    void endGame();

    //-------------------------
}
