package jjoller.foxinabox

import java.util.*

/**
 * Is responsible for dealing the cards. This implementation deals random cards.
 */
class RandomDealer : Dealer {

    private val deck: MutableList<Card>
    private val removed: MutableList<Card>
    private val random: Random

    init {
        deck = ArrayList<Card>(Card.values().size)
        deck.addAll(Arrays.asList(*Card.values()))
        removed = ArrayList<Card>(25)
        random = Random()
    }

    override fun reset() {
        deck.addAll(removed)
        removed.clear()
    }

    override fun dealTableCards(hand: TexasHand) {

        when (hand.phase()) {
            Phase.PRE_FLOP -> {
                hand.flop = EnumSet.of(removeRandomFromDeck(),
                        removeRandomFromDeck(), removeRandomFromDeck())
            }
            Phase.FLOP -> hand.turn = removeRandomFromDeck()
            Phase.TURN -> hand.river = removeRandomFromDeck()
            else -> throw IllegalStateException()
        }
    }

    override fun dealHoleCards(player: TexasHand.Player) {

        player.setCards(EnumSet.of(removeRandomFromDeck(),
                removeRandomFromDeck()))
    }

    private fun removeRandomFromDeck(): Card {
        val card = deck.removeAt(random.nextInt(deck.size))
        removed.add(card)
        return card
    }

    override fun smallBlind(): Int {
        return 1
    }

    override fun bigBlind(): Int {
        return 2
    }

}

