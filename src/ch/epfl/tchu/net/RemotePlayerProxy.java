package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Classe représentant le mandataire du joueur à distance
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */

public final class RemotePlayerProxy implements Player {
    private final Socket socket;

    /**
     * Construit un mandataire de joueur distant
     * @param socket la "prise" que le mandataire utilise pour communiquer à travers le réseau avec le client par échange de messages textuels
     */
    public RemotePlayerProxy(Socket socket){
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

    private String receiveMessage() {
        String serialized;
        try{

            BufferedReader r = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), UTF_8)
            );

            serialized = r.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return serialized;
    }

    /**
     * sérialise et envoie au serveur l'ordre de communiquer au joueur sa propre identité ownId, ainsi que les noms des différents joueurs, le sien inclus, qui se trouvent dans playerNames
     * @param ownId propre identité du joueur
     * @param playerNames noms des différents joueurs
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        String playerIdString = Serdes.playerIdSerde.serialize(ownId);
        Map<PlayerId, String> sortedPlayerNames = new EnumMap<>(playerNames);
        String playerNamesSerialized = Serdes.listOfString.serialize(new ArrayList<>(sortedPlayerNames.values()));
        sendMessage(MessageId.INIT_PLAYERS, playerIdString, playerNamesSerialized);
    }

    /**
     * sérialise et envoie au serveur l'ordre de communiquer une information au joueur courant de la partie
     * @param info String, information que l'on souhaite communiquer au joueur courant
     */
    @Override
    public void receiveInfo(String info) {
        String s = Serdes.stringSerde.serialize(info);
        sendMessage(MessageId.RECEIVE_INFO, s);
    }

    /**
     * sérialise et envoie au serveur l'ordre d'informer le joueur de la composante publique de ce nouvel état, newState, ainsi que de son propre état, ownState
     * @param newState nouvel état public de jeu
     * @param ownState propre état privé du joueur
     */
    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        String serializedPGS = Serdes.publicGameStateSerde.serialize(newState);
        String serializedPS = Serdes.playerStateSerde.serialize(ownState);
        sendMessage(MessageId.UPDATE_STATE, serializedPGS, serializedPS);
    }

    /**
     * sérialise et envoie au serveur l'ordre de communiquer au joueur les cinq billets qu'on lui distribue
     * @param tickets Les 5 billets distribués au joueur
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        String serializedTickets = Serdes.bagOfTicket.serialize(tickets);
        sendMessage(MessageId.SET_INITIAL_TICKETS, serializedTickets);
    }

    /**
     * sérialise et envoie au serveur l'ordre de demander au joueur quels billets le joueur a choisi pour commencer la partie
     * @return Les billets initialement distribués au joueur
     */
    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        sendMessage(MessageId.CHOOSE_INITIAL_TICKETS);
        return Serdes.bagOfTicket.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur un ordre pour savoir quel type d'action il désire effectuer durant ce tour
     * @return le type d'action que le joueur souhaite effectuer
     */
    @Override
    public TurnKind nextTurn() {
        sendMessage(MessageId.NEXT_TURN);
        return Serdes.turnKindSerde.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur l'ordre de demander au joueur quels billets supplémentaires le joueur a choisi de tirer
     * @param options Liste des billets que le joueur peut choisir
     * @return la liste des billets que le joueur a choisi
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        String sOptions = Serdes.bagOfTicket.serialize(options);
        sendMessage(MessageId.CHOOSE_TICKETS, sOptions);
        return Serdes.bagOfTicket.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur un ordre pour savoir d'où il désire tirer les cartes
     * @return (0,4) inclus s'il tire les cartes depuis les cartes face visible
     */
    @Override
    public int drawSlot() {
        sendMessage(MessageId.DRAW_SLOT);
        return Serdes.integerSerde.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur un ordre pour savoir quelle route le joueur a décidé de tenter de s'emparer
     * @return la route dont le joueur tente de s'emparer
     */
    @Override
    public Route claimedRoute() {
        sendMessage(MessageId.ROUTE);
        return Serdes.routeSerde.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur un ordre pour savoir quelle(s) carte(s) le joueur désire initialement utiliser pour tenter de s'emparer d'une route
     * @return les cartes que le joueur désire initialement utiliser
     */
    @Override
    public SortedBag<Card> initialClaimCards() {
        sendMessage(MessageId.CARDS);
        return Serdes.bagOfCard.deserialize(receiveMessage());
    }

    /**
     * sérialise et envoie au serveur un ordre pour savoir quelle(s) carte(s) supplémentaire(s)
     * le joueur désire utiliser pour tenter de s'emparer d'un tunnel
     * @param options différentes possibilités de groupe de cartes supplémentaires à poser pour s'emparer de la route
     * @return la possibilité choisie, ou un SortedBag vide si le joueur ne pouvait ou de voulait pas jouer de cartes supplémentaires
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        String sOptions = Serdes.listOfBagOfCard.serialize(options);
        sendMessage(MessageId.CHOOSE_ADDITIONAL_CARDS, sOptions);
        return Serdes.bagOfCard.deserialize(receiveMessage());
    }

    @Override
    public void addSpectator(Spectator spectator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player.TurnKind displayEndScreen(String endMessage) {
        String sEndMessage = Serdes.stringSerde.serialize(endMessage);
        sendMessage(MessageId.DISPLAY_END, sEndMessage);
        return Serdes.turnKindSerde.deserialize(receiveMessage());
    }

    @Override
    public void endGame() {
        sendMessage(MessageId.END);
    }
}
