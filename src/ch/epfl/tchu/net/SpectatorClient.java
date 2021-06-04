package ch.epfl.tchu.net;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.game.PlayerState;
import ch.epfl.tchu.game.PublicGameState;
import ch.epfl.tchu.game.Spectator;
import ch.epfl.tchu.gui.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SpectatorClient {
    private final Spectator spectator;
    private final String hostName;
    private final int port;

    public SpectatorClient(Spectator player, String hostName, int port) {
        this.spectator = player;
        this.hostName = hostName;
        this.port = port;
    }

    public void run() {
        String message;
        try (Socket s = new Socket(hostName, port)) {

            BufferedReader r = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), UTF_8)
            );

            while ((message = r.readLine()) != null) {
                String[] splitMessage = message.split(Pattern.quote(" "));
                MessageId messageId = MessageId.valueOf(splitMessage[0]);
                switch (messageId) {
                    case INIT_SPECTATOR:
                        PlayerId dPlayerId = Serdes.playerIdSerde.deserialize(splitMessage[1]);
                        List<String> names = Serdes.listOfString.deserialize(splitMessage[2]);
                        Map<PlayerId, String> sortedPlayerNames = new EnumMap<>(PlayerId.class);
                        for(PlayerId id : PlayerId.ALL){
                            sortedPlayerNames.put(id, names.get(id.ordinal()));
                        }
                        spectator.initSpectator(dPlayerId, sortedPlayerNames);
                        break;
                    case UPDATE_STATE:
                        PublicGameState dPublicGameState = Serdes.publicGameStateSerde.deserialize(splitMessage[1]);
                        PlayerState dPlayerState = Serdes.playerStateSerde.deserialize(splitMessage[2]);
                        spectator.setState(dPublicGameState, dPlayerState);
                        break;
                    case RECEIVE_INFO:
                        String dInfo = Serdes.stringSerde.deserialize(splitMessage[1]);
                        spectator.receiveInfo(dInfo);
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        } catch (IOException e) {
            Helper.showGameNotFoundWindow();
            throw new UncheckedIOException(e);
        }

    }
}



