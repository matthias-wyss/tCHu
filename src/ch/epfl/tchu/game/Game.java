package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import static javafx.application.Platform.runLater;

//---------- CONTIENT DU BONUS ----------

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Game {
    private Game() {

    }
    private final static int CARD_DRAWS_PER_TURN = 2;

    private static final SimpleBooleanProperty gameHasStarted = new SimpleBooleanProperty();

    /**t
     * Fait jouer une partie de tCHu aux joueurs donnés
     * @param players joueurs et leurs identités
     * @param playerNames table de noms des joueurs
     * @param tickets billets disponibles pour cette partie
     * @param rng générateur de nombre aléatoire pour mélanger les cartes de la défausse et faire une nouvelle pioche
     */
    public static void play(Map<PlayerId, Player> players, Map<PlayerId, String> playerNames, SortedBag<Ticket> tickets, Random rng) {
        Preconditions.checkArgument(players.size() == PlayerId.ALL.size() && playerNames.size() == PlayerId.ALL.size());

        gameHasStarted.set(true);

        List<PlayerId> allPlayers = PlayerId.ALL;
        Map<PlayerId,Info> infoMap = new EnumMap<>(PlayerId.class);
        GameState gameState = GameState.initial(tickets, rng);
        for(Map.Entry<PlayerId,String> e : playerNames.entrySet()){
            players.get(e.getKey()).initPlayers(e.getKey(), playerNames);
            infoMap.put(e.getKey(), new Info(playerNames.get(e.getKey())));
        }
        receiveInfoBoth(infoMap.get(gameState.currentPlayerId()).willPlayFirst(), players);

        for(PlayerId id : allPlayers) {
            players.get(id).setInitialTicketChoice(gameState.topTickets(Constants.INITIAL_TICKETS_COUNT));
            gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);
            players.get(id).updateState(gameState, gameState.playerState(id));
        }
        Map<PlayerId, Integer> keptTicketsSizes = new HashMap<>();
        for(PlayerId id : allPlayers){
            SortedBag<Ticket> keptTicketsBag = players.get(id).chooseInitialTickets();
            gameState = gameState.withInitiallyChosenTickets(id, keptTicketsBag);
            keptTicketsSizes.put(id, keptTicketsBag.size());
        }
        keptTicketsSizes.forEach((x,y) -> receiveInfoBoth(infoMap.get(x).keptTickets(y), players));



        do {
            gameState = newTurn(players, rng, gameState, infoMap);
        } while(!gameState.lastTurnBegins() && gameState.lastPlayer() == null);
        for(PlayerId id : PlayerId.ALL){
            gameState = newTurn(players, rng, gameState, infoMap);
        }


        int maxLength = 0;
        Map<PlayerId, Trail> playerLongestTrailsMap = new EnumMap<>(PlayerId.class);
        for (PlayerId id : allPlayers) {
            PlayerState ps = gameState.playerState(id);
            Trail longestTrail = Trail.longest(ps.routes());
            if(longestTrail.length() > maxLength){
                maxLength = longestTrail.length();
            }
            playerLongestTrailsMap.put(id, longestTrail);

        }

        Map<PlayerId, Integer> playerScores = new EnumMap<>(PlayerId.class);
        for (Map.Entry<PlayerId, Trail> entry : playerLongestTrailsMap.entrySet()) {
            PlayerId id = entry.getKey();
            if(entry.getValue().length() == maxLength){
                playerScores.put(id, gameState.playerState(id).finalPoints() + Constants.LONGEST_TRAIL_BONUS_POINTS);
                receiveInfoBoth(infoMap.get(id).getsLongestTrailBonus(Trail.longest(gameState.playerState(id).routes())), players);
            } else {
                playerScores.put(id, gameState.playerState(id).finalPoints());
            }
        }


        boolean draw = false;
        int maxScore = Integer.MIN_VALUE;
        PlayerId winner = null;
        for(Map.Entry<PlayerId, Integer> entry : playerScores.entrySet()){
            if(entry.getValue() > maxScore){
                maxScore = entry.getValue();
                winner = entry.getKey();
            }

            if(entry.getKey() == PlayerId.PLAYER_2 && entry.getValue().equals(playerScores.get(PlayerId.PLAYER_2.next()))) {
                draw = true;
            }
        }

        updateBothStates(gameState, players);

        //----------BONUS----------
        BlockingQueue<Player.TurnKind> player1Choice = new ArrayBlockingQueue<>(1);
        BlockingQueue<Player.TurnKind> player2Choice = new ArrayBlockingQueue<>(1);
        //-------------------------

        if(draw){
            receiveInfoBoth(Info.draw(new ArrayList<>(playerNames.values()), maxScore), players);

            //----------BONUS----------
            int finalMaxScore1 = maxScore;

            new Thread(() -> {
                try {
                    Player.TurnKind result = players.get(PlayerId.PLAYER_1).displayEndScreen(Info.draw(new ArrayList<>(playerNames.values()), finalMaxScore1));
                    player1Choice.put(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
            new Thread(() -> {
                try {
                    Player.TurnKind result = players.get(PlayerId.PLAYER_2).displayEndScreen(Info.draw(new ArrayList<>(playerNames.values()), finalMaxScore1));
                    player2Choice.put(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
            //-------------------------

        } else {
            receiveInfoBoth(infoMap.get(winner).won(maxScore, playerScores.get(winner.next())), players);

            //----------BONUS----------
            PlayerId finalWinner = winner;
            int finalMaxScore = maxScore;
            new Thread( () -> {

                try {
                    Player.TurnKind result = players.get(PlayerId.PLAYER_1).displayEndScreen(infoMap.get(finalWinner).
                            won(finalMaxScore, playerScores.get(finalWinner.next())));
                    player1Choice.put(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread( () -> {

                try {
                    Player.TurnKind result = players.get(PlayerId.PLAYER_2).displayEndScreen(infoMap.get(finalWinner).
                            won(finalMaxScore, playerScores.get(finalWinner.next())));
                    player2Choice.put(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            //-------------------------

        }

        //----------BONUS----------
        CountDownLatch latch = new CountDownLatch(2);
        List<Boolean> checksEnd = new ArrayList<>();
        new Thread(() -> {
            checksEnd.add(checkEndCondition(player1Choice, players));
            latch.countDown();

        }).start();

        new Thread(() -> {
            checksEnd.add(checkEndCondition(player2Choice, players));
            latch.countDown();

        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if(checksEnd.stream().allMatch(e -> e)){
            players.values().forEach(Player::endGame);
            play(players, playerNames, tickets, rng);
        }
        //-------------------------

    }

    private static GameState newTurn(Map<PlayerId, Player> players, Random rng, GameState gameState, Map<PlayerId, Info> infoMap) {
        PlayerId currentPlayerId = gameState.currentPlayerId();
        Player player = players.get(currentPlayerId);
        receiveInfoBoth(infoMap.get(currentPlayerId).canPlay(), players);
        updateBothStates(gameState, players);
        Player.TurnKind turnKind = player.nextTurn();
        Info playerInfo = infoMap.get(currentPlayerId);

        switch(turnKind){
            case DRAW_TICKETS:
                receiveInfoBoth(playerInfo.drewTickets(Constants.IN_GAME_TICKETS_COUNT), players);
                SortedBag<Ticket> chosenTickets = player.chooseTickets(gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT));
                gameState = gameState.withChosenAdditionalTickets(gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT), chosenTickets);
                receiveInfoBoth(playerInfo.keptTickets(chosenTickets.size()), players);
                if(gameState.lastTurnBegins()){
                    receiveInfoBoth(playerInfo.lastTurnBegins(gameState.currentPlayerState().carCount()), players);
                }
                gameState = gameState.forNextTurn();

                return gameState;
            case DRAW_CARDS:
                for(int i = 0 ; i < CARD_DRAWS_PER_TURN; ++i) {
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    if(i==1){
                        updateBothStates(gameState, players);
                    }
                    int drawnSlot = player.drawSlot();
                    if(drawnSlot == Constants.DECK_SLOT) {
                        gameState = gameState.withBlindlyDrawnCard();
                        receiveInfoBoth(playerInfo.drewBlindCard(), players);
                    } else if((Constants.FACE_UP_CARD_SLOTS.contains(drawnSlot))){
                        receiveInfoBoth(playerInfo.drewVisibleCard(gameState.cardState().faceUpCards().get(drawnSlot)), players);
                        gameState = gameState.withDrawnFaceUpCard(drawnSlot);

                    }
                }
                if(gameState.lastTurnBegins()){
                    receiveInfoBoth(playerInfo.lastTurnBegins(gameState.currentPlayerState().carCount()), players);
                }
                gameState = gameState.forNextTurn();

                return gameState;

            case CLAIM_ROUTE:
                Route route = player.claimedRoute();
                if(gameState.currentPlayerState().canClaimRoute(route)) {
                    SortedBag<Card> initialClaimCards = player.initialClaimCards();
                    if(route.level() == Route.Level.UNDERGROUND) {

                        receiveInfoBoth(playerInfo.attemptsTunnelClaim(route, initialClaimCards), players);

                        SortedBag.Builder<Card> drawnCardsBuilder = new SortedBag.Builder<>();
                        for(int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; ++i) {
                            gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                            drawnCardsBuilder.add(gameState.topCard());
                            gameState = gameState.withoutTopCard();
                        }
                        SortedBag<Card> drawnCards = drawnCardsBuilder.build();
                        int additionalClaimCards = route.additionalClaimCardsCount(initialClaimCards, drawnCards);
                        receiveInfoBoth(playerInfo.drewAdditionalCards(drawnCards, additionalClaimCards), players);
                        gameState = gameState.withMoreDiscardedCards(drawnCards);
                        SortedBag<Card> chosenAdditionalCards = SortedBag.of();
                        if(additionalClaimCards>0){
                            List<SortedBag<Card>> possibleAdditionalCards = gameState.playerState(currentPlayerId)
                                    .possibleAdditionalCards(additionalClaimCards, initialClaimCards);
                            if(possibleAdditionalCards.size()>0){
                                chosenAdditionalCards = player.chooseAdditionalCards(possibleAdditionalCards);
                            }

                        }

                        if(chosenAdditionalCards.isEmpty() && additionalClaimCards > 0) {
                            receiveInfoBoth(playerInfo.didNotClaimRoute(route), players);
                        } else {
                            SortedBag<Card> cardsToRemove = initialClaimCards.union(chosenAdditionalCards);
                            gameState = gameState.withClaimedRoute(route, cardsToRemove);
                            receiveInfoBoth(playerInfo.claimedRoute(route, cardsToRemove), players);
                        }
                    } else {
                        gameState = gameState.withClaimedRoute(route, initialClaimCards);
                        receiveInfoBoth(playerInfo.claimedRoute(route, initialClaimCards), players);

                    }

                } else {
                    receiveInfoBoth(playerInfo.didNotClaimRoute(route), players);
                }
                if(gameState.lastTurnBegins()){
                    receiveInfoBoth(playerInfo.lastTurnBegins(gameState.currentPlayerState().carCount()), players);
                }
                gameState = gameState.forNextTurn();

                return gameState;

        }

        return gameState;
    }

    private static void receiveInfoBoth(String info, Map<PlayerId, Player> players){
        PlayerId.ALL.forEach(id -> players.get(id).receiveInfo(info));
    }

    private static void updateBothStates(GameState gameState, Map<PlayerId, Player> players){
        PlayerId.ALL.forEach(id -> players.get(id).updateState(gameState, gameState.playerState(id)));
    }


    //----------BONUS----------
    private static boolean checkEndCondition(BlockingQueue<Player.TurnKind> playerChoice, Map<PlayerId, Player> players) {
        try {
            Player.TurnKind playerEnd = playerChoice.take();
            if (playerEnd == Player.TurnKind.QUIT) {
                runLater(() -> players.get(PlayerId.PLAYER_1).endGame());
                runLater(() -> players.get(PlayerId.PLAYER_2).endGame());
                return false;
            }
            return true;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

    public static SimpleBooleanProperty hasGameStarted() {
        return gameHasStarted;
    }
    //-------------------------

}