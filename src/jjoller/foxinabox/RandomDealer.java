package jjoller.foxinabox;

import java.util.*;

/**
 * Is responsible for dealing the cards. This implementation deals random cards.
 */
public class RandomDealer implements Dealer {

    public RandomDealer() {
        deck = new ArrayList<>(Card.values().length);
        deck.addAll(Arrays.asList(Card.values()));
        removed = new ArrayList<>(25);
        random = new Random();
    }


    private final List<Card> deck;
    private final List<Card> removed;
    private final Random random;

    public void reset() {
        deck.addAll(removed);
        removed.clear();
    }

    public void dealTableCards(TexasHand hand) {

        switch (hand.phase()) {
            case PRE_FLOP:
                EnumSet<Card> flop = EnumSet.of(removeRandomFromDeck(),
                        removeRandomFromDeck(), removeRandomFromDeck());
                hand.setFlop(flop);
                break;
            case FLOP:
                hand.setTurn(removeRandomFromDeck());
                break;
            case TURN:
                hand.setRiver(removeRandomFromDeck());
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void dealHoleCards(TexasHand.Player player) {

        player.setCards(EnumSet.of(removeRandomFromDeck(),
                removeRandomFromDeck()));
    }

    private Card removeRandomFromDeck() {
        Card card = deck.remove(random.nextInt(deck.size()));
        removed.add(card);
        return card;
    }

    public int smallBlind() {
        return 1;
    }

    public int bigBlind() {
        return 2;
    }

}

