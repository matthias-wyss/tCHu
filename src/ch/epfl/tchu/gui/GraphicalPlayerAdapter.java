package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


import static javafx.application.Platform.runLater;

//---------- CONTIENT DU BONUS ----------
/**
 * Classe adaptrice de <code>GraphicalPlayer</code> en <code>Player</code>
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class GraphicalPlayerAdapter implements Player {
    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<SortedBag<Ticket>> sbTicketsQueue;
    private final BlockingQueue<Integer> cardSlotQueue;
    private final BlockingQueue<Route> routeQueue;
    private final BlockingQueue<SortedBag<Card>> sbCardQueue;

    /**
     * Construit un adapteur de GraphicalPlayer
     */
    public GraphicalPlayerAdapter(){
        sbTicketsQueue = new ArrayBlockingQueue<>(1);
        cardSlotQueue = new ArrayBlockingQueue<>(1);
        routeQueue = new ArrayBlockingQueue<>(1);
        sbCardQueue = new ArrayBlockingQueue<>(1);
    }


    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        BlockingQueue<GraphicalPlayer> playerQueue = new ArrayBlockingQueue<>(1);
        runLater(() -> playerQueue.add(new GraphicalPlayer(ownId, playerNames)));
        try {
            graphicalPlayer = playerQueue.take();
        }  catch (InterruptedException e){
            throw new Error();
        }
    }

    @Override
    public void receiveInfo(String info) {
        runLater(() -> graphicalPlayer.receiveInfo(info));
    }

    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        runLater(() -> graphicalPlayer.setState(newState, ownState));
    }

    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        ActionHandlers.ChooseTicketsHandler ctHandler = (x) -> {
            try {
                sbTicketsQueue.put(x);
            } catch (InterruptedException e) {
                throw new Error();
            }
        };
        runLater(() -> graphicalPlayer.chooseTickets(tickets, ctHandler));
    }

    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        try {
            return sbTicketsQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    @Override
    public TurnKind nextTurn() {
        try{
            BlockingQueue<TurnKind> result = new ArrayBlockingQueue<>(1);
            ActionHandlers.DrawTicketsHandler drawTicketsHandler = () -> {
                try {
                    result.put(TurnKind.DRAW_TICKETS);
                } catch (InterruptedException e) {
                    throw new Error();
                }
            };

            ActionHandlers.DrawCardHandler drawCardsHandler = (x) -> {

                try {
                    cardSlotQueue.put(x);
                    result.put(TurnKind.DRAW_CARDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            ActionHandlers.ClaimRouteHandler claimRouteHandler = (x,y) -> {
                try {
                    routeQueue.put(x);
                    sbCardQueue.put(y);
                    result.put(TurnKind.CLAIM_ROUTE);
                } catch (InterruptedException e) {
                    throw new Error();
                }
            };

            runLater(() -> graphicalPlayer.startTurn(drawTicketsHandler, drawCardsHandler,claimRouteHandler));

            return result.take();

        } catch (InterruptedException e) {
            throw new Error();
        }

    }

    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        setInitialTicketChoice(options);
        return chooseInitialTickets();
    }

    @Override
    public int drawSlot() {
            if (cardSlotQueue.isEmpty()) {
                ActionHandlers.DrawCardHandler dcHandler = (x) -> {
                    try {
                        cardSlotQueue.put(x);
                    } catch (InterruptedException e) {
                        throw new Error();
                    }
                };
                runLater(() -> graphicalPlayer.drawCard(dcHandler));
            }
            try{
                return cardSlotQueue.take();
            } catch(InterruptedException e){
                throw new Error();
            }
    }

    @Override
    public Route claimedRoute() {
        try {
            return routeQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    @Override
    public SortedBag<Card> initialClaimCards() {
        try {
            return sbCardQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        BlockingQueue<SortedBag<Card>> additionalCardsQueue = new ArrayBlockingQueue<>(1);
        ActionHandlers.ChooseCardsHandler ccHandler = (x) -> {
            try {
                additionalCardsQueue.put(x);
            } catch (InterruptedException e) {
                throw new Error();
            }
        };
        runLater(() -> graphicalPlayer.chooseAdditionalCards(options, ccHandler));
        try {
            return additionalCardsQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }


    //----------BONUS----------
    @Override
    public void addSpectator(Spectator spectator) {
        runLater(() -> graphicalPlayer.addSpectator(spectator));
    }

    @Override
    public Player.TurnKind displayEndScreen(String endMessage) {
        BlockingQueue<TurnKind> result = new ArrayBlockingQueue<>(1);

        runLater(() -> {
            try {
                result.put(graphicalPlayer.showEndScreen(endMessage));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        try {
            return result.take();
        } catch (InterruptedException e) {
            throw new Error();
        }

    }

    @Override
    public void endGame() {
        runLater(graphicalPlayer::endGame);
    }
    //-------------------------

}
