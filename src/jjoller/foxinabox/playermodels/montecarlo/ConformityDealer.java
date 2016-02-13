package jjoller.foxinabox.playermodels.montecarlo;

import jjoller.foxinabox.Card;
import jjoller.foxinabox.Dealer;
import jjoller.foxinabox.TexasHand;

import java.util.*;

/**
 * Deals cards which are more likely.
 */
public class ConformityDealer implements Dealer {

    public ConformityDealer(TexasHand hand) {
        tableCards = new ArrayList<>(5);
        if (hand.getFlop().isPresent()) {
            tableCards.addAll(hand.getFlop().get());
            if (hand.getTurn().isPresent()) {
                tableCards.add(hand.getTurn().get());
                if (hand.getRiver().isPresent())
                    tableCards.add(hand.getRiver().get());
            }
        }
        deck = new ArrayList<>();
        deck.addAll(Arrays.asList(Card.values()));
        deck.removeAll(tableCards);
        removed = new ArrayList<>();
        this.tableCardsIter = tableCards.iterator();
    }

    private final List<Card> tableCards;
    private Iterator<Card> tableCardsIter;
    private final List<Card> deck;
    private final List<Card> removed;
    private final Random random = new Random();

    @Override
    public void reset() {
        deck.addAll(removed);
        removed.clear();
        tableCardsIter = tableCards.iterator();
    }

    @Override
    public void dealTableCards(TexasHand hand) {
        switch (hand.phase()) {
            case PRE_FLOP:
                hand.setFlop(EnumSet.of(tableCard(), tableCard(), tableCard()));
                break;
            case FLOP:
                hand.setTurn(tableCard());
                break;
            case TURN:
                hand.setRiver(tableCard());
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void dealHoleCards(TexasHand.Player player) {

        // TODO select cards according to a Bandit model to choose cards which are more conform with the player model

        player.setCards(EnumSet.of(removeRandomFromDeck(), removeRandomFromDeck()));
    }

    public void update(TexasHand.Player player, double conformity) {
        // TODO
    }

    private Card tableCard() {
        if (tableCardsIter.hasNext())
            return tableCardsIter.next();
        else
            return removeRandomFromDeck();
    }

    private Card removeRandomFromDeck() {
        Card card = deck.remove(random.nextInt(deck.size()));
        removed.add(card);
        return card;
    }
}
