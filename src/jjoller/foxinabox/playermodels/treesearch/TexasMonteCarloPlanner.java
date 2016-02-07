package jjoller.foxinabox.playermodels.treesearch;

        import java.util.logging.Logger;

        import jjoller.foxinabox.Dealer;
        import jjoller.foxinabox.PlayerModel;
        import jjoller.foxinabox.TexasHand;
        import jjoller.foxinabox.playermodels.treesearch.trees.MonteCarloTreeNode;


/**
 * Perform random samplings on a hand to approximate the optimal action.
 *
 */
public class TexasMonteCarloPlanner implements PlayerModel {

    private static final Logger log = Logger
            .getLogger(TexasMonteCarloPlanner.class.getName());

    /**
     *
     * @param playerModelStatistics
     *            Provides player models to model opponents.
     *
     * @param planningTime
     *            The time in milliseconds the computer gets time to think about
     *            the right move.
     *
     * @param minSampleRuns
     *            The minimum number of sample runs that have to be made
     *            regardless of the time limit.
     *
     * @param rootNodeFactory
     *            Provider for monte carlo tree nodes.
     *
     */
    public TexasMonteCarloPlanner(PlayerModel opponentModel, int time,
                                  int minSampleRuns, foxinabox.server.ai.treeSearch.trees.MCRootNodeFactory rootNodeFactory) {

        this.opponentModel = opponentModel;
        this.minSampleRuns = minSampleRuns;
        this.planningTime = time;
        this.rootNodeFactory = rootNodeFactory;
        // this.lastUpdated = 0;
    }

    private MCRootNodeFactory rootNodeFactory = new MCRootNodeFactory() {
        @Override
        public MonteCarloTreeNode rootNode(double maxReward, double minReward) {
            return new UCTTexasNode(true, maxReward, minReward);
        }
    };

    private MonteCarloTreeNode rootNode;
    private PlayerModel opponentModel;
    private int planningTime = 1000;
    private int sampleCount;
    private int minSampleRuns = -1;

    // private long lastUpdated;

    @Override
    public PlayerModel clone() {
        TexasMonteCarloPlanner copy = new TexasMonteCarloPlanner(
                opponentModel.clone(), planningTime, minSampleRuns,
                rootNodeFactory);
        return copy;
    }

    @Override
    public void update(TexasHand.Player hero) {

        // update the opponent model
        for (TexasHand.Player opponent : hero.getHand().getPlayers()) {
            if (!hero.getName().equals(opponent.getName())) {
                opponentModel.update(opponent);
            }
        }
    }

    @Override
    public int action(TexasHand hand) {

        TexasHand.Player player = hand.playerOnTurn().get();

        log.info("GET ACTION for player " + player.getName());


        assert (player.getHoldings().size() == 2);

        // modelStatistics.setHero(player);

        // for (Hand previous : player.getHand().getHistory()) {
        // if (previous.getTimestamp() > lastUpdated) {
        // modelStatistics.update(previous);
        // lastUpdated = previous.getTimestamp();
        // }
        // }

        Hand hand = player.getHand();

        double maxWin = hand.maxPotsize();
        double numPlayers = hand.numPlayers();
        double maxLoose = -maxWin / numPlayers;
        maxWin = (numPlayers - 1) * maxWin / numPlayers;

        // Cut reference to original hand, only modify copies.
        hand = hand.clone();
        player = hand.getPlayerByName(player.getName());

        Dealer dealer = new PlayerModelDealer(opponentModel, new RandomDealer());

        EnumSet<Card> holeCards = player.getHoldings();

        // don't look into the cards of the opponents.
        hand.collectHoleCards();

        // set own cards just for pretty printing
        player.setHoldings(holeCards);

        // rootNode = MonteCarloTreeNode.reuseTree(rootNode, currentHand, hand);
        // if (rootNode == null) {
        // log.info("do not reuse tree");
        rootNode = rootNodeFactory.rootNode(maxWin, maxLoose);

        MonteCarloJob job = new MonteCarloJob(rootNode, dealer, opponentModel, hand,
                planningTime, minSampleRuns, player.getName());
        job.run();

        // get sample count
        sampleCount = job.getSampleCount();

        log.info(hand + "");

        // print the whole search tree:
        // log.info(rootNode.toString());

        log.info(opponentModel + "");

        int bestAction = rootNode.getBestAction();

        log.info("====== best action: " + bestAction + ", samples: "
                + sampleCount + " ======");

        return ImmutableMap.of(bestAction,1.0);
    }

    public int getSampleCount() {
        return sampleCount;
    }

    @Override
    public String toString() {
        return "TexasMonteCarloPlanner";
    }
}

class MonteCarloJob implements Runnable {

    public MonteCarloJob(MonteCarloTreeNode rootNode, Dealer dealer,
                         PlayerModel modelStatistics, TexasHand hand, int planningTime,
                         int minSampleRuns, String heroName) {
        this.rootNode = rootNode;
        this.dealer = dealer;
        this.hand = hand;
        this.planningTime = planningTime;
        this.modelStatistics = modelStatistics;
        this.heroName = heroName;

        // have to perform at least 2 sample runs because on the first sample
        // run no nodes are created.
        this.minSampleRuns = Math.max(minSampleRuns, 2);
    }

    private Hand hand;
    private MonteCarloTreeNode rootNode;
    private int planningTime;
    private int minSampleRuns;
    private PlayerModel modelStatistics;
    private String heroName;
    private int sampleCount = 0;
    private Dealer dealer;

    @Override
    public void run() {

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < planningTime
                || sampleCount < minSampleRuns) {

            Hand copy = hand.clone();

            Player playerCopy = copy.getPlayerByName(heroName);
            copy.dealHoleCards(dealer);

            rootNode.sample(dealer,playerCopy, modelStatistics, 0);
            sampleCount++;

        }
    }

    public int getSampleCount() {
        return sampleCount;
    }

}
