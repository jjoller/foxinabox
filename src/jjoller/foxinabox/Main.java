package jjoller.foxinabox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	public static void main(String[] args) {

		PlayerModel model = Main::playRandomActionWeighted;

		int stack = 1000000;
		List<Player> players = new ArrayList<>();
		players.add(new Player("John", model, stack));
		players.add(new Player("Ringo", model, stack));
		players.add(new Player("George", model, stack));
		players.add(new Player("John", model, stack));
		players.add(new Player("Ringo", model, stack));
		players.add(new Player("George", model, stack));
		players.add(new Player("John", model, stack));
		players.add(new Player("Ringo", model, stack));
		players.add(new Player("George", model, stack));

		TexasHand hand = new LimitTexasHand(new Dealer(), players);

		long t = System.currentTimeMillis();
		int count = 0;
		long duration = 20000;
		while (System.currentTimeMillis() - t < duration) {
			count++;
			TexasHand oldHand = hand;
			hand = hand.playHand();
			// System.out.println(oldHand.toString());
		}

		System.out.println("played " + count + " hands in "
				+ (System.currentTimeMillis() - t) + " ms");

	}

	private static final Random random = new Random();
	private static final double pFold = 0.1;
	private static final double pCall = 0.6;

	public static int playRandomActionWeighted(TexasHand hand) {

		double r = random.nextDouble();
		int call = hand.callAmount();
		if (r < pFold && call > 0)
			// it is always possible to fold if you have to pay something
			return Action.FOLD;
		else if (r < pCall + pFold)
			return call;
		else
			return hand.raiseAmount();

	}
}
