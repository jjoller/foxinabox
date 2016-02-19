package jjoller.foxinabox

/**
 * Helper class for Action encoding
 */
object Action {

    val FOLD = -1
    val CHECK = 0
    val DEAL_PREFLOP = -2
    val DEAL_FLOP = -3
    val DEAL_TURN = -4
    val DEAL_RIVER = -5

    fun toString(action: Int): String {

        when (action) {
            FOLD -> return "f"
            DEAL_PREFLOP -> return ""
            DEAL_FLOP -> return "F"
            DEAL_TURN -> return "T"
            DEAL_RIVER -> return "R"
            else -> return action.toString();
        }
    }
}
