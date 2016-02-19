package jjoller.foxinabox

import java.util.ArrayList
import java.util.EnumSet
import java.util.HashSet
import java.util.LinkedList
import java.util.Optional

/**
 * Implementation of a texas hold'em game.
 */
abstract class TexasHand {

    protected val dealer: Dealer
    public val players: MutableList<Player>
    protected val activePlayers: LinkedList<Player>
    public val actions: MutableList<Int>

    // amount each player paid
    protected val paid: IntArray

    // preflop, flop, turn or river
    protected var phase: Phase

    // amount which a player has to pay to stay active
    protected var toPay: Int = 0

    // toPay at the end of the previous phase
    protected var toPayPreviousPhase: Int = 0

    // Number of minimal required player actions which are required to complete
    // the phase.
    protected var minPlayerActions: Int = 0

    // iterate over the active players
    protected var onTurnIterator: MutableListIterator<Player>

    // player currently on turn
    public var onTurn: Player? = null
        private set

    var flop: EnumSet<Card>?
    var turn: Card?
    var river: Card?

    constructor(dealer: Dealer, players: MutableList<Player>) {
        assert(players.size > 1) { "A poker game consists of at least 2 players" }

        dealer.reset()
        this.dealer = dealer
        this.players = players
        this.activePlayers = LinkedList<Player>()
        activePlayers.addAll(players)
        this.actions = ArrayList<Int>(this.players.size * 8)
        flop = null
        turn = null
        river = null
        paid = IntArray(players.size)
        phase = Phase.PRE_FLOP
        toPay = 0
        toPayPreviousPhase = 0
        minPlayerActions = this.players.size

        // deal cards
        players.forEach { p -> dealer.dealHoleCards(p) }

        // play blinds
        performPlayerAction(players[0], dealer.smallBlind())
        performPlayerAction(players[1], dealer.bigBlind())

        onTurnIterator = this.activePlayers.listIterator(if (activePlayers.size > 2) 2 else 0)

        for (phase in PHASES) {
            performPhase()
            if (this.activePlayers.size < 2) {
                return
            } else {
                actions.add(phase.action)
                dealer.dealTableCards(this)
                this.toPayPreviousPhase = this.toPay
            }
            this.phase = phase
        }
        performPhase()
    }

    public fun tableCards(): List<Card> {
        val tableCards = ArrayList<Card>(5);
        if (flop != null) {
            tableCards.addAll(flop!!)
            if (turn != null) {
                tableCards.add(turn!!)
                if (river != null)
                    tableCards.add(river!!)
            }
        }
        return tableCards
    }

    private fun performPhase() {

        // every player has to perform at least 1 action
        minPlayerActions = this.activePlayers.size
        moveToNextPlayer()
        do {
            val action = onTurn!!.model.action(this)
            performPlayerAction(onTurn!!, action)
            moveToNextPlayer()
        } while (onTurn != null)
    }

    private fun performPlayerAction(player: Player, action: Int) {

        minPlayerActions--
        actions.add(action)
        val seat = seat(player)
        if (action == Action.FOLD) {
            onTurnIterator.remove()
        } else {
            paid[seat] += action
            if (paid[seat] >= player.stack)
            // is all-in
                onTurnIterator.remove()
        }

        toPay = Math.max(toPay, paid[seat])
    }

    protected fun toPay(): Int {
        return toPay
    }

    protected fun toPayPreviousPhase(): Int {
        return toPayPreviousPhase
    }


    public fun outcome(player: Player): Double {
        assert(this.isComplete) { "Hand must be complete" }

        val paid = this.paid(player)
        if (this.activePlayers.contains(player)) {

            var isWinner = false
            var winners = 0.0

            if (this.activePlayers.size < 2) {
                // player is the only one left
                isWinner = true
                winners = 1.0
            } else {

                // has not folded
                var bestVal: FiveCardValue? = null

                for (p in this.activePlayers) {
                    val cards = player.holdings().get()
                    this.addTableCards(cards)
                    val value = FiveCardValue(cards)
                    if (bestVal == null || value.isBetterThan(bestVal)) {
                        winners = 1.0
                        bestVal = value
                        isWinner = p === player
                    } else if (value.isEqual(bestVal)) {
                        winners++
                    }
                }
            }

            if (isWinner)
                return this.pot() / winners - paid
            else
                return paid.toDouble()

        } else {
            // has folded
            return (-paid).toDouble()
        }
    }

