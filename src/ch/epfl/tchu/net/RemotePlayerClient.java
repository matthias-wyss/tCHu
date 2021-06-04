package ch.epfl.tchu.net;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import ch.epfl.tchu.gui.Helper;
import ch.epfl.tchu.gui.Main;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.application.Platform.runLater;

/**
 * Classe représentant le client du joueur distant
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class RemotePlayerClient {

    private final Player player;
    private final String hostName;
    private final int port;

    /**
     * Construit un client de joueur distant
     * @param player joueur auquel il doit fournir un accès distant
     * @param hostName adresse IP du joueur - serveur
     * @param port port de conection utilisé
     */
    public RemotePlayerClient(Player player, String hostName, int port) {
        Preconditions.checkArgument(!hostName.equals("") && port >= 1024);
        this.player = player;
        this.hostName = hostName;
        this.port = port;
    }

    /**
     * Effectue une boucle durant laquelle :
     *   - on attend un message en provenance du mandataire
     *   - on découpe ce message en utilisant le caractère d'espacement comme séparateur
     *   - on détermine le type du message en fonction de la première chaîne résultant du découpage
     *   - en fonction de ce type de message :
     *        - on désérialise les arguments
     *        - on appelle la méthode correspondante du joueur
     *        - si cette méthode retourne un résultat, on le sérialise pour le renvoyer au mandataire en réponse
     */
    public void run() {
        String message;
        try (Socket s = new Socket(hostName,port)){

            BufferedReader r = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), UTF_8)
            );

            while((message = r.readLine()) != null){
                String[] splitMessage = message.split(Pattern.quote(" "));
                MessageId messageId = MessageId.valueOf(splitMessage[0]);
                switch(messageId){
                    case INIT_PLAYERS:
                        PlayerId ownId = Serdes.playerIdSerde.deserialize(splitMessage[1]);
                        List<String> names = Serdes.listOfString.deserialize(splitMessage[2]);
                        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);
                        for (PlayerId playerId: PlayerId.ALL) {
                            playerNames.put(playerId, names.get(playerId.ordinal()));
                        }

                        player.initPlayers(ownId, playerNames);
                        break;
                    case RECEIVE_INFO:
                        String info = Serdes.stringSerde.deserialize(splitMessage[1]);
                        player.receiveInfo(info);
                        break;
                    case UPDATE_STATE:
                        PublicGameState PGS = Serdes.publicGameStateSerde.deserialize(splitMessage[1]);
                        PlayerState PS = Serdes.playerStateSerde.deserialize(splitMessage[2]);
                        player.updateState(PGS, PS);
                        break;
                    case SET_INITIAL_TICKETS:
                        SortedBag<Ticket> tickets = Serdes.bagOfTicket.deserialize(splitMessage[1]);
                        player.setInitialTicketChoice(tickets);
                        break;
                    case CHOOSE_INITIAL_TICKETS:
                        String chosenInitialTickets = Serdes.bagOfTicket.serialize(player.chooseInitialTickets());
                        sendMessage(chosenInitialTickets, s);
                        break;
                    case NEXT_TURN:
                        String turnKind = Serdes.turnKindSerde.serialize(player.nextTurn());
                        sendMessage(turnKind, s);
                        break;
                    case CHOOSE_TICKETS:
                        SortedBag<Ticket> options = Serdes.bagOfTicket.deserialize(splitMessage[1]);
                        String chosenTickets = Serdes.bagOfTicket.serialize(player.chooseTickets(options));
                        sendMessage(chosenTickets, s);
                        break;
                    case DRAW_SLOT:
                        String slot = Serdes.integerSerde.serialize(player.drawSlot());
                        sendMessage(slot, s);
                        break;
                    case ROUTE:
                        String route = Serdes.routeSerde.serialize(player.claimedRoute());
                        sendMessage(route, s);
                        break;
                    case CARDS:
                        String cards = Serdes.bagOfCard.serialize(player.initialClaimCards());
                        sendMessage(cards, s);
                        break;
                    case CHOOSE_ADDITIONAL_CARDS:
                        List<SortedBag<Card>> opts = Serdes.listOfBagOfCard.deserialize(splitMessage[1]);
                        String chosenAdditionalCards = Serdes.bagOfCard.serialize(player.chooseAdditionalCards(opts));
                        sendMessage(chosenAdditionalCards, s);
                        break;
                    case DISPLAY_END:
                        String endMessage = Serdes.stringSerde.deserialize(splitMessage[1]);
                        Player.TurnKind choice = player.displayEndScreen(endMessage);
                        String sChoice = Serdes.turnKindSerde.serialize(choice);
                        sendMessage(sChoice, s);
                        break;
                    case END:
                        player.endGame();
                        break;
                }

            }


        } catch (IOException e) {
            //----------BONUS----------
            Platform.runLater(Main::createHomeWindow);
            Helper.showGameNotFoundWindow();
            //-------------------------
            throw new UncheckedIOException(e);
        }
    }

    private static void sendMessage(String message, Socket socket){
        try{
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            w.write(message);
            w.write('\n');
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
