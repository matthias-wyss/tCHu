package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 * Classe contenant la totalité des serdes utilisées par la communication réseau de tCHU
 */
public final class Serdes {
    private Serdes(){

    }

    /** Serde d'entier */
    public static final Serde<Integer> integerSerde = Serde.of(i -> Integer.toString(i), Integer::parseInt);

    /** Serde de chaîne de caractères (String) */
    public static final Serde<String> stringSerde = Serde.of(i -> Base64.getEncoder().encodeToString(i.getBytes(StandardCharsets.UTF_8)),
           i-> new String(Base64.getDecoder().decode(i), StandardCharsets.UTF_8));

    /** Serde de PlayerId */
    public static final Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.ALL);

    /** Serde de TurnKind */
    public static final Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);

    /** Serde de Card */
    public static final Serde<Card> cardSerde = Serde.oneOf(Card.ALL);

    /** Serde de Route */
    public static final Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());

    /** Serde de Tickets */
    public static final Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());

    /** Serde de liste de chaînes de caractères (String) */
    public static final Serde<List<String>> listOfString = Serde.listOf(stringSerde, ',');

    /** Serde de liste de cartes */
    public static final Serde<List<Card>> listOfCard = Serde.listOf(cardSerde, ',');

    /** Serde de liste de routes  */
    public static final Serde<List<Route>> listOfRoute = Serde.listOf(routeSerde, ',');

    /** Serde de SortedBag de cartes */
    public static final Serde<SortedBag<Card>> bagOfCard = Serde.bagOf(cardSerde, ',');

    /** Serde de SortedBag de tickets */
    public static final Serde<SortedBag<Ticket>> bagOfTicket = Serde.bagOf(ticketSerde, ',');

    /** Serde de liste de SortedBags de cartes */
    public static final Serde<List<SortedBag<Card>>> listOfBagOfCard = Serde.listOf(bagOfCard, ';');

    /** Serde de PublicCardState */
    public static final Serde<PublicCardState> publicCardStateSerde = Serde.of(
            i -> String.join(";", listOfCard.serialize(i.faceUpCards()), integerSerde.serialize(i.deckSize()), integerSerde.serialize(i.discardsSize())),
            i -> {
                String[] stringToTable= i.split(Pattern.quote(";"), -1);
                return new PublicCardState(listOfCard.deserialize(stringToTable[0]), integerSerde.deserialize(stringToTable[1]), integerSerde.deserialize(stringToTable[2]));
            });

    /** Serde de PublicPlayerState */
    public static final Serde<PublicPlayerState> publicPlayerStateSerde = Serde.of(
            i -> String.join(";", integerSerde.serialize(i.ticketCount()), integerSerde.serialize(i.cardCount()), listOfRoute.serialize(i.routes())),
            i -> {
                String[] stringToTable = i.split(Pattern.quote(";"), -1);
                return new PublicPlayerState(integerSerde.deserialize(stringToTable[0]), integerSerde.deserialize(stringToTable[1]), listOfRoute.deserialize(stringToTable[2]));
            });

    /** Serde de PlayerState */
    public static final Serde<PlayerState> playerStateSerde = Serde.of(
            i -> String.join(";", bagOfTicket.serialize(i.tickets()), bagOfCard.serialize(i.cards()), listOfRoute.serialize(i.routes())),
            i -> {
                String[] stringToTable = i.split(Pattern.quote(";"), -1);
                return new PlayerState(bagOfTicket.deserialize(stringToTable[0]), bagOfCard.deserialize(stringToTable[1]), listOfRoute.deserialize(stringToTable[2]));
            });

    /** Serde de PublicGameState */
    public static final Serde<PublicGameState> publicGameStateSerde = Serde.of(i -> {
        String lastPlayerSerialized = i.lastPlayer() == null ? "" : playerIdSerde.serialize(i.lastPlayer());

        return String.join(":", integerSerde.serialize(i.ticketsCount()), publicCardStateSerde.serialize(i.cardState()),
                        playerIdSerde.serialize(i.currentPlayerId()), publicPlayerStateSerde.serialize(i.playerState(PlayerId.PLAYER_1)), publicPlayerStateSerde.serialize(i.playerState(PlayerId.PLAYER_2)), lastPlayerSerialized);
            },
            i -> {
                String[] stringToTable = i.split(Pattern.quote(":"), -1);
                PlayerId lastPlayerDeserialized = stringToTable[5].isBlank() ? null : playerIdSerde.deserialize(stringToTable[5]);
                return new PublicGameState(integerSerde.deserialize(stringToTable[0]), publicCardStateSerde.deserialize(stringToTable[1]), playerIdSerde.deserialize(stringToTable[2]), Map.of(PlayerId.PLAYER_1, publicPlayerStateSerde.deserialize(stringToTable[3]), PlayerId.PLAYER_2, publicPlayerStateSerde.deserialize(stringToTable[4])), lastPlayerDeserialized);
            });
}


