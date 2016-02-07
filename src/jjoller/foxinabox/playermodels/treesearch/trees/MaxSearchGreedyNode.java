package jjoller.foxinabox.playermodels.treesearch.trees;

import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

import java.util.Set;

/**
 * Instead of choosing hero actions using a bandit strategy perform a depth
 * first search on the tree.
 * 
 */
@Deprecated
public class MaxSearchGreedyNode extends MaxSearchUCTNode {

	private MaxSearchGreedyNode(boolean isHeroNode, double maxReward, double minReward) {
		super(isHeroNode, maxReward, minReward);
	}

	@Override
	public int sample(Dealer dealer, TexasHand.Player hero, PlayerModel modelProvider, int depth) {

		visits++;

		Hand hand = hero.getHand();

		if (hand.isComplete()) {
			hand.payOutPotAndHideHoleCards(modelProvider);
			updateExpectation(hero.getWin() - hero.getAmountPaid());
		} else if (hand.getPhase() == Phase.Preflop && hand.getPotsize() == 0
				|| hand.phaseComplete()) {
			// dealer action

			hand.playAction(dealer, modelProvider);

			// compensate for another sample call on the same node
			visits--;
			sample(dealer, hero, modelProvider, depth);
		} else {
			// player action

			Player onTurn = hand.playerOnTurn();
			isHeroNode = onTurn.equals(hero);

			if (hero.hasFolded()) {

				// don't have to complete the hand if the hero folded!
				updateExpectation(-hero.getAmountPaid());

			} else {
				if (isHeroNode) {

					Set<Integer> actions = hand.getValidActions();
					for (int a : actions) {
						if (!children().containsKey(a)) {
							children().put(
									a,
									new MaxSearchGreedyNode(false, maxReward,
											minReward));
						}
						Hand copy = hand.clone();
						copy.playAction(dealer, new MetaModel(
								new SingleActionModel(a)));
						children().get(a).sample(dealer, 
								copy.getPlayerByName(hero.getName()),
								modelProvider,++depth);

					}

					// int action = actionSelect(hand);
					// if (!children().containsKey(action)) {
					// children().put(
					// action,
					// new MaxExpectationSearchNode(maxReward,
					// minReward));
					// }
					// hand.playAction(new SimpleModelProvider(
					// new SingleActionModel(action)));
					// children().get(action).sample(hero, modelProvider);

					// update reward
					double e = Double.NEGATIVE_INFINITY;
					for (MonteCarloTreeNode child : children().values()) {
						if (child.expectation() > e) {
							e = child.expectation();
						}
					}
					expectation = e;

				} else {

//					PlayerModel model = modelProvider.getModel(onTurn);
					int action = 0;
					action = RandomUtils.weightedRandomNumber(modelProvider.actionProbabilities(onTurn));

					
					
					if (!children().containsKey(action)) {
						
						boolean childIsHeroNode = onTurn.successor().equals(hero);
						
						children().put(action,
								new MaxSearchGreedyNode(childIsHeroNode, maxReward, minReward));
					}

					hand.playAction(dealer, new MetaModel(
							new SingleActionModel(action)));

					children().get(action).sample(dealer, hero, modelProvider,++depth);

					// update reward
					double mean = 0;
					int totalChildVisits = 0;
					for (MonteCarloTreeNode child : children().values()) {
						int childVisits = child.visits();
						mean += child.expectation() * (double) childVisits;
						totalChildVisits += childVisits;
					}
					expectation = mean / (double) totalChildVisits;
				}
			}
		}
		return 0;
	}

}
