package jjoller.foxinabox.playermodels.montecarlo

import jjoller.foxinabox.Action
import jjoller.foxinabox.PlayerModel
import jjoller.foxinabox.TexasHand

import java.util.ArrayList
import java.util.HashMap

/**
 * Player who builds a monte carlo exploration tree
 */
class LimitBanditPlayer(val actionHistory: ActionHistory) : PlayerModel {

    private val stats = HashMap<String, PlannerStatistics>()
    private val toUpdateHistories = ArrayList<String>()

    override fun action(hand: TexasHand): Int {

        if (actionHistory.hasNext()) {
            // the player can not change the past, play according to the history
            return actionHistory.nextAction()
        } else {
            // optimize the next action

            val history = hand.actionIdentifier()

            var selectedAction = Integer.MAX_VALUE

            val actions = possibleActions(hand)
            var selectedHistory: String? = null
            var totalVisits = 0
            for (a in actions) {
                val childHistory = history + a + "_"
                if (stats.containsKey(childHistory)) {
                    totalVisits += stats[childHistory]!!.visits().toInt()
                } else {
                    // If we haven't explored all possible actions, choose one uniformly at
                    // random from the not yet explored actions.
                    selectedAction = a
                    selectedHistory = history + a + "_"
                }
            }

            if (selectedHistory == null) {
                var maxValue = java.lang.Double.NEGATIVE_INFINITY

                for (action in actions) {
                    // there is no point in exploring the fold action multiple times
                    // because the outcomes is the same every time
                    if (action >= 0) {
                        val childHistory = history + action + "_"

                        val childNode = stats[childHistory]

                        if (childNode == null) {
                            // unexplored node
                            toUpdateHistories.add(childHistory)
                            return action
                        } else {
                            // the node has been explored before

                            // Bandit formula
                            val value = childNode.expectation() + explorationRatio * Math.sqrt(Math.log(totalVisits.toDouble()) / childNode.visits())

                            if (value > maxValue) {
                                maxValue = value
                                selectedAction = action
                                selectedHistory = history + action + "_"
                            }
                        }
                    }
                }
            }
            toUpdateHistories.add(selectedHistory!!)
            return selectedAction
        }
    }

    private fun possibleActions(hand: TexasHand): List<Int> {

        val callAmount = hand.callAmount()
        val raiseAmount = hand.raiseAmount()
        val actions = ArrayList<Int>(3)
        actions.add(callAmount)
        if (raiseAmount > callAmount)
            actions.add(raiseAmount)
        if (callAmount > 0)
            actions.add(Action.FOLD)
        return actions
    }

    fun bestAction(hand: TexasHand): Int {

        val history = hand.actionIdentifier()
        var maxValue = java.lang.Double.NEGATIVE_INFINITY
        val possibleActions = possibleActions(hand)
        var bestAction = possibleActions[0]
        for (action in possibleActions) {
            val childHistory = history + action + "_"
            val stats = this.stats[childHistory]
            if (stats != null) {
                if (maxValue < stats.expectation()) {
                    maxValue = stats.expectation()
                    bestAction = action
                }
            } else {
                throw IllegalStateException(childHistory)
            }
        }
        System.out.println(hand)
        println("BEST ACTION: $bestAction e: $maxValue")
        return bestAction
    }

    /**
     * Usually this is called after the hand is completed.

     * @param outcome The value to modify the statistics with.
     */
    fun update(outcome: Double) {

        for (history in toUpdateHistories) {
            if (!stats.containsKey(history))
                stats.put(history, PlannerStatistics())
            stats[history]!!.update(outcome)
        }
        toUpdateHistories.clear()
    }

    internal inner class PlannerStatistics {

        private var expectation = 0.0
        private var visits = 0.0

        fun expectation(): Double {
            return expectation / visits
        }

        fun visits(): Double {
            return visits
        }

        fun update(outcome: Double) {
            expectation += outcome
            visits++
        }
    }

    override fun toString(): String {

        var s = ""
        for (e in this.stats.entries)
            s += e.key + " e: " + e.value.expectation() + ", visits: " + e.value.visits() + "\n"

        return s
    }

    companion object {

        private val explorationRatio = Math.sqrt(2.0)
    }

}
