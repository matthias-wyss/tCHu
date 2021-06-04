package ch.epfl.tchu.game;

import java.util.Map;

public interface Spectator {

    void initSpectator(PlayerId playerId, Map<PlayerId, String> playerNames);

    void setState(PublicGameState gameState, PlayerState playerState);

    void receiveInfo(String info);
}
