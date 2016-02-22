package jjoller.foxinabox

import java.util.*

/**
 * Created by jost on 2/20/16.
 */
public class Odds() {

    public fun pWin(holeCards: EnumSet<Card>, tableCards: EnumSet<Card>, numOpponents: Int): Double {


        return 0.0;
    }

    private fun encode(holeCards: EnumSet<Card>, tableCards: EnumSet<Card>, numOpponents: Long): Long {

        var suitMap: MutableMap<Card.CardSuit, Card.CardSuit> = HashMap()
        val holeCardIterator = holeCards.iterator()
        val cards = ArrayList<Card>();
        cards.add(map(holeCardIterator.next(), suitMap))
        cards.add(map(holeCardIterator.next(), suitMap))

        val tableCardIterator = tableCards.iterator()
        while (tableCardIterator.hasNext())
            cards.add(map(tableCardIterator.next(), suitMap))

        var code: Long = numOpponents - 1
        for (i in cards.indices) {
            val card = cards[i];
        }
        return code
    }

    private fun map(card: Card, map: MutableMap<Card.CardSuit, Card.CardSuit>): Card {
        if (!map.containsKey(card.suit))
            map.put(card.suit, Card.CardSuit.values()[map.size])
        val suit: Card.CardSuit = map[card.suit]!!
        return Card.get(card.value, suit);
    }


}