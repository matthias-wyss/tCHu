package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 */
public final class CardState extends PublicCardState {

    private final Deck<Card> deck;
    private final SortedBag<Card> discards;
    private CardState(List<Card> faceUpCards, Deck<Card> deck, SortedBag<Card> discards){
        super(faceUpCards, deck.size(), discards.size());
        this.deck = deck;
        this.discards = discards;

    }

    /**
     * Crée un nouvel état CardState privé à partir d'un tas de de cartes.
     * Les 5 cartes disposées face visibles sont les 5 premières du tas de cartes passé
     * La pioche est le reste des cartes et la défausse et vide
     * @param deck Tas de carte pour l'état
     * @return Nouvel état CardState
     * @throws IllegalArgumentException Si le tas de carte contient moins de 5 cartes
     */
    public static CardState of(Deck<Card> deck){
        Preconditions.checkArgument(deck.size() >= 5);
        return new CardState(deck.topCards(5).toList(), deck.withoutTopCards(5), SortedBag.of());
    }

    /**
     * Crée un nouvel état CardState identique au récepteur avec comme carte visible d'index slot (0 =< slot < 5)
     * remplacée par celle au sommet de la pioche du récepteur
     * @param slot Index de la carte à remplacer
     * @return Nouvel état CardState avec la carte Slot modifiée
     * @throws IndexOutOfBoundsException Si slot n'est pas compris entre 0 (inclus) et 5 (exclus)
     * @throws IllegalArgumentException Si la pioche est vide
     */
    public CardState withDrawnFaceUpCard(int slot) throws IndexOutOfBoundsException {
        Objects.checkIndex(slot, Constants.FACE_UP_CARDS_COUNT);
        Preconditions.checkArgument(!this.deck.isEmpty());
        List<Card> faceUpCards = new ArrayList<>(super.faceUpCards());
        faceUpCards.set(slot, deck.topCard());
        return new CardState(faceUpCards, deck.withoutTopCard(), discards);
    }

    /**
     * Retourne la carte au sommet de la pioche
     * @return La carte au sommet de la pioche
     * @throws IllegalArgumentException Si la pioche est vide
     */
    public Card topDeckCard() {
        Preconditions.checkArgument(!deck.isEmpty());
        return deck.topCard();
    }

    /**
     * Crée un nouvel ensemble identique au récepteur sans la carte au sommet de la pioche
     * @return Nouvel ensemble identique sans la carte du sommet de la pioche
     * @throws IllegalArgumentException Si la pioche est vide
     */
    public CardState withoutTopDeckCard() {
        Preconditions.checkArgument(!deck.isEmpty());
        return new CardState(this.faceUpCards(), this.deck.withoutTopCard(), this.discards);
    }

    /**
     * Crée un nouvel ensemble de cartes identique au récepteur avec les cartes de la défausse mélangées
     * à l'aide d'un générateur de nombre aléatoires
     * @param rng Le générateur de nombre aléatoires
     * @return Le nouvel ensemble de cartes identiques avec la défausse mélangée
     * @throws IllegalArgumentException Si la pioche n'est pas vide
     */
    public CardState withDeckRecreatedFromDiscards(Random rng) {
        Preconditions.checkArgument(deck.isEmpty());
        return new CardState(faceUpCards(), Deck.of(discards, rng), SortedBag.of());
    }

    /**
     * Crée un nouvel ensemble de cartes identique au récepteur avec les cartes en argument ajoutées à la défausse
     * @param additionalDiscards cartes additionneles
     * @return un nouvel ensemble de cartes identique au récepteur avec les cartes en argument ajoutées à la défausse
     */
    public CardState withMoreDiscardedCards(SortedBag<Card> additionalDiscards){
        return new CardState(super.faceUpCards(), deck, discards.union(additionalDiscards));
    }



}
