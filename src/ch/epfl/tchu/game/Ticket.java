package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.*;

//----- CONTIENT DU BONUS -----

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Ticket implements Comparable<Ticket> {

    private final List<Trip> trips;
    private final String textRepresentation;

    /**
     * Construit un Ticket à partir d'une liste de Trips, non vide et avec la même station de départ
     * @param trips La liste de Trips pour lesquels on veut créer un ticket
     * @throws IllegalArgumentException Si la liste est vide ou si les gares de départ n'ont pas le même nom
     */
    public Ticket(List<Trip> trips) {
        Preconditions.checkArgument(!trips.isEmpty() && sameName(trips));
        this.trips = List.copyOf(trips);
        textRepresentation = computeText();
    }

    /**
     * Construit un ticket à partir d'une seule gare de départ et d'arrivée
     * @param from Gare de départ
     * @param to Gare d'arrivée
     * @param points Nombre de points du ticket
     */
    public Ticket(Station from, Station to, int points) {
        this(Collections.singletonList(new Trip(from, to, points)));
    }

    /**
     * Donne la représentation textuelle du billet
     * @return La représentation textuelle du billet (String)
     */
    public String text(){
        return textRepresentation;
    }

    private String computeText(){
        String text;
        if(containsCountry()){
            String stationFrom = trips.get(0).from().name();
            TreeSet<String> countriesTo = new TreeSet<>();
            for(Trip trip : trips){
                countriesTo.add(trip.to().name() + " ("  + trip.points() + ")" );
            }
            String countriesToString = String.join(", ", countriesTo);
            text = String.format("%s - {%s}", stationFrom, countriesToString);
        } else {
                Trip trip = trips.get(0);
                text = String.format("%s - %s (%s)", trip.from().name(), trip.to().name(), trip.points());
            }

        return text;
    }

    /**
     * Calcule le nombre de points du ticket dépendemment de la connectivité du réseau du jouer
     * @param connectivity Connectivité du réseau du joueur
     * @return Pour un billet Ville - Pays / Pays - Pays, le nombre de points maximum s'il est connecté, ou
     * moins le nombre de points minimum s'il ne l'est pas
     * Pour un billet Ville à Ville retourne le nombre points positif s'il est connecté, sinon le nombre de points négatif
     */
    public int points(StationConnectivity connectivity) {
        if(containsCountry()){
            int maxPoints =0;
            for(Trip trip : trips){
                if(trip.points(connectivity) > maxPoints){
                    maxPoints = trip.points(connectivity);
                }
            }
            if(maxPoints == 0){
                int minPoints = Integer.MAX_VALUE;
                for(Trip trip : trips){
                    if(trip.points() < minPoints){
                        minPoints = trip.points();
                    }
                }
                return -minPoints;
            }
            else {
                return maxPoints;
            }
        } else {
            return trips.get(0).points(connectivity);
        }

    }

    /**
     * Donne la représentation textuelle du billet
     * @return La représentation textuelle du billet
     */
    @Override
    public String toString() {
        return textRepresentation;
    }

    /**
     * Compare deux tickets alphabétiquement
     * @param that Autre ticket auquel on veut comparer
     * @return Retourne un entier négatif si le billet comparant précède le billet comparé alphabétiquement
     * Retourne un entier positif si le billet comparant devance le billet comparé alphabétiquement
     * Retourne 0 si les deux sont égaux
     */
    @Override
    public int compareTo(Ticket that){
        return textRepresentation.compareTo(that.text());
    }

    // BONUS : changé en public
    public boolean containsCountry(){
        ArrayList<String> countries = new ArrayList<>(Arrays.asList("Allemagne", "France", "Autriche", "Italie"));
        return trips.stream()
                .anyMatch(trip -> countries.contains(trip.from().name()) || countries.contains(trip.to().name()));
    }

    private boolean sameName(List<Trip> trips) {
        String stationFrom = trips.get(0).from().name();
        return trips.stream()
                .allMatch(trip -> trip.from().name().equals(stationFrom));
    }

    //----------BONUS----------
    public boolean cityToCountry() {
        ArrayList<String> countries = new ArrayList<>(Arrays.asList("Allemagne", "France", "Autriche", "Italie"));
        return (trips.stream()
                .anyMatch((trip -> countries.contains(trip.to().name()))));
    }

    public List<Trip> getTrips() {
        return trips;
    }
    //-------------------------

}
