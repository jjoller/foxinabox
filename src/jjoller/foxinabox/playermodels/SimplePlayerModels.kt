package jjoller.foxinabox.playermodels

import jjoller.foxinabox.Action
import jjoller.foxinabox.PlayerModel
import jjoller.foxinabox.TexasHand

import java.util.Random

/**
 * Created by jost on 2/7/16.
 */
class SimplePlayerModels {

    class RandomizedPlayer : PlayerModel {

        private val random = Random()
        private val pFold = 0.1
        private val pCall = 0.6

        override fun action(hand: TexasHand): Int {
            val r = random.nextDouble()
            val call = hand.callAmount()
            if (r < pFold && call > 0)
            // it is always possible to fold if you have to pay something
                return Action.FOLD
            else if (r < pCall + pFold)
                return call
            else
                return hand.raiseAmount()
        }
    }

    class Aggressor : PlayerModel {
        override fun action(hand: TexasHand): Int {
            return hand.raiseAmount()
        }
    }

    class Caller : PlayerModel {
        override fun action(hand: TexasHand): Int {
            return hand.callAmount()
        }
    }

}
