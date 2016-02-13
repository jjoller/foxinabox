package jjoller.foxinabox.playermodels.montecarlo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jjoller.foxinabox.LimitTexasHand;
import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

/**
 * Perform random samplings on a hand to approximate the optimal action.
 */
public class TexasMonteCarloPlanner implements PlayerModel {

    private static final Logger log = Logger
            .getLogger(TexasMonteCarloPlanner.class.getName());


    public TexasMonteCarloPlanner(PlayerModel opponentModel, int time, int minSampleRuns) {

        this.opponentModel = opponentModel;
        this.minSampleRuns = minSampleRuns;
        this.planningTime = time;
    }

    private PlayerModel opponentModel;
    private int planningTime;
    private int minSampleRuns;

    @Override
    public int action(TexasHand hand) {

        TexasHand.Player player = hand.playerOnTurn().get();

        String heroName = player.getName();
        System.out.println("hero name: "+heroName);

        log.info("GET ACTION for player " + player.getName());

        LimitBanditPlayer explorer = new LimitBanditPlayer();

        MonteCarloJob job = new MonteCarloJob(hand, explorer, heroName, opponentModel, planningTime, minSampleRuns);
        job.run();

        // get sample count
        log.info(hand + "");
        log.info(opponentModel + "");

        int bestAction = explorer.bestAction();

        log.info("====== best action: " + bestAction + ", samples: "
                + job.getSampleCount() + " ======");

        return bestAction;
    }

    @Override
    public String toString() {
        return "TexasMonteCarloPlanner";
    }
}

class MonteCarloJob implements Runnable {

    public MonteCarloJob(TexasHand hand, LimitBanditPlayer explorer, String heroName,
                         PlayerModel opponentModel, int planningTime,
                         int minSampleRuns) {

        this.hand = hand;
        this.planningTime = planningTime;
        this.opponentModel = opponentModel;
        this.heroName = heroName;
        this.minSampleRuns = minSampleRuns;
        this.explorer = explorer;
    }

    private TexasHand hand;
    private int planningTime;
    private int minSampleRuns;
    private LimitBanditPlayer explorer;
    private PlayerModel opponentModel;
    private String heroName;
    private int sampleCount = 0;

    @Override
    public void run() {

        // dealer used for experiments
        ConformityDealer dealer = new ConformityDealer(hand);

        // create the players used in the experiment
        List<TexasHand.Player> players = new ArrayList<>();

        // keep a map of player to model
        Map<TexasHand.Player, HistoryPlayer> opponentModels = new HashMap<>();

        for (TexasHand.Player player : hand.getPlayers()) {
            if (player.getName().equals(heroName)) {
                // this is the hero player which tries to outsmart the opponents
                players.add(new TexasHand.Player(heroName, explorer, player.getStack()));
            } else {
                // the opponents are modelled as history players to reenact the past but also approximate the future
                // using the given model
                HistoryPlayer historyPlayer = new HistoryPlayer(player.getName(), opponentModel, hand.getActions());
                TexasHand.Player p = new TexasHand.Player(player.getName(), historyPlayer, player.getStack());
                opponentModels.put(p, historyPlayer);
                players.add(p);
            }
        }

        // run the experiments
        long startTime = System.currentTimeMillis();
        while (sampleCount < minSampleRuns || System.currentTimeMillis() - startTime < planningTime) {

            // create the hand
            TexasHand hand = new LimitTexasHand(dealer, players);

            // play the hand until no more player can take an action
            hand.playHand();

            // inform the planner about the outcome (we're only interested in how much the hero won or lost)
            explorer.update(hand.outcome(heroName));

            // inform the conformity dealer about the conformity of the cards
            for (Map.Entry<TexasHand.Player, HistoryPlayer> e : opponentModels.entrySet())
                dealer.update(e.getKey(), e.getValue().conformity());

            // prepare for the next sample run
            dealer.reset();
            opponentModels.values().forEach(m -> m.reset());

            // increase the sample count
            sampleCount++;
        }
    }

    public int getSampleCount() {
        return sampleCount;
    }

}
