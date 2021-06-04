package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Objects;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class StationPartition implements StationConnectivity {

    private final int[] representatives;

    private StationPartition(int[] representatives){
        this.representatives = representatives;
    }

    /**
     * Permet de vérifier la connexion ou non de deux stations sur le réseau d'un joueur
     * @param s1 Station de départ
     * @param s2 Station d'arrivée
     * @return vrai si les deux stations sont connectées par le jour, faux sinon
     */
    @Override
    public boolean connected(Station s1, Station s2) {
        return s1.id() >= representatives.length || s2.id() >= representatives.length ? s1.id() == s2.id() : representatives[s1.id()] == representatives[s2.id()];
    }

    /**
     * Classe bâtisseur imbriquée
     */
    public final static class Builder {
        private final int[] representatives;

        /**
         * construit un bâtisseur de partition d'un ensemble de gares dont l'identité est comprise entre 0 et la valeur en argument
         * @param stationCount id de station maximum
         * @throws IllegalArgumentException si stationCount est strictement positif
         */
        public Builder(int stationCount) {
            Preconditions.checkArgument(stationCount >= 0);
            representatives = new int[stationCount];
            int i =0;
            for(Station s : ChMap.stations().subList(0, stationCount)) {
                representatives[i] = s.id();
                ++i;
            }
        }

        /**
         * joint les sous-ensembles contenant les deux gares passées en argument, en élisant l'un des deux représentants comme représentant du sous-ensemble joint
         * @param s1 gare 1
         * @param s2 gare 2
         * @return le bâtisseur
         * @throws IndexOutOfBoundsException si l'identité des gares est hors bornes
         */
        public Builder connect(Station s1, Station s2) throws IndexOutOfBoundsException {
            Objects.checkIndex(s1.id(), representatives.length);
            Objects.checkIndex(s2.id(), representatives.length);

            int initialRepresentative = representatives[s2.id()];
            for(int i = 0; i< representatives.length; ++i){
                if(representatives[i] == initialRepresentative){
                    representatives[i] = representative(s1.id());
                }
            }
            return this;
        }

        /**
         * retourne la partition aplatie des gares correspondant à la partition profonde en cours de construction par ce bâtisseur
         * @return la partition aplatie des gares correspondant à la partition profonde en cours de construction par ce bâtisseur
         */
        public StationPartition build(){
            return new StationPartition(representatives);
        }

        private int representative(int stationId){
            return representatives[stationId];
        }

    }

}
