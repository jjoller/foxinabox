package jjoller.foxinabox.playermodels.montecarlo

import jjoller.foxinabox.Action
import jjoller.foxinabox.Phase
import jjoller.foxinabox.PlayerModel
import jjoller.foxinabox.TexasHand
import java.util.*

/**
 * Player who builds a monte carlo exploration tree
 */
class LimitBanditPlayer(val actionHistory: ActionHistory, val explorationTradeOff: Int = 2) : PlayerModel {

    private val stats = HashMap<String, PlannerStatistics>()
    private val toUpdateHistories = ArrayList<String>()
    private var foldExplored = false;
    private var numPlayers = 3
    private var bigBlind = 2;


    override fun action(hand: TexasHand): Int {

        this.numPlayers = hand.players.size
        this.bigBlind = hand.dealer.bigBlind()

        if (actionHistory.hasNext()) {
            // the player can not change the past, play according to the history
            return actionHistory.nextAction()
        } else {
            // optimize the next action

            val history = hand.actionIdentifier()
            val actions = actionsToExplore(hand)
            var totalVisits = 0
            var minExp = Double.MAX_VALUE
            var selectedAction = Integer.MAX_VALUE
            var selectedHistory: String? = null
            for (a in actions) {
                val childHistory = history + a + "_"
                if (stats.containsKey(childHistory)) {
                    val stat = stats[childHistory]!!
                    totalVisits += stat.visits().toInt()
                    minExp = Math.min(minExp, stat.expectation())
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
                            throw IllegalStateException(childHistory)
                        } else {
                            // the node has been explored before

                            // Bandit formula
                            val value = childNode.expectation() + Math.sqrt(explorationTradeOff * Math.log(totalVisits.toDouble()) / childNode.visits())
                            //println("a " + action + ", value: " + value + " n " + totalVisits + " ni " + childNode.visits())
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

            if (selectedAction == Action.FOLD)
                foldExplored = true

            return selectedAction
        }
    }

    private fun actionsToExplore(hand: TexasHand): MutableSet<Int> {

        val callAmount = hand.callAmount()
        val actions = HashSet<Int>()
        actions.add(callAmount)
        actions.add(hand.raiseAmount())
        if (!foldExplored && callAmount > 0)
            actions.add(Action.FOLD)
        return actions
    }

    fun bestAction(hand: TexasHand): Int {

        val history = hand.actionIdentifier()
        var maxValue = java.lang.Double.NEGATIVE_INFINITY
        val possibleActions = actionsToExplore(hand)
        if (!possibleActions.contains(Action.CHECK))
            possibleActions.add(Action.FOLD)
        var bestAction = 0;
        for (action in possibleActions) {
            val childHistory = history + action + "_"
            val stats = this.stats[childHistory]
            if (stats != null) {
                var expectation = stats.expectation()
                //                if (action == Action.FOLD && expectation >= 0.0) {
                //                    // player looses money by folding even he is not on the blind
                //                    expectation -= (hand.dealer.bigBlind() + hand.dealer.smallBlind()) / hand.players.size
                //                }
                if (hand.phase() == Phase.PRE_FLOP && hand.actions.size <= 5)
                    println("exp of " + action + ": " + expectation + " n " + stats.visits())
                if (maxValue < expectation) {
                    maxValue = expectation
                    bestAction = action
                }
            } else {
                throw IllegalStateException(childHistory)
            }
        }
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

}
