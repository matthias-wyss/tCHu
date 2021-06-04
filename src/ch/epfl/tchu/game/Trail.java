package ch.epfl.tchu.game;

import ch.epfl.tchu.gui.StringsFr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Trail {

    private final int length;

    private final List<Route> routesList;
    private final Station station1;
    private final Station station2;
    private Trail(List<Route> routesList, Station station1, Station station2) {
        this.routesList = routesList;
        this.length = computeLength();
        this.station1 = station1;
        this.station2 = station2;
    }

    /**
     * retourne le plus long chemin du réseau constitué des routes données
     *
     * @param routes liste de routes
     * @return le plus long chemin du réseau constitué des routes données
     */
    public static Trail longest(List<Route> routes){
        if(routes.isEmpty()){
            return new Trail(List.of(), null, null);
        }
        List<Route> routesCopy = new ArrayList<>(routes);
        List<Trail> trailsList = new ArrayList<>();

        routesCopy.forEach(route -> {
            trailsList.add(new Trail(Collections.singletonList(route), route.station1(), route.station2()));
            trailsList.add(new Trail(Collections.singletonList(route), route.station2(), route.station1()));
        });

        boolean continuationFound;
        List<Trail> finishedTrails = new ArrayList<>();

        do {
            List<Trail> tempTrailsList = new ArrayList<>();

            for(Trail trail : trailsList){
                List<Trail> continuedTrails = findContinuation(trail, routesCopy);

                if(continuedTrails.size() == 1 && continuedTrails.contains(trail)){
                    finishedTrails.add(trail);
                }
                else {
                    tempTrailsList.addAll(continuedTrails);
                }

            }
            if(tempTrailsList.isEmpty()){
                continuationFound = false;
            } else {
                continuationFound = true;
                trailsList.clear();
                trailsList.addAll(tempTrailsList);
            }

        } while(continuationFound);


        int biggestLength =0;
        Trail longestTrail = null;
        for(Trail trail : finishedTrails){
            if(trail.length > biggestLength){
                longestTrail = trail;
                biggestLength = trail.length;
            }
        }
        return longestTrail;
    }

    private static List<Trail> findContinuation(Trail trail, List<Route> possibleRoutes){
        List<Trail> continuedTrails = new ArrayList<>();
        if(trail == null || trail.station1() == null || trail.station2() == null){
            return List.of();
        } else {
            for(Route route : possibleRoutes){

                if((trail.station2().id() == route.station1().id() || trail.station2().id() == route.station2().id()) && (!(trail.routesList.contains(route)))){

                    List<Route> continuationRoutes = new ArrayList<>(trail.routesList);
                    continuationRoutes.add(route);
                    if(trail.station2().id() == route.station1().id()){
                        continuedTrails.add(new Trail(continuationRoutes, trail.station1(), route.station2()));
                    }
                    if(trail.station2().id() == route.station2().id()){
                        continuedTrails.add(new Trail(continuationRoutes, trail.station1(), route.station1()));
                    }
                }
            }
            if(continuedTrails.isEmpty()){
                continuedTrails.add(trail);
            }
            return continuedTrails;
        }

    }






    private int computeLength() {
        int length=0;
        for(Route route : routesList){
            length+= route.length();
        }
        return length;
    }

    /**
     * retourne la longueur du chemin
     *
     * @return la longueur du chemin
     */
    public int length(){
        return length;
    }

    /**
     * retourne la première gare du chemin ou null si le chemin est de longueur 0
     *
     * @return la première gare du chemin ou null si le chemin est de longueur 0
     */
    public Station station1() {
        return length !=0 ? station1 : null;
    }

    /**
     * retourne la deuxième gare du chemin ou null si le chemin est de longueur 0
     *
     * @return la deuxième gare du chemin ou null si le chemin est de longueur 0
     */
    public Station station2() {
        return length !=0 ? station2 : null;
    }

    /**
     * retourne la représentation textuelle du chemin sous la forme
     *         Nom_Station_1 - Nom_Station_2 (longueur)
     *
     * @return la représentation textuelle du chemin
     */
    @Override
    public String toString() {
        return String.format("%s%s%s (%s)", station1.name(), StringsFr.EN_DASH_SEPARATOR,  station2.name(), length);
    }

}
