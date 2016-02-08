package jjoller.foxinabox.playermodels.treesearch.trees;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jjoller.foxinabox.Action;
import jjoller.foxinabox.Dealer;
import jjoller.foxinabox.RandomSeed;
import jjoller.foxinabox.TexasHand;

/**
 * Classical UCT implementation for Poker. Average reward is not updated in a
 * max search manner.
 */
public class UCTTexasNode extends MonteCarloTreeNode {

    // public static MonteCarloProvider getBetterProvider() {
    // return new MonteCarloProvider() {
    // @Override
    // public MonteCarloTreeNode getRootNode(Hand hand, double maxReward,
    // double minReward) {
    // return new UCTTexasNode(true, maxReward, minReward, true);
    // }
    // };
    // }

    private static final Logger log = Logger.getLogger(UCTTexasNode.class.getName());

    // private Random random = new Random(RandomSeed.getSeed());

    public UCTTexasNode(boolean isHeroNode, double maxReward, double minReward) {
        super(isHeroNode, maxReward, minReward);
    }

    protected int totalReward = 0;

    // explore fold actions
    protected boolean selectFoldAction = true;

    @Override
    public double expectation() {

        if (visits == 0) {
            // every node which gets created gets visited.
            throw new IllegalStateException();
        }

        double mean = (double) totalReward / (double) visits;
        mean = (mean - minReward) / (maxReward - minReward);
        return mean;
    }

    public int visits() {
        return visits;
    }

    /**
     * Choose an action and apply some exploration exploitation trade-off-rule.
     *
     * @param hand
     * @return
     */
    @Override
    protected int actionSelect(TexasHand hand) {

        int selectedAction = Integer.MAX_VALUE;

        // If we haven't explored all possible actions, choose one uniformly at
        // random from the not yet explored actions.
        Set<Integer> actions = new HashSet<>();

        actions.add(hand.callAmount());
        if (hand.callAmount() > 0)
            actions.add(Action.FOLD);

        actions.add(hand.raiseAmount());

        if (children().size() < actions.size()) {
            actions.removeAll(children().keySet());
            // selectedAction = actions.iterator().next();
            selectedAction = actions.toArray(new Integer[actions.size()])[RandomSeed.random
                    .nextInt(actions.size())];
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
                int a = c.getKey();

                MonteCarloTreeNode childNode = c.getValue();

                double value = childNode.expectation()
                        + explorationRatio
                        * Math.sqrt(Math.log((double) visits())
                        / (double) childNode.visits());

                if (value > maxValue) {
                    maxValue = value;
                    selectedAction = a;
                }
            }
        }
        return selectedAction;
    }

    /**
     * Just select the action with the best expectation.
     *
     * @return
     */
    @Override
    public int getBestAction() {

        int best = 0;
        double bestReward = Double.NEGATIVE_INFINITY;
        boolean found = false;

        for (Entry<Integer, MonteCarloTreeNode> e : children().entrySet()) {

            double expect = e.getValue().expectation();

            assert (!Double.isNaN(expect));

            if (expect > bestReward) {
                bestReward = expect;
                best = e.getKey();
                found = true;
            }
        }

        if (!found)
            throw new IllegalStateException();

        log.fine("best action " + best);

        return best;
    }

    @Override
    public int sample(Dealer dealer, TexasHand hand, int depth) {

        int reward;

        if (hand.isComplete()) {
            hand.payOutPotAndHideHoleCards(modelProvider);
            reward = hero.getWin() - hero.getAmountPaid();
            log.fine("reward " + reward);
        } else if (hand.getPhase() == Phase.Preflop && hand.getPotsize() == 0
                || hand.phaseComplete()) {

            log.fine("dealer action");

            // dealer action
            hand.playAction(dealer, modelProvider);
            reward = this.sample(dealer, hero, modelProvider, depth);

            // compensate for another sample call on the same node
            totalReward -= reward;
            visits--;
        } else {

            log.fine("player action");

            Player onTurn = hand.playerOnTurn();
            isHeroNode = onTurn.equals(hero);

            // player action
            if (!hero.hasFolded()) {
                log.fine("player on turn: " + onTurn);
                if (isHeroNode) {
                    if (visits <= 0) {

                        log.fine("rollout");

                        reward = rollout(dealer, hero, modelProvider);
                    } else {

                        int action = actionSelect(hand);

                        if (!children().containsKey(action)) {
                            children().put(
                                    action,
                                    new UCTTexasNode(false, maxReward,
                                            minReward));
                        }
                        hand.playAction(dealer, new SingleActionModel(
                                action));
                        reward = children().get(action).sample(dealer, hero,
                                modelProvider, ++depth);
                    }

                } else {

                    int action = 0;
                    action = RandomUtils.weightedRandomNumber(modelProvider.actionProbabilities(onTurn));
                    log.fine("action " + action);

                    if (!children().containsKey(action)) {
                        boolean childIsHeroNode = onTurn.successor().equals(
                                hero);
                        children().put(
                                action,
                                new UCTTexasNode(childIsHeroNode, maxReward,
                                        minReward));
                        log.fine("created node, is heroNode: "
                                + childIsHeroNode);
                    }

                    hand.playAction(dealer, new SingleActionModel(action));

                    reward = children().get(action).sample(dealer, hero, modelProvider,
                            ++depth);
                }
            } else {
                // don't have to complete the hand if the hero folded!
                reward = -hero.getAmountPaid();
            }
        }

        visits++;

        totalReward += reward;
        return reward;
    }

    /**
     * Play random actions until the hand is complete without creating new
     * nodes.
     *
     * @param hero
     * @return the amount the player looses or wins in this round.
     */
    private int rollout(Dealer dealer, TexasHand hand, TexasHand.Player hero) {

        while (!hand.isComplete()) {

            log.fine("play action");

            hand.playAction(dealer, modelProvider);

            if (hero.hasFolded()) {
                break;
            }

        }

        if (!hero.hasFolded()) {
            hand.payOutPotAndHideHoleCards(modelProvider);
        }

        int reward = hero.getWin() - hero.getAmountPaid();
        log.fine("reward: " + reward);
        return reward;
    }

}
