package jjoller.foxinabox

import java.util.Arrays
import java.util.EnumSet

import jjoller.foxinabox.Card.CardValue

/**
 * Try all combinations of five cards and find out which has the highest
 * value.

 * @param hand
 * *            A Hand of at least five cards.
 * *
 * *
 * @param flush
 * *            If set to false do not look at the suit of the cards.
 */
public class FiveCardValue(hand: EnumSet<Card>, flush: Boolean = true) : Comparable<FiveCardValue> {

    // private EnumSet<Card> hand;
    val hand: EnumSet<Card>
    val rank: Int

    init {

        // precondition
        if (hand.size < 5)
            throw IllegalArgumentException(
                    "the Hand contains less than five Cards.")

        var bestRank = Integer.MIN_VALUE
        var bestCombination: EnumSet<Card>? = null;
        val numCards = hand.size
        var iter = hand.iterator();
        val cards = Array(numCards, { iter.next() })

        //var cards = hand.toArray()

        // array elements are sorted by index, starting with the lowest index.
        // Card[] cards = hand.toArray(new Card[numCards]);


        for (p in 0..numCards - 4 - 1) {
            for (o in p + 1..numCards - 3 - 1) {
                for (k in o + 1..numCards - 2 - 1) {
                    for (e in k + 1..numCards - 1 - 1) {
                        for (r in e + 1..numCards - 1) {

                            val c0 = cards[p]
                            val c1 = cards[o]
                            val c2 = cards[k]
                            val c3 = cards[e]
                            val c4 = cards[r]

                            val v0: Int
                            val v1: Int
                            val v2: Int
                            val v3: Int
                            val v4: Int
                            v0 = c0.value.ordinal
                            v1 = c1.value.ordinal
                            v2 = c2.value.ordinal
                            v3 = c3.value.ordinal
                            v4 = c4.value.ordinal

                            var rank = RANKING.rank[v0][v1][v2][v3][v4]

                            if (flush) {

                                if (isFlush(c0, c1, c2, c3, c4)) {
                                    if (RANKING.isStraight(v0, v1, v2, v3, v4)) {
                                        // straight flush
                                        rank = rank - RANKING.straightRank + RANKING.straightFlushRank
                                    } else {

                                        // normal flush
                                        rank = RANKING.flushRank + (rank - RANKING.minRank)
                                    }
                                }
                            }

                            if (rank > bestRank) {
                                bestRank = rank
                                bestCombination = EnumSet.of(c0, c1, c2, c3, c4)
                            }
                        }
                    }
                }
            }
        }
        this.hand = bestCombination!!
        this.rank = bestRank
    }

    val handValue: HandValueType
        get() {
            var iter = hand.iterator();
            return getHandValueType(iter.next(), iter.next(), iter.next(), iter.next(), iter.next())
        }

    /**
     * returns a positive number if this Value is better than the given
     * FiveCardValue. Returns 0 if the given value is equal this value.
     */
    override fun compareTo(o: FiveCardValue): Int {
        return rank - o.rank
    }

    fun isBetterThan(o: FiveCardValue): Boolean {
        return rank > o.rank
    }

    fun isEqual(o: FiveCardValue): Boolean {
        return rank == o.rank
    }

    override fun toString(): String {

        var s = ""

        val v = handValue

        val fiveCardValues = arrayOfNulls<CardValue>(5)
        var i = 0
        for (c in hand!!) {
            fiveCardValues[i++] = c.value
        }

        s += v

        when (v) {

            FiveCardValue.HandValueType.HIGH_CARD -> s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " " + fiveCardValues[2] + " " + fiveCardValues[3] + " " + fiveCardValues[4]

            FiveCardValue.HandValueType.PAIR -> s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " " + fiveCardValues[2] + " " + fiveCardValues[3] + " " + fiveCardValues[4]

            FiveCardValue.HandValueType.TWO_PAIR -> s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " " + fiveCardValues[2] + " " + fiveCardValues[3] + " " + fiveCardValues[4]

            FiveCardValue.HandValueType.THREE_OF_A_KIND -> s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " " + fiveCardValues[2] + " " + fiveCardValues[3] + " " + fiveCardValues[4]

            FiveCardValue.HandValueType.STRAIGHT -> if (fiveCardValues[0] === CardValue.ACE && fiveCardValues[1] === CardValue.FIVE) {
                s += " " + fiveCardValues[1] + " high"
            } else {
                s += " " + fiveCardValues[0] + " high"
            }

            FiveCardValue.HandValueType.FLUSH -> s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " " + fiveCardValues[2] + " " + fiveCardValues[3] + " " + fiveCardValues[4]

            FiveCardValue.HandValueType.FULL_HOUSE ->
                if (fiveCardValues[0] === fiveCardValues[2]) {
                    s += " " + fiveCardValues[0] + "s, full of " + fiveCardValues[3] + "s"
                } else {
                    s += " " + fiveCardValues[3] + "s, full of " + fiveCardValues[0] + "s"
                }

            FiveCardValue.HandValueType.FOUR_OF_A_KIND ->

                if (fiveCardValues[0] === fiveCardValues[1]) {
                    s += " " + fiveCardValues[0] + "s, " + fiveCardValues[4] + " kicker"
                } else {
                    s += " " + fiveCardValues[1] + "s, " + fiveCardValues[0] + " kicker"
                }

            FiveCardValue.HandValueType.STRAIGHT_FLUSH ->

                if (fiveCardValues[0] === CardValue.ACE && fiveCardValues[1] === CardValue.FIVE) {
                    s += " " + fiveCardValues[1] + " high"
                } else {
                    s += " " + fiveCardValues[0] + " high"
                }
        }

        return s
    }

