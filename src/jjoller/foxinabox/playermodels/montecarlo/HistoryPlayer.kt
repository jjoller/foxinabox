package jjoller.foxinabox.playermodels.montecarlo

import jjoller.foxinabox.PlayerModel
import jjoller.foxinabox.TexasHand

/**
 * Plays according to the history if there is a history or according to a model if there is no more history. Keeps track
 * how conform the model is to the history.
 */
class HistoryPlayer(private val model: PlayerModel, private val history: ActionHistory) : PlayerModel {

    private var hits = 0.0
    private var misses = 0.0

    override fun action(hand: TexasHand): Int {

        val modelAction = model.action(hand)
        if (history.hasNext()) {
            val historyAction = history.nextAction()
            if (historyAction == modelAction)
                hits++
            else
                misses++
            return historyAction
        } else {
            return modelAction
        }
    }

    fun conformity(): Double {
        return hits / (hits + misses)
    }
}
