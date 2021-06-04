package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;
import java.util.Objects;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public class PublicCardState {

    private final List<Card> faceUpCards;
    private final int deckSize;
    private final int discardsSize;

    /**
     * Crée un nouvel état PublicCardState à partir d'une liste de cartes (face visible), d'une taille de pioche
     * et d'une taille de défausse
     * @param faceUpCards La liste des cartes face visible
     * @param deckSize La taille de la pioche
     * @param discardsSize La taille de la défausse
     * @throws IllegalArgumentException Si la taille de du tas de cartes face visible n'est pas de 5 ou si la défausse
     * ou la pioche ont une taille négative
     */
    public PublicCardState(List<Card> faceUpCards, int deckSize, int discardsSize) {
        Preconditions.checkArgument(faceUpCards.size() == 5 && deckSize >= 0 && discardsSize >= 0);
        this.faceUpCards = List.copyOf(faceUpCards);
        this.deckSize = deckSize;
        this.discardsSize = discardsSize;
    }

    /**
     * Retourne une liste des cartes face visible
     * @return Une liste des cartes face visible
     */
    public List<Card> faceUpCards(){
        return faceUpCards;
    }

    /**
     * Retourne la carte face visible à l'index slot
     * @param slot L'index de la carte
     * @return La carte face visible de l'index slot
     * @throws IndexOutOfBoundsException Si l'index n'est pas cmpris entre 0 (inlucs) et la taille du tas de cartes
     * face visible (exclue)
     */
    public Card faceUpCard(int slot) throws IndexOutOfBoundsException {
        Objects.checkIndex(slot, faceUpCards.size());
        return faceUpCards.get(slot);

    }

    /**
     * Retourne la taille de la pioche
     * @return La taille de la pioche
     */
    public int deckSize() {
        return this.deckSize;
    }

    /**
     * Retourne vrai si et seulement si le deck est vide
     * @return Vrai si la pioche est vide, Faux sinon
     */
    public boolean isDeckEmpty() {
        return deckSize == 0;
    }

    /**
     * Retourne la taille de la défausse
     * @return La taille de la défausse
     */
    public int discardsSize() {
        return this.discardsSize;
    }



}
