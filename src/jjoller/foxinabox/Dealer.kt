package jjoller.foxinabox

/**
 * Is responsible for dealing the cards.
 */
interface Dealer {

    fun reset()

    fun dealTableCards(hand: TexasHand)

    fun dealHoleCards(player: TexasHand.Player)

    fun smallBlind(): Int {
        return 1
    }

    fun bigBlind(): Int {
        return 2
    }
    
}

