package jjoller.foxinabox

import jjoller.foxinabox.playermodels.SimplePlayerModels
import jjoller.foxinabox.playermodels.montecarlo.TexasMonteCarloPlanner
import java.util.ArrayList

fun main(args: Array<String>) {

    val model = SimplePlayerModels.Aggressor()
    val smart = TexasMonteCarloPlanner(model, 1, 1000)

    val stack = 1000
    val players = ArrayList<TexasHand.Player>()

    players.add(TexasHand.Player("John", model, stack))
    players.add(TexasHand.Player("Ringo", smart, stack))
    players.add(TexasHand.Player("George", model, stack));
    //        players.add(new TexasHand.Player("John", model, stack));
    //        players.add(new TexasHand.Player("Ringo", model, stack));
    //        players.add(new TexasHand.Player("George", model, stack));
    //        players.add(new TexasHand.Player("John", model, stack));
    //        players.add(new TexasHand.Player("Ringo", model, stack));
    //        players.add(new TexasHand.Player("George", model, stack));

    var hand: TexasHand = LimitTexasHand(RandomDealer(), players)

    val t = System.currentTimeMillis()
    var count = 0
    val duration: Long = 20000
    while (System.currentTimeMillis() - t < duration) {
        count++
        val oldHand = hand
        hand = hand.payOutPot()
        System.out.println(oldHand.toString())
    }

    println("played " + count + " hands in "
            + (System.currentTimeMillis() - t) + " ms")
}

