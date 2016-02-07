package jjoller.foxinabox.playermodels.treesearch.trees;

import jjoller.foxinabox.Dealer;
import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public abstract class MonteCarloTreeNode {

	public MonteCarloTreeNode(boolean isHeroNode, double maxReward, double minReward) {
		this.maxReward = maxReward;
		this.minReward = minReward;
		this.isHeroNode = isHeroNode;
	}

	protected int visits = 0;
	protected double maxReward, minReward;
	protected boolean isHeroNode;
	private Map<Integer, MonteCarloTreeNode> children = new HashMap<Integer, MonteCarloTreeNode>();
	protected double explorationRatio = 1;

	public abstract int getBestAction();

	public abstract int sample(Dealer dealer, TexasHand.Player hero, PlayerModel modelProvider, int depth);

	protected abstract int actionSelect(TexasHand hand);

	public abstract double expectation();

	public int visits() {
		return visits;
	}

	public Map<Integer, MonteCarloTreeNode> children() {
		return children;
	}

	public boolean isHeroNode() {
		return isHeroNode;
	}

	/**
	 * @return Number of child nodes in total.
	 */
	public int numChildNodes() {
		int result = 0;
		if (children() != null) {
			result = children().size();
			for (MonteCarloTreeNode node : children().values()) {
				result += node.children().size();
			}
		}
		return result;
	}

	/**
	 * @return The number of leaves of the subtree.
	 */
	public int numLeaves() {
		int result;
		if (children() == null || children().size() == 0) {
			result = 1;
		} else {
			result = 0;
			for (MonteCarloTreeNode node : children().values()) {
				result += node.numLeaves();
			}
		}
		return result;
	}

	@Override
	public String toString() {

		return "\n" + toString("");

	}

	private String toString(String s) {

		String input = s + "";

		String result = "";

		if (children() == null || children().size() == 0) {
			return s + this.expectation() + ", visits: " + visits();
		} else {

			Iterator<Entry<Integer, MonteCarloTreeNode>> iter = children()
					.entrySet().iterator();

			// String childrenActionString = "";

			while (iter.hasNext()) {
				Entry<Integer, MonteCarloTreeNode> e = iter.next();
				if (isHeroNode()) {
					result = result
							+ e.getValue().toString(input + e.getKey() + "!,");
				} else {
					result = result
							+ e.getValue().toString(input + e.getKey() + ",");
				}
				if (iter.hasNext()) {
					result += "\n";
				}
				// childrenActionString += e.getKey() + ",";
			}
			// log.info("children of node: " + childrenActionString);

			// log.info("\n=======\ninput:\n"+input+"\nreturn:\n"+s+"\n=====");

			return result;

		}
	}
}
