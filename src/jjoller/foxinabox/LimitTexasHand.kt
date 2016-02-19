package jjoller.foxinabox

class LimitTexasHand : TexasHand {

    constructor(dealer: Dealer, players: MutableList<TexasHand.Player>) : super(dealer, players) {

    }

    /**
     * How much would you have to pay in order to call. Returns the amount of
     * chips you have if this would mean that you have to go all-in.

     * @return A positive number or 0 if you have the possibility to check.
     */
    override fun callAmount(): Int {

        if (this.onTurn != null) {
            val player = this.onTurn!!
            val playerPaid = paid(player)
            val moneyLeft = player.stack - playerPaid
            return Math.min(moneyLeft, toPay() - playerPaid)
        } else {
            throw IllegalStateException("There is no player on turn")
        }
    }

    /**
     * How much would you pay (at least) if you wanted to raise

     * @return return the call amount if raising is not possible
     */
    override fun raiseAmount(): Int {

        if (this.onTurn != null) {

            val player = this.onTurn!!

            val phase = this.phase()
            var raiseStep = dealer.bigBlind()
            if (phase === Phase.TURN || phase === Phase.RIVER)
                raiseStep *= 2

            val playerPaid = paid(player)
            val moneyLeft = player.stack - playerPaid

            // raising is possible if number of raises in phase < 4
            if ((this.toPay() - this.toPayPreviousPhase()) / raiseStep < 4)
                return Math.min(moneyLeft, toPay() - playerPaid + raiseStep)
            else
                return Math.min(moneyLeft, toPay() - playerPaid)
        } else {
            throw IllegalStateException();
        }
    }

    override fun follower(dealer: Dealer, players: MutableList<TexasHand.Player>): TexasHand {
        return LimitTexasHand(dealer, players)
    }
}
