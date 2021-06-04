package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Trip {

    private final Station stationFrom;
    private final Station stationTo;
    private final int points;

    /**
     * Construit un trajet entre 2 gares données et valant le nombre de points donné
     *
     * @param stationFrom
     *              Gare de départ
     * @param stationTo
     *              Gare d'arrivée
     * @param points
     *              Nombre de points
     */
    public Trip(Station stationFrom, Station stationTo, int points) {
        this.stationFrom = Objects.requireNonNull(stationFrom);
        this.stationTo = Objects.requireNonNull(stationTo);
        Preconditions.checkArgument(points > 0);
        this.points = points;

    }

    /**
     * Retourne la liste de tous les trajets possibles allant d'une des gares de la première liste (from)
     * à l'une des gares de la seconde liste (to), chacun valant le nombre de points donné
     *
     * @param from
     *              liste de toutes les gares de départ
     * @param to
     *              liste de toutes les gares d'arrivée
     * @param points
     *              nombre de points
     * @return la liste de tous les trajets possibles d'une gare à l'autre avec le nombre de points associé
     * @throws IllegalArgumentException
     *              si from est vide
     *              si to est vide
     *              si points est négatif ou nul
     */
    public static List<Trip> all(List<Station> from, List<Station> to, int points){
        Preconditions.checkArgument(!from.isEmpty() && !to.isEmpty() && points >0);
        List<Trip> all = new ArrayList<>();
        from.forEach(sFrom -> to.stream().filter(sTo -> sTo.id() != sFrom.id())
                .forEach(sTo -> all.add(new Trip(sFrom, sTo, points))));
        return all;
    }

    /**
     * Donne la station de départ du trip
     * @return La station de départ du trip
     */
    public Station from(){
        return stationFrom;
    }

    /**
     * Donne la station d'arrivée du trip
     * @return La station d'arrivée du Trip
     */
    public Station to(){
        return stationTo;
    }

    /**
     * Retourne le nombre de points associé au trip
     * @return Le nombre de points associé au trip
     */
    public int points() { return points; }

    /**
     * Calcule le nombre de points du Trip dépendemment de la connectivité du réseau du joueur
     *
     * @param connectivity Connectivité du réseau du joueur
     * @return Retourne le nombre de points positif du trip si les stations sont connectées sur le réseau du joueur
     * Sinon retourne le nombre de points négatif
     */
    public int points(StationConnectivity connectivity){
        return connectivity.connected(stationFrom, stationTo) ? points : -points;
    }
}
