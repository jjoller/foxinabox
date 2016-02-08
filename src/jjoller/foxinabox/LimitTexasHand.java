package jjoller.foxinabox;

import java.util.List;

public class LimitTexasHand extends TexasHand {

	public LimitTexasHand(Dealer dealer, List<Player> players) {
		super(dealer, players);
	}

	protected LimitTexasHand(TexasHand other){
		super(other);
	}

	@Override
	public LimitTexasHand clone() {
		return new LimitTexasHand(this);
	}

	/**
	 * How much would you have to pay in order to call. Returns the amount of
	 * chips you have if this would mean that you have to go all-in.
	 * 
	 * @return A positive number or 0 if you have the possibility to check.
	 */
	@Override
	public int callAmount() {

		Player player = this.playerOnTurn().get();
		int playerPaid = paid(player);
		int moneyLeft = player.getStack() - playerPaid;
		return Math.min(moneyLeft, toPay() - playerPaid);
	}

	/**
	 * How much would you pay (at least) if you wanted to raise
	 * 
	 * @return return the call amount if raising is not possible
	 */
	@Override
	public int raiseAmount() {

		// raising
		Player player = this.playerOnTurn().get();

		Phase phase = this.phase();
		int raiseStep = dealer.bigBlind();
		if (phase == Phase.TURN || phase == Phase.RIVER)
			raiseStep *= 2;

		int playerPaid = paid(player);
		int moneyLeft = player.getStack() - playerPaid;

		// raising is possible if number of raises in phase < 4
		if ((this.toPay() - this.toPayPreviousPhase()) / raiseStep < 4)
			return Math.min(moneyLeft, toPay() - playerPaid + raiseStep);
		else
			return Math.min(moneyLeft, toPay() - playerPaid);
	}

	@Override
	protected TexasHand follower(Dealer dealer, List<Player> players) {
		return new LimitTexasHand(dealer, players);
	}
}