    internal class HandValueRanking constructor() {

        val rank = Array(13) { Array(13) { Array(13) { Array(13) { IntArray(13) } } } }
        private var count: Int = 0
        var flushRank: Int = 0
        var straightRank: Int = 0
        var fullHouseRank: Int = 0
        var fourOfAKindRank: Int = 0
        var straightFlushRank: Int = 0
        var maxRank: Int = 0
        var minRank: Int = 0

        init {

            count = Integer.MAX_VALUE / 2

            maxRank = count + 9 // the number of different straight flushs

            straightFlushRank = count

            putFourOfAKind()

            fourOfAKindRank = count

            putFullHouse()

            fullHouseRank = count

            count -= 13 * 12 * 11 * 10 * 9

            flushRank = count
            count--

            putStraight()

            straightRank = count

            putThreeOfAKind()
            putTwoPair()
            putPair()
            putHighCard()
            minRank = count

        }


        // private void fill() {
        //
        //
        //
        // }

        private fun putFourOfAKind() {
            for (p in 0..12) {
                for (o in 0..12) {
                    if (areDifferent(p, o)) {
                        put(p, p, p, p, o, count--)
                    }
                }
            }
        }

        private fun putFullHouse() {
            for (p in 0..12) {
                for (o in 0..12) {
                    if (areDifferent(p, o)) {
                        put(p, p, p, o, o, count--)
                    }
                }
            }
        }

        private fun putStraight() {
            // straight five high
            for (p in 0..8) {
                rank[p][p + 1][p + 2][p + 3][p + 4] = count--
            }
            rank[0][9][10][11][12] = count--
        }

        private fun putThreeOfAKind() {
            for (p in 0..12) {
                for (o in 0..12) {
                    for (k in o..12) {
                        if (areDifferent(p, o, k)) {
                            put(p, p, p, o, k, count--)
                        }
                    }
                }
            }
        }

        private fun putTwoPair() {
            for (p in 0..12) {
                for (o in p..12) {
                    for (k in 0..12) {
                        if (areDifferent(p, o, k)) {
                            put(p, p, o, o, k, count--)
                        }
                    }
                }
            }
        }

        private fun putPair() {
            for (p in 0..12) {
                for (o in 0..12) {
                    for (k in o..12) {
                        for (e in k..12) {
                            if (areDifferent(p, o, k, e)) {
                                put(p, p, o, k, e, count--)
                            }
                        }
                    }
                }
            }
        }

