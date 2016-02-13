package jjoller.foxinabox;

import java.util.*;

/**
 * Is responsible for dealing the cards.
 */
public interface Dealer {

    void reset();

    void dealTableCards(TexasHand hand);

    void dealHoleCards(TexasHand.Player player);

    default int smallBlind() {
        return 1;
    }

    default int bigBlind() {
        return 2;
    }

}

