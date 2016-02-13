package jjoller.foxinabox.playermodels.montecarlo;

import jjoller.foxinabox.PlayerModel;
import jjoller.foxinabox.TexasHand;

import java.util.Iterator;
import java.util.List;

/**
 * Plays according to the history if there is a history or according to a model if there is no more history. Keeps track
 * how conform the model is to the history.
 */
public class HistoryPlayer implements PlayerModel {

    public HistoryPlayer(String playerName, PlayerModel model, List<Integer> history) {
        this.playerName = playerName;
        this.model = model;
        this.history = history;
        this.historyIterator = this.history.iterator();
    }

    private String playerName;
    private PlayerModel model;
    private List<Integer> history;
    private Iterator<Integer> historyIterator;
    private double hits = 0;
    private double misses = 0;

    public void reset() {
        this.historyIterator = this.history.iterator();
    }

    @Override
    public int action(TexasHand hand) {
        int modelAction = model.action(hand);
        if (historyIterator.hasNext()) {
            int historyAction = historyIterator.next();
            if (historyAction < -1)
                // is dealer action, proceed
                historyAction = historyIterator.next();
            if (historyAction == modelAction)
                hits++;
            else
                misses++;
            return historyAction;
        } else {
            return modelAction;
        }
    }

    public double conformity() {
        return hits / (hits + misses);
    }

    public String getPlayerName() {
        return playerName;
    }
}
