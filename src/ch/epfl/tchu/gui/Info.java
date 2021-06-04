package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Trail;

import java.util.List;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Info {

    private final String playerName;

    /**
     * Construit un générateur de messages liés au joueur ayant le nom donné
     *
     * @param playerName nom du joueur
     */
    public Info(String playerName) {
        this.playerName = playerName;
    }

    /**
     * retourne le nom de la carte donnée au singulier si la valeur absolue du second argument vaut 1, au pluriel sinon
     *
     * @param card carte dont on veut connaître le nom
     * @param count nombre de carte
     * @return le nom de la carte donnée au singulier si la valeur absolue du second argument vaut 1, au pluriel sinon
     */
    public static String cardName(Card card, int count){

        if(card.color() != null){
            switch(card.color()){
                case BLACK:
                    return StringsFr.BLACK_CARD + StringsFr.plural(count);
                case VIOLET:
                    return  StringsFr.VIOLET_CARD + StringsFr.plural(count);
                case BLUE:
                    return StringsFr.BLUE_CARD + StringsFr.plural(count);
                case GREEN:
                    return StringsFr.GREEN_CARD + StringsFr.plural(count);
                case YELLOW:
                    return StringsFr.YELLOW_CARD + StringsFr.plural(count);
                case ORANGE:
                    return StringsFr.ORANGE_CARD + StringsFr.plural(count);
                case RED:
                    return StringsFr.RED_CARD + StringsFr.plural(count);
                case WHITE:
                    return StringsFr.WHITE_CARD + StringsFr.plural(count);

            }
        }
        return StringsFr.LOCOMOTIVE_CARD + StringsFr.plural(count);

    }

    /**
     * retourne le message déclarant que les joueurs, dont les noms sont ceux donnés, ont terminé la partie ex æqo en ayant chacun remporté les points donnés
     *
     * @param playerNames noms des joueurs ex æqo
     * @param points nombre de leurs points
     * @return le message déclarant que les joueurs, dont les noms sont ceux donnés, ont terminé la partie ex æqo en ayant chacun remporté les points donnés
     */
    public static String draw(List<String> playerNames, int points){
        String playersSeparated = String.format("%s%s%s", playerNames.get(0), StringsFr.AND_SEPARATOR, playerNames.get(1));
        return String.format(StringsFr.DRAW, playersSeparated, points);
    }

    /**
     * retourne le message déclarant que le joueur jouera en premier
     *
     * @return le message déclarant que le joueur jouera en premier
     */
    public String willPlayFirst() {
        return String.format(StringsFr.WILL_PLAY_FIRST, this.playerName);
    }

    /**
     * retourne le message déclarant que le joueur a gardé le nombre de billets donné
     * @param count nombre de billets
     *
     * @return le message déclarant que le joueur a gardé le nombre de billets donné
     */
    public String keptTickets(int count) {
        return String.format(StringsFr.KEPT_N_TICKETS, this.playerName, count, StringsFr.plural(count));
    }

    /**
     * retourne le message déclarant que le joueur peut jouer
     *
     * @return le message déclarant que le joueur peut jouer
     */
    public String canPlay() {
        return String.format(StringsFr.CAN_PLAY, this.playerName);
    }

    /**
     * retourne le message déclarant que le joueur a tiré le nombre donné de billets
     *
     * @param count nombre de billets
     * @return le message déclarant que le joueur a tiré le nombre donné de billets
     */
    public String drewTickets(int count) {
        return String.format(StringsFr.DREW_TICKETS, this.playerName, count, StringsFr.plural(count));
    }

    /**
     * retourne le message déclarant que le joueur a tiré une carte à l'aveugle, c'est à dire du sommet de la pioche
     *
     * @return le message déclarant que le joueur a tiré une carte à l'aveugle, c'est à dire du sommet de la pioche
     */
    public String drewBlindCard() {
        return String.format(StringsFr.DREW_BLIND_CARD, this.playerName);
    }

    /**
     * retourne le message déclarant que le joueur a tiré la carte disposée face visible donnée
     *
     * @param card carte tirée
     * @return le message déclarant que le joueur a tiré la carte disposée face visible donnée
     */
    public String drewVisibleCard(Card card) {
        return String.format(StringsFr.DREW_VISIBLE_CARD, this.playerName, cardName(card, 1));
    }

    /**
     * retourne le message déclarant que le joueur s'est emparé de la route donnée au moyen des cartes données
     *
     * @param route route dont le joueur s'est emparée
     * @param cards cartes que le joueur a joué
     * @return le message déclarant que le joueur s'est emparé de la route donnée au moyen des cartes données
     */
    public String claimedRoute(Route route, SortedBag<Card> cards) {
        return String.format(StringsFr.CLAIMED_ROUTE, this.playerName, formatRoute(route), formatCards(cards));
    }

    /**
     * retourne le message déclarant que le joueur désire s'emparer de la route en tunnel donnée en utilisant initialement les cartes données
     *
     * @param route route dont le joueur désire s'emparer
     * @param initialCards cartes que le joueur a joué
     * @return le message déclarant que le joueur désire s'emparer de la route en tunnel donnée en utilisant initialement les cartes données
     */
    public String attemptsTunnelClaim(Route route, SortedBag<Card> initialCards) {
        return String.format(StringsFr.ATTEMPTS_TUNNEL_CLAIM, this.playerName, formatRoute(route), formatCards(initialCards));
    }

    /**
     * retourne le message déclarant que le joueur a tiré les trois cartes additionnelles données, et qu'elles impliquent un coût additionel du nombre de cartes donné
     *
     * @param drawnCards trois cartes additionnelles que le joueur a tiré
     * @param additionalCost nombre de cartes supplémentaires que le joueurs doit jouer pour s'emparer de la route
     * @return le message déclarant que le joueur a tiré les trois cartes additionnelles données, et qu'elles impliquent un coût additionel du nombre de cartes donné
     */
    public String drewAdditionalCards(SortedBag<Card> drawnCards, int additionalCost){
        String supplementaryCards = String.format(StringsFr.ADDITIONAL_CARDS_ARE, formatCards(drawnCards));
        return additionalCost == 0 ? supplementaryCards + StringsFr.NO_ADDITIONAL_COST : supplementaryCards + String.format(StringsFr.SOME_ADDITIONAL_COST, additionalCost, StringsFr.plural(additionalCost));
    }

    /**
     * retourne le message déclarant que le joueur n'a pas pu (ou voulu) s'emparer du tunnel donné
     *
     * @param route route dont le joueur a voulu s'emparer
     * @return le message déclarant que le joueur n'a pas pu (ou voulu) s'emparer du tunnel donné
     */
    public String didNotClaimRoute(Route route) {
        return String.format(StringsFr.DID_NOT_CLAIM_ROUTE, this.playerName, formatRoute(route));
    }

    /**
     * retourne le message déclarant que le joueur n'a plus que le nombre donné (et inférieur ou égale à 2) de wagons, et que le dernier tour commence donc
     *
     * @param carCount nombre restant de wagons du joueur
     * @return le message déclarant que le joueur n'a plus que le nombre donné (et inférieur ou égale à 2) de wagons, et que le dernier tour commence donc
     */
    public String lastTurnBegins(int carCount) {
        return String.format(StringsFr.LAST_TURN_BEGINS, this.playerName, carCount, StringsFr.plural(carCount));
    }

    /**
     * retourne le message déclarant que le joueur obtient le bonus de fin de partie grâce au chemin donné, qui est le plus long, ou l'un des plus longs
     *
     * @param longestTrail chemin le plus long construit lors de la partie
     * @return le message déclarant que le joueur obtient le bonus de fin de partie grâce au chemin donné, qui est le plus long, ou l'un des plus longs
     */
    public String getsLongestTrailBonus(Trail longestTrail) {

        return String.format(StringsFr.GETS_BONUS, this.playerName, longestTrail.station1() + StringsFr.EN_DASH_SEPARATOR + longestTrail.station2());
    }

    /**
     * retourne le message déclarant que le joueur remporte la partie avec le nombre de points donnés, son adversaire n'en ayant obtenu que le nombre de points donné en second argument
     *
     * @param points nombre de points du joueur (gagnant)
     * @param loserPoints nombre de points de l'adversaire (perdant)
     * @return le message déclarant que le joueur remporte la partie avec le nombre de points donnés, son adversaire n'en ayant obtenu que le nombre de points donné en second argument
     */
    public String won(int points, int loserPoints){
        return String.format(StringsFr.WINS, this.playerName, points, StringsFr.plural(points), loserPoints, StringsFr.plural(loserPoints));
    }

    public static String formatCards(SortedBag<Card> cards){
        StringBuilder formattedStringBuilder = new StringBuilder();
        int i = 0;
        for(Card card : cards.toSet()){
            String cardString = cards.countOf(card) + " " + cardName(card, cards.countOf(card));
            if(i == 0){
                formattedStringBuilder.append(cardString);
            }
            else if(i == cards.toSet().size()-1){
                formattedStringBuilder.append(StringsFr.AND_SEPARATOR).append(cardString);
            } else {
                formattedStringBuilder.append(", ").append(cardString);
            }
            ++i;
        }
        return formattedStringBuilder.toString();
    }

    private static String formatRoute(Route route) {
        return String.format("%s%s%s", route.station1().name(), StringsFr.EN_DASH_SEPARATOR, route.station2().name());
    }
}