        private fun putHighCard() {
            for (p in 0..12) {
                for (o in p + 1..12) {
                    for (k in o + 1..12) {
                        for (e in k + 1..12) {
                            for (r in e + 1..12) {
                                if (areDifferent(p, o, k, e, r) && !isStraight(p, o, k, e, r)) {
                                    put(p, o, k, e, r, count--)
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun put(p: Int, o: Int, k: Int, e: Int, r: Int, count: Int) {
            val i = IntArray(5)
            i[0] = p
            i[1] = o
            i[2] = k
            i[3] = e
            i[4] = r
            Arrays.sort(i)
            rank[i[0]][i[1]][i[2]][i[3]][i[4]] = count
        }

        private fun areDifferent(vararg a: Int): Boolean {
            val n = a.size
            for (i in 0..n - 1) {
                for (j in 0..n - 1) {
                    if (i != j) {
                        if (a[i] == a[j]) {
                            return false
                        }
                    }
                }
            }
            return true
        }

        fun isPair(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {

            val pairs = BooleanArray(10)

            pairs[0] = p == o
            pairs[1] = p == k
            pairs[2] = p == e
            pairs[3] = p == r
            pairs[4] = o == k
            pairs[5] = o == e
            pairs[6] = o == r
            pairs[7] = k == e
            pairs[8] = k == r
            pairs[9] = e == r

            var result = false
            for (b in pairs) {
                if (b) {
                    result = true
                    break
                }
            }
            return result
        }

        fun isTwoPair(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {
            val pairs = BooleanArray(10)

            pairs[0] = p == o
            pairs[1] = p == k
            pairs[2] = p == e
            pairs[3] = p == r
            pairs[4] = o == k
            pairs[5] = o == e
            pairs[6] = o == r
            pairs[7] = k == e
            pairs[8] = k == r
            pairs[9] = e == r

            var numPairs = 0
            for (b in pairs) {
                if (b) {
                    numPairs++
                }
            }
            var result = false

            if (numPairs == 2) {
                result = true
            }
            return result
        }

        fun isThreeOfAKind(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {

            var result = false
            for (i in 0..12) {
                var count = 0
                if (p == i) {
                    count++
                }
                if (o == i) {
                    count++
                }
                if (k == i) {
                    count++
                }
                if (e == i) {
                    count++
                }
                if (r == i) {
                    count++
                }
                if (count == 3) {
                    result = true
                    break
                }
            }
            return result
        }

        fun isFourOfAKind(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {
            var result = false
            for (i in 0..12) {
                var count = 0
                if (p == i) {
                    count++
                }
                if (o == i) {
                    count++
                }
                if (k == i) {
                    count++
                }
                if (e == i) {
                    count++
                }
                if (r == i) {
                    count++
                }
                if (count == 4) {
                    result = true
                    break
                }
            }
            return result
        }

        fun isFullHouse(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {
            var result: Boolean
            result = p == o && o == k && k != e && e == r
            result = result or (p == o) && o != k && k == e && e == r
            return result
        }

        fun isStraight(p: Int, o: Int, k: Int, e: Int, r: Int): Boolean {
            var isStraight = false
            if (o - p == 1 && k - o == 1 && e - k == 1 && r - e == 1) {
                isStraight = true
            } else if (p == 0 && o == 9 && k == 10 && e == 11 && r == 12) {
                isStraight = true
            }

            //
            // int[] a = new int[5];
            // a[0] = p;
            // a[1] = o;
            // a[2] = k;
            // a[3] = e;
            // a[4] = r;
            // Arrays.sort(a);
            // boolean isStraight = true;
            // for (int i = 1; i < 5; i++) {
            // if (a[i] - a[i - 1] != 1) {
            // isStraight = false;
            // break;
            // }
            // }

            // straight ace to 5
            // if (isStraight)
            // log.info("is straight: " + p + " " + o + " " + k + " " + e
            // + " " + r + " " + isStraight);

            return isStraight
        }
    }

    enum class HandValueType {

        HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH

    }

    companion object {

        private val RANKING = HandValueRanking()

        private fun getHandValueType(c0: Card, c1: Card, c2: Card,
                                     c3: Card, c4: Card): HandValueType {

            val p: Int
            val o: Int
            val k: Int
            val e: Int
            val r: Int
            p = c0.value.ordinal
            o = c1.value.ordinal
            k = c2.value.ordinal
            e = c3.value.ordinal
            r = c4.value.ordinal

            val flush = isFlush(c0, c1, c2, c3, c4)
            val isStraight = RANKING.isStraight(p, o, k, e, r)

            if (flush && isStraight)
                return HandValueType.STRAIGHT_FLUSH

            if (RANKING.isFourOfAKind(p, o, k, e, r))
                return HandValueType.FOUR_OF_A_KIND

            if (RANKING.isFullHouse(p, o, k, e, r))
                return HandValueType.FULL_HOUSE

            if (flush)
                return HandValueType.FLUSH

            if (isStraight)
                return HandValueType.STRAIGHT

            if (RANKING.isThreeOfAKind(p, o, k, e, r))
                return HandValueType.THREE_OF_A_KIND

            if (RANKING.isTwoPair(p, o, k, e, r))
                return HandValueType.TWO_PAIR

            if (RANKING.isPair(p, o, k, e, r))
                return HandValueType.PAIR

            return HandValueType.HIGH_CARD
        }

        private fun isFlush(c1: Card, c2: Card, c3: Card, c4: Card, c5: Card): Boolean {
            return c1.suit == c2.suit && c2.suit == c3.suit && c3.suit == c4.suit && c4.suit == c5.suit
        }
    }
}
