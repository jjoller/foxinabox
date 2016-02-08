package jjoller.foxinabox.playermodels.treesearch;

import jjoller.foxinabox.Action;
import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.RandomSeed;
import jjoller.foxinabox.TexasHand;
import jjoller.foxinabox.playermodels.treesearch.trees.MonteCarloTreeNode;

import java.util.*;

/**
 * Player who acts as a node in an exploration tree.
 */
public class PlanningPlayer implements PlayerModel {

    private static final double explorationRatio = Math.sqrt(2);

    public PlanningPlayer(String hero, Map<String, PlayerModel> opponentModels) {
        this.hero = hero;
        this.opponentModels = opponentModels;
    }

    private final String hero;
    private final Map<String, PlayerModel> opponentModels;
    private final Map<String, PlannerStatistics> stats = new HashMap<>();
    private final List<String> toUpdateHistories = new ArrayList<>();

    @Override
    public int action(TexasHand hand) {

        String history = hand.actionIdentifier();
        TexasHand.Player onTurn = hand.playerOnTurn().get();
        if (onTurn.getName().equals(hero)) {
            PlayerModel model = opponentModels.get(onTurn.getName());

        }

        PlannerStatistics currentNode = this.stats.get(history);

        int selectedAction = Integer.MAX_VALUE;

        // If we haven't explored all possible actions, choose one uniformly at
        // random from the not yet explored actions.
        Set<Integer> actions = new HashSet<>();

        actions.add(hand.callAmount());
        if (hand.callAmount() > 0)
            actions.add(Action.FOLD);

        actions.add(hand.raiseAmount());

        double maxValue = Double.NEGATIVE_INFINITY;

        String selectedHistory = null;
        for (int action : actions) {


            String childHistory = history + "_" + action;

            PlannerStatistics childNode = stats.get(childHistory);

            if (childNode == null) {
                // unexplored node
                toUpdateHistories.add(childHistory);
                return action;
            } else {
                // the node has been explored before
                double value = childNode.expectation()
                        + explorationRatio
                        * Math.sqrt(Math.log((double) currentNode.visits())
                        / (double) childNode.visits());

                if (value > maxValue) {
                    maxValue = value;
                    selectedAction = action;
                    selectedHistory = history + "_" + action;
                }
            }
        }
        toUpdateHistories.add(selectedHistory);
        return selectedAction;


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

            Iterator<Map.Entry<Integer, MonteCarloTreeNode>> it = children()
                    .entrySet().iterator();

            double maxValue = Double.NEGATIVE_INFINITY;

            while (it.hasNext()) {

                Map.Entry<Integer, MonteCarloTreeNode> c = it.next();
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


        return 0;
    }


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
        private int visits = 1;

        public double expectation() {
            return expectation;
        }

        public int visits() {
            return visits;
        }

        public void update(double outcome) {
            expectation += outcome;
            visits++;
        }

    }

}