    fun payOutPot(): TexasHand {
        assert(this.isComplete) { "Hand must be complete when paying out the pot" }

        val winners: MutableCollection<Player>
        var bestVal: FiveCardValue? = null

        if (this.activePlayers.size > 1) {
            winners = HashSet<Player>()
            for (player in this.activePlayers) {
                val cards = player.holdings().get()
                this.addTableCards(cards)
                val `val` = FiveCardValue(cards)
                if (bestVal == null || `val`.isBetterThan(bestVal)) {
                    winners.clear()
                    winners.add(player)
                    bestVal = `val`
                } else if (`val`.isEqual(bestVal)) {
                    winners.add(player)
                }
            }
        } else {
            winners = this.activePlayers
        }

        val pot = this.pot()
        for (player in players) {
            var newStack = player.stack - this.paid(player)
            if (winners.contains(player))
                newStack += pot / winners.size
            player.stack = newStack
        }

        // move dealer button
        val smallBlind = players.removeAt(0)
        players.add(smallBlind)
        return follower(dealer, players)
    }

    /**
     * Helper method to add the table cards

     * @param cards
     */
    private fun addTableCards(cards: EnumSet<Card>) {
        if (flop != null) {
            cards.addAll(flop!!)
            if (turn != null) {
                cards.add(turn!!)
                if (river != null)
                    cards.add(river!!)
            }
        }
    }

    protected abstract fun follower(dealer: Dealer, players: MutableList<Player>): TexasHand

    /**
     * All the player except 1 folded or showdown

     * @return true if the hand is
     */
    val isComplete: Boolean
        get() = this.activePlayers.size < 2 || phase() === Phase.RIVER && onTurn == null

    /**
     * Return the player which is on turn. Return empty if there is no player on
     * turn, which means there is either a dealer action or the hand is
     * complete.
     */
    private fun moveToNextPlayer() {

        if (this.activePlayers.size > 1) {
            if (!this.onTurnIterator.hasNext())
                this.onTurnIterator = this.activePlayers.listIterator()
            val player = this.onTurnIterator.next()
            if (this.minPlayerActions > 0 || paid(player) < toPay())
            // the player still has to pay something to stay in the
            // game or he did not perform an action in this phase
                onTurn = player
            else
                onTurn = null
        } else {
            onTurn = null
        }
    }

    fun phase(): Phase {
        return phase
    }

    fun seat(player: Player): Int {
        assert(players.contains(player)) { "Player not on table" }

        return players.indexOf(player)
    }

    fun paid(player: Player): Int {

        return paid[seat(player)]

        // return actions.stream().filter(a -> a.getParty() == player)
        // .filter(a -> a.getAction() > 0)
        // .collect(Collectors.summingInt(a -> a.getAction()));
    }

    fun pot(): Int {
        return actions.filter { a -> a > 0 }.sum();
    }

    override fun toString(): String {

        // limiter
        val separator = TexasHand.SEPARATOR

        // write blinds
        var s = dealer.smallBlind().toString() + separator + dealer.bigBlind().toString() + separator

        // write table cards
        if (flop != null) {
            val iter = flop!!.iterator()
            while (iter.hasNext())
                s += iter.next()
            if (this.turn != null) {
                s += this.turn!!
                if (this.river != null)
                    s += this.river!!
            }
        }

        // write players
        for (p in this.players) {

            var cards = EMPTY

            val holeCards = p.holdings()

            if (holeCards.isPresent) {
                val iter = holeCards.get().iterator()
                cards = iter.next().toString() + "" + iter.next()
            }

            s += separator + p.name.replace("_".toRegex(), _REPLACEMENT) + separator + p.stack + separator + cards
        }

        // write actions
        for (a in this.actions)
            s += separator + Action.toString(a)

        return s
    }

    abstract fun callAmount(): Int

    abstract fun raiseAmount(): Int

    fun actionIdentifier(): String {

        var s = ""
        for (a in this.actions)
            s += Action.toString(a) + "_"

        return s
    }

    class Player(val name: String, var model: PlayerModel, stack: Int) {

        var stack: Int
        private var cards: Optional<EnumSet<Card>> = Optional.empty()

        init {
            assert(name.length > 0)
            assert(stack > 1) { "Player must have some stack" }
            this.stack = stack
            cards = Optional.empty<EnumSet<Card>>()
        }

        fun holdings(): Optional<EnumSet<Card>> {
            if (this.cards.isPresent)
                return Optional.of(this.cards.get().clone())
            else
                return this.cards
        }

        fun setCards(cards: EnumSet<Card>) {
            assert(cards.size == 2)
            this.cards = Optional.of(cards)
        }
    }

    companion object {

        protected val PHASES = arrayOf(Phase.FLOP, Phase.TURN, Phase.RIVER)
        protected val EMPTY = "-"
        protected val SEPARATOR = "_"
        protected val _REPLACEMENT = "\\.\\*\\."
    }
}

