package ch.epfl.tchu.net;

import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SpectatorProxy implements Spectator {
    private final Socket socket;
    public SpectatorProxy(Socket socket) {
        this.socket = socket;
    }

    private void sendMessage(MessageId messageId, String... messageStrings){
        try{
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            w.write(messageId.name() + " ");
            String args = String.join(" ", messageStrings);
            w.write(args);
            w.write('\n');
            w.flush();
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }

    }



    @Override
    public void initSpectator(PlayerId playerId, Map<PlayerId, String> playerNames) {
        Map<PlayerId, String> sortedPlayerNames = new EnumMap<>(playerNames);
        String sPlayerId = Serdes.playerIdSerde.serialize(playerId);
        String playerNamesSerialized = Serdes.listOfString.serialize(new ArrayList<>(sortedPlayerNames.values()));
        sendMessage(MessageId.INIT_SPECTATOR, sPlayerId, playerNamesSerialized);
    }

    @Override
    public void setState(PublicGameState gameState, PlayerState playerState) {
        String sGameState = Serdes.publicGameStateSerde.serialize(gameState);
        String sPlayerState = Serdes.playerStateSerde.serialize(playerState);
        sendMessage(MessageId.UPDATE_STATE, sGameState, sPlayerState);
    }

    @Override
    public void receiveInfo(String info) {
        String sInfo = Serdes.stringSerde.serialize(info);
        sendMessage(MessageId.RECEIVE_INFO, sInfo);
    }
}
