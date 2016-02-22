package jjoller.foxinabox.playermodels.montecarlo

import jjoller.foxinabox.*
import java.util.logging.Logger

import java.util.ArrayList
import java.util.HashMap

/**
 * Perform random samplings on a hand to approximate the optimal action.
 */
class TexasMonteCarloPlanner(val opponentModel: PlayerModel, val planningTime: Int = 10, val minSampleRuns: Int = 100) : PlayerModel {

    override fun action(hand: TexasHand): Int {

        val player = hand.onTurn!!

        //        log.info("GET ACTION for player " + player.name)

        val explorer = LimitBanditPlayer(ActionHistory(hand))
        val job = MonteCarloJob(player, hand, explorer, player.name, opponentModel, planningTime, minSampleRuns)
        job.run()


        //        log.info(hand.toString() + "")

        //log.info(opponentModel + "");

        val bestAction = explorer.bestAction(hand)
        log.info(hand.toString());
        log.info("====== best action: " + bestAction + ", samples: " + job.sampleCount + " ======")

        return bestAction
    }

    override fun toString(): String {
        return "TexasMonteCarloPlanner"
    }

    companion object {

        private val log = Logger.getLogger(TexasMonteCarloPlanner::class.java!!.name)
    }
}

internal class MonteCarloJob(private val hero: TexasHand.Player, private val hand: TexasHand, private val explorer: LimitBanditPlayer, private val heroName: String,
                             private val opponentModel: PlayerModel, private val planningTime: Int,
                             private val minSampleRuns: Int) : Runnable {
    var sampleCount = 0
        private set

    override fun run() {

        // dealer used for experiments
        val dealer = ConformityDealer(hero, hand)

        // create the players used in the experiment
        val players = ArrayList<TexasHand.Player>()

        // keep a map of player to model
        val opponentModels = HashMap<TexasHand.Player, HistoryPlayer>()

        var hero: TexasHand.Player? = null

        for (player in hand.players) {
            if (player.name.equals(heroName)) {
                // this is the hero player which tries to outsmart the opponents
                hero = TexasHand.Player(heroName, explorer, player.stack)
                players.add(hero)
            } else {
                // the opponents are modelled as history players to reenact the past but also approximate the future
                // using the given model
                val historyPlayer = HistoryPlayer(opponentModel, explorer.actionHistory)
                val p = TexasHand.Player(player.name, historyPlayer, player.stack)
                opponentModels.put(p, historyPlayer)
                players.add(p)
            }
        }

        // used to normalize the reward
        val maxPay = dealer.bigBlind() * 24
        val maxWin = maxPay * (hand.players.size - 1)

        // run the experiments
        val startTime = System.currentTimeMillis()
        while (sampleCount < minSampleRuns || System.currentTimeMillis() - startTime < planningTime) {

            // run an experiment
            val hand = LimitTexasHand(dealer, players)

            // inform the planner about the outcome (we're only interested in how much the hero won or lost)
            explorer.update((maxPay + hand.outcome(hero!!)) / maxWin)

            // inform the conformity dealer about the conformity of the cards
            for (e in opponentModels.entries)
                dealer.update(e.key, e.value.conformity())

            // prepare for the next sample run
            dealer.reset()
            explorer.actionHistory.reset()

            // increase the sample count
            sampleCount++

        }
    }
}
