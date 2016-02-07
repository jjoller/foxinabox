package jjoller.foxinabox;

import jjoller.foxinabox.playermodels.SimplePlayerModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	public static void main(String[] args) {

		SimplePlayerModels models = new SimplePlayerModels();

		PlayerModel model = models::random;

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

}
