package jjoller.foxinabox;

import java.util.List;

public class LimitTexasHand extends TexasHand {

	public LimitTexasHand(Dealer dealer, List<Player> players) {
		super(dealer, players);
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

//	/**
//	 * What options does the player on turn have
//	 */
//	public Set<Integer> validActions() {
//
//		Player player = this.playerOnTurn().get();
//		Set<Integer> actions = new HashSet<Integer>();
//
//		int playerPaid = paid(player);
//		int moneyLeft = player.getStack() - this.paid(player);
//		int diff = toPay() - playerPaid;
//
//		// folding is only possible if you have to pay something
//		if (diff > 0)
//			actions.add(Action.FOLD);
//
//		// calling or checking should always be possible
//		actions.add(Math.min(moneyLeft, diff));
//
//		// raising
//		Phase phase = this.phase();
//		int raiseStep = dealer.bigBlind();
//		if (phase == Phase.TURN || phase == Phase.RIVER)
//			raiseStep *= 2;
//
//		// raising is possible if number of raises in phase < 4
//		if ((this.toPay() - this.toPayPreviousPhase()) / raiseStep < 4)
//			actions.add(Math.min(moneyLeft, toPay() - playerPaid + raiseStep));
//
//		return actions;
//	}

	@Override
	protected TexasHand follower(Dealer dealer, List<Player> players) {
		return new LimitTexasHand(dealer, players);
	}
}
