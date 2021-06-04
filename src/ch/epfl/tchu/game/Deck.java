package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class Deck<C extends Comparable<C>> {

    private final List<C> cards;
    private Deck(List<C> cards){
        this.cards = List.copyOf(cards);
    }

    /**
     * Crée un nouveau tas de carte (Deck) ayant les mêmes cartes que le multiensemble mélangées
     * à l'aide d'un générateur de nombre aléatoires
     * @param cards L'ensemble de cartes à partir duquel créer le tas de carte
     * @param rng Le générateur de nombre aléatoires
     * @param <C> Le type de cartes
     * @return Nouveau tas de carte avec les mêmes cartes que le multiensemble, mélangé
     */
    public static <C extends Comparable<C>> Deck<C> of(SortedBag<C> cards, Random rng){
        List<C> tempCards = new ArrayList<>(cards.toList());
        Collections.shuffle(tempCards, rng);
        return new Deck<>(tempCards);
    }

    /**
     * Retourne la taille du tas de carte
     * @return La taille du tas de carte
     */
    public int size() {
        return cards.size();
    }

    /**
     * Retourne un booléen sur l'état (vide ou non) du tas de carte
     * @return Vrai si le tas de cartes est vide, Faux sinon
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Retourne la carte du sommet du tas
     * @return La carte du sommet du tas
     */
    public C topCard() {
        Preconditions.checkArgument(!cards.isEmpty());
        return cards.get(0);
    }

    /**
     * Crée un nouveau tas de cartes identique au récepteur avec les count premières cartes en moins
     * @param count Le nombre de premières cartes à enlever
     * @return Un nouveau tas de cartes identique au récepteur sans les count premières cartes
     * @throws IllegalArgumentException si le tas de carte est vide ou si le count n'est pas compris entre 0 (inclus) et la taille du taille (incluse)
     */
    public Deck<C> withoutTopCards(int count) {
        Preconditions.checkArgument(!cards.isEmpty() && count >=0 && count <= cards.size());
        return new Deck<>(cards.subList(count,cards.size()));
    }

    /**
     * Crée un nouveau tas de carte sans la première carte
     * @return Nouveau tas de carte sans la première carte
     * @throws IllegalArgumentException Si le tas de carte est vide
     */
    public Deck<C> withoutTopCard(){
        return withoutTopCards(1);
    }

    /**
     * Retourne un nouveau multiemsemble de cartes contentant les count premières cartes du tas de carte récepteur
     * @param count Les premières cartes à sélectionner
     * @return Le nouveau multiensemble de cartes avec seulement les count premières cartes
     * @throws IllegalArgumentException Si le count n'est pas compris entre 0 (inclus) et la taille du tas de carte (incluse)
     */
    public SortedBag<C> topCards(int count){
        Preconditions.checkArgument(count >=0 && count <= cards.size());
        SortedBag.Builder<C> bagBuilder = new SortedBag.Builder<>();
        cards.subList(0, count).forEach(card -> bagBuilder.add(1, card));
        return bagBuilder.build();
    }








}
