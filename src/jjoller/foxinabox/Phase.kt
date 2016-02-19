package jjoller.foxinabox

/**
 * Phases of a poker hand
 */
public enum class Phase private constructor(val action: Int) {

    PRE_FLOP(-2), FLOP(-3), TURN(-4), RIVER(-5)
}
