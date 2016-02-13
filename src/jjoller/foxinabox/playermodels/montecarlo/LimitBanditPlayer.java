package jjoller.foxinabox.playermodels.montecarlo;

import jjoller.foxinabox.Action;
import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

import java.util.*;

/**
 * Player who builds a monte carlo exploration tree
 */
public class LimitBanditPlayer implements PlayerModel {

    private static final double explorationRatio = Math.sqrt(2);

    public LimitBanditPlayer() {

    }

    private final Map<String, PlannerStatistics> stats = new HashMap<>();
    private final List<String> toUpdateHistories = new ArrayList<>();
    private int bestAction;

    @Override
    public int action(TexasHand hand) {

        String history = hand.actionIdentifier();

        // this should be the hero player
        TexasHand.Player onTurn = hand.playerOnTurn().get();

        PlannerStatistics currentNode = this.stats.get(history);

        int selectedAction = Integer.MAX_VALUE;

        // figure out all the possible actions
        Set<Integer> actions = new HashSet<>();
        actions.add(hand.callAmount());
        if (hand.callAmount() > 0)
            actions.add(Action.FOLD);
        actions.add(hand.raiseAmount());


        // If we haven't explored all possible actions, choose one uniformly at
        // random from the not yet explored actions.

        double maxValue = Double.NEGATIVE_INFINITY;

        String selectedHistory = null;

        // TODO make sure the iteration is performed in random order
        double bestExpectation = Double.NEGATIVE_INFINITY;
        for (int action : actions) {

            String childHistory = history + "_" + action;

            PlannerStatistics childNode = stats.get(childHistory);

            if (childNode == null) {
                // unexplored node
                toUpdateHistories.add(childHistory);
                return action;
            } else {
                // the node has been explored before

                // Bandit formula
                double value = childNode.expectation()
                        + explorationRatio
                        * Math.sqrt(Math.log(currentNode.visits())
                        / childNode.visits());

                if (value > maxValue) {
                    maxValue = value;
                    selectedAction = action;
                    selectedHistory = history + "_" + action;
                }

                // update the best action
                if(childNode.expectation() > bestExpectation){
                    bestExpectation = childNode.expectation();
                    this.bestAction = action;
                }
            }
        }
        toUpdateHistories.add(selectedHistory);
        return selectedAction;
    }

    public int bestAction() {
        return bestAction;
    }

    /**
     * Usually this is called after the hand is completed.
     *
     * @param outcome
     */
    public void update(double outcome) {

        for (int i = 0; i < toUpdateHistories.size(); i++) {
            String history = toUpdateHistories.get(i);
            if (!stats.containsKey(history))
                stats.put(history, new PlannerStatistics());
            stats.get(history).update(outcome);
        }
        toUpdateHistories.clear();
    }

    class PlannerStatistics {

        private double expectation = 0;
        private double visits = 0;

        public double expectation() {
            return expectation / visits;
        }

        public double visits() {
            return visits;
        }

        public void update(double outcome) {
            expectation += outcome;
            visits++;
        }

    }

}
