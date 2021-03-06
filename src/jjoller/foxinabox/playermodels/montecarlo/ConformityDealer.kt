package jjoller.foxinabox.playermodels.montecarlo

import java.util.*

import jjoller.foxinabox.Card
import jjoller.foxinabox.Dealer
import jjoller.foxinabox.Phase
import jjoller.foxinabox.TexasHand

/**
 * Deals cards which are more likely.
 */
class ConformityDealer : Dealer {

    constructor(hero: TexasHand.Player, hand: TexasHand) {
        heroName = hero.name
        heroHoldings = hero.holdings().get()
        tableCards = hand.tableCards()
        deck = ArrayList<Card>()
        deck.addAll(Card.values())
        deck.removeAll(heroHoldings)
        deck.removeAll(tableCards)
        removed = ArrayList<Card>()
        tableCardsIter = tableCards.iterator()
    }

    private val heroName: String
    private val heroHoldings: EnumSet<Card>
    private val tableCards: List<Card>
    private var tableCardsIter: Iterator<Card>
    private val deck: MutableList<Card>
    private val removed: MutableList<Card>
    private val random = Random()

    override fun reset() {

        deck.addAll(removed)
        removed.clear()
        tableCardsIter = tableCards.iterator()
    }

    override fun dealTableCards(hand: TexasHand) {

        when (hand.phase()) {
            Phase.PRE_FLOP -> hand.flop = EnumSet.of(tableCard(), tableCard(), tableCard())
            Phase.FLOP -> hand.turn = tableCard()
            Phase.TURN -> hand.river = tableCard()
            else -> throw IllegalStateException()
        }
    }

    override fun dealHoleCards(player: TexasHand.Player) {

        if (player.name === heroName)
            player.setCards(heroHoldings)
        else

        // TODO select cards according to a Bandit model to choose cards which are more conform with the player model

            player.setCards(EnumSet.of(removeRandomFromDeck(), removeRandomFromDeck()))
        //     println("set cards of " + player.name + " to " + player.holdings())
    }

    fun update(player: TexasHand.Player, conformity: Double) {
        // TODO
    }

    private fun tableCard(): Card {
        if (tableCardsIter.hasNext())
            return tableCardsIter.next()
        else
            return removeRandomFromDeck()
    }

    private fun removeRandomFromDeck(): Card {
        val card = deck.removeAt(random.nextInt(deck.size))
        removed.add(card)
        return card
    }
}
