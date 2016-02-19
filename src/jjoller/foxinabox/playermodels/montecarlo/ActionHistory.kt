package jjoller.foxinabox.playermodels.montecarlo

import jjoller.foxinabox.TexasHand
import java.util.*

/**
 * Representation of an action history
 */
class ActionHistory(hand: TexasHand) {

    private val actions: MutableList<Int>
    private var iterator: Iterator<Int>

    init {

        actions = ArrayList<Int>()
        actions.addAll(hand.actions.subList(2, hand.actions.size))

        // remove dealer actions
        actions.removeIf { a -> a < -1 }
        iterator = actions.iterator()
    }

    operator fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    fun nextAction(): Int {
        return iterator.next()
    }

    fun reset() {
        iterator = actions.iterator()
    }

}
