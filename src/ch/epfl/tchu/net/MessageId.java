package ch.epfl.tchu.net;

/**
 * Énumération des types de messages que le serveur peut envoyer aux clients
 *  @author Thibault Czarniak (327577)
 *  @author Matthias Wyss (329884)
 */
public enum MessageId {

    INIT_PLAYERS,
    RECEIVE_INFO,
    UPDATE_STATE,
    SET_INITIAL_TICKETS,
    CHOOSE_INITIAL_TICKETS,
    NEXT_TURN,
    CHOOSE_TICKETS,
    DRAW_SLOT,
    ROUTE,
    CARDS,
    CHOOSE_ADDITIONAL_CARDS,
    // -- BONUS --
    INIT_SPECTATOR,
    DISPLAY_END,
    END
    // -- BONUS --
}
