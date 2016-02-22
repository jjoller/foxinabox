package jjoller.foxinabox

import java.util.*

/**
 * Find the best option in an unknown environment
 */
public class MultiArmedBandit<T>(val options: Set<T>, val e: Int = 2) {

    private val totalReward: MutableMap<T, Double>
    private val visits: MutableMap<T, Int>
    private var totalVisits: Int

    init {
        this.totalReward = HashMap()
        this.visits = HashMap()
        this.totalVisits = 0
    }

    public fun update(option: T, outcome: Double) {
        visits.increment(option, 1)
        totalReward.increment(option, outcome)
        totalVisits++
    }

    /**
     * Get an option using the bandit formula, applying an exploration exploitation trade-off.
     */
    public fun option(greedy: Boolean = false): T {

        var max = Double.NEGATIVE_INFINITY
        var bestOption: T? = null
        for (o in options) {

            if (visits[o] == null)
                return o;

            val ni = visits[o]!!.toDouble()
            var v = totalReward[o]!! / ni

            if (!greedy)
            // apply exploration exploitation trade-off
                v = v * Math.sqrt(e * Math.log(ni) / totalVisits.toDouble())

            if (v > max) {
                max = v
                bestOption = o
            }
        }
        return bestOption!!
    }

    /**
     * Get the option with the highest average reward
     */
    public fun greedyOption() {

    }

    fun MutableMap<T, Int>.increment(key: T, i: Int) {
        var old = this[key]
        if (old == null)
            old = 0
        this[key] = old + i;
    }

    fun MutableMap<T, Double>.increment(key: T, i: Double) {
        var old = this[key]
        if (old == null)
            old = 0.0
        this[key] = old + i;
    }

}