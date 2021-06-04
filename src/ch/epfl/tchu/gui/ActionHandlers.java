package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Ticket;


/**
 * Interface des gestionnaires d'action
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public interface ActionHandlers{

    /**
     * gestionnaire d'action de tirage de billets
     */
    @FunctionalInterface
    interface DrawTicketsHandler {
        void onDrawTickets();
    }

    /**
     * gestionnaire d'action de tirage de cartes
     */
    @FunctionalInterface
    interface DrawCardHandler {
        void onDrawCard(int cardSlot);
    }

    /**
     * gestionnaire d'action de tenter de s'emparer d'une route
     */
    @FunctionalInterface
    interface ClaimRouteHandler {
        void onClaimRoute(Route routeToClaim, SortedBag<Card> claimCards);
    }

    /**
     * gestionnaire d'action de choix de tickets
     */
    @FunctionalInterface
    interface ChooseTicketsHandler {
        void onChooseTickets(SortedBag<Ticket> keptTickets);
    }

    /**
     * gestionnaire d'action de choix de cartes
     */
    @FunctionalInterface
    interface ChooseCardsHandler{
        void onChooseCards(SortedBag<Card> chosenAdditionalCards);
    }

}
