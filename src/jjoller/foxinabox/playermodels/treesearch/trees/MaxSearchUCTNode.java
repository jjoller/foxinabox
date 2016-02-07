package jjoller.foxinabox.playermodels.treesearch.trees;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

/**
 * Perform a tree search. Use a bandit approach to choose hero actions.
 * 
 */
@Deprecated
public class MaxSearchUCTNode extends MonteCarloTreeNode {

	private static final Logger log = Logger.getLogger(MaxSearchUCTNode.class
			.getName());

	protected MaxSearchUCTNode(boolean isHeroNode, double maxReward,
			double minReward) {
		super(isHeroNode, maxReward, minReward);
	}

	@Override
	public int getBestAction() {

		int best = 0;
		double bestReward = Double.NEGATIVE_INFINITY;

		String s = "\n";
		for (Entry<Integer, MonteCarloTreeNode> e : children().entrySet()) {
			s += e.getKey() + " " + e.getValue().expectation() + "\n";
			double expect = e.getValue().expectation();
			if (expect > bestReward) {
				bestReward = expect;
				best = e.getKey();
			}

		}
		log.info(s);
		return best;
	}

	protected int totalReward = 0;
	protected double expectation = 0;

	public double expectation() {
		return expectation;
	}

	protected void incrementVisits(int amount) {
		visits += amount;
	}

	protected void setExpectation(double e) {
		this.expectation = e;
	}

	protected void updateExpectation(int reward) {
		totalReward += reward;
		expectation = ((double) totalReward / (double) visits())
				/ (maxReward - minReward);
	}

	@Override
	public int sample(Dealer dealer, TexasHand.Player hero, PlayerModel modelProvider,
					  int depth) {

		incrementVisits(1);

		log.fine("sample, " + hero.getName());

		Hand hand = hero.getHand();

		if (hand.isComplete()) {
			hand.payOutPotAndHideHoleCards(modelProvider);
			updateExpectation(hero.getWin() - hero.getAmountPaid());
		} else if (hand.getPhase() == Phase.Preflop && hand.getPotsize() == 0
				|| hand.phaseComplete()) {
			// dealer action

			hand.playAction(dealer, modelProvider);

			// compensate for another sample call on the same node
			incrementVisits(-1);
			this.sample(dealer, hero, modelProvider, depth);

		} else {
			// player action

			Player onTurn = hand.playerOnTurn();

			if (hero.hasFolded()) {

				// don't have to complete the hand if the hero folded!
				updateExpectation(-hero.getAmountPaid());

			} else {
				log.fine("player on turn: " + onTurn);
				if (isHeroNode) {

					int action = actionSelect(hand);
					synchronized (children()) {
						if (!children().containsKey(action)) {
							children().put(
									action,
									new MaxSearchUCTNode(false, maxReward,
											minReward));
						}
					}
					hand.playAction(dealer, new SingleActionModel(action));
					children().get(action).sample(dealer, hero, modelProvider,
							++depth);

					// update reward
					double e = Double.NEGATIVE_INFINITY;
					for (MonteCarloTreeNode child : children().values()) {
						if (child.expectation() > e) {
							e = child.expectation();
						}
					}
					setExpectation(e);

				} else {

					// PlayerModel model = modelProvider.getModel(onTurn);
					int action = 0;
					action = RandomUtils.weightedRandomNumber(modelProvider
							.actionProbabilities(onTurn));
					log.fine("action " + action);

					synchronized (children()) {
						if (!children().containsKey(action)) {

							boolean childIsHeroNode = onTurn.successor()
									.equals(hero);

							children().put(
									action,
									new MaxSearchUCTNode(childIsHeroNode,
											maxReward, minReward));
						}
					}

					hand.playAction(dealer, new MetaModel(
							new SingleActionModel(action)));

					children().get(action).sample(dealer, hero, modelProvider,
							++depth);

					// update reward
					double mean = 0;
					int totalChildVisits = 0;
					for (MonteCarloTreeNode child : children().values()) {
						int childVisits = child.visits();
						mean += child.expectation() * (double) childVisits;
						totalChildVisits += childVisits;
					}
					setExpectation(mean / (double) totalChildVisits);
				}
			}
		}
		return 0;
	}

	/**
	 * Choose an action and apply some exploration exploitation trade-off-rule.
	 * 
	 * @param hand
	 * @return
	 */
	@Override
	protected int actionSelect(Hand hand) {

		int selectedAction = Integer.MAX_VALUE;

		// If we haven't explored all possible actions, choose one uniformly at
		// random from the not yet explored actions.
		Set<Integer> actions = hand.getValidActions();

		if (children().size() < actions.size()) {
			actions.removeAll(children().keySet());
			selectedAction = actions.iterator().next();
		} else {

			assert (actions.size() == children().size());

			// The general idea is to explore the most promising (with the
			// highest expected reward) actions. But also explore other actions
			// to not get stuck with wrong decisions.

			Iterator<Entry<Integer, MonteCarloTreeNode>> it = children()
					.entrySet().iterator();

			double maxValue = Double.NEGATIVE_INFINITY;

			while (it.hasNext()) {
				Entry<Integer, MonteCarloTreeNode> c = it.next();
				if (c.getKey() != Action.FOLD) {
					MonteCarloTreeNode childNode = c.getValue();
					double value = childNode.expectation()
							+ explorationRatio
							* Math.sqrt(Math.log((double) visits())
									/ (double) childNode.visits());
					if (value > maxValue) {
						maxValue = value;
						selectedAction = c.getKey();
					}
				}
			}
		}
		return selectedAction;
	}

}
