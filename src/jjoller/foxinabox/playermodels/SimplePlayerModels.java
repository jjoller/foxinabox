package jjoller.foxinabox.playermodels;

import jjoller.foxinabox.Action;
import jjoller.foxinabox.TexasHand;

import java.util.Random;

/**
 * Created by jost on 2/7/16.
 */
public class SimplePlayerModels {

    private static final Random random = new Random();
    private static final double pFold = 0.1;
    private static final double pCall = 0.6;

    public int random(TexasHand hand){
        double r = random.nextDouble();
        int call = hand.callAmount();
        if (r < pFold && call > 0)
            // it is always possible to fold if you have to pay something
            return Action.FOLD;
        else if (r < pCall + pFold)
            return call;
        else
            return hand.raiseAmount();
    }

    public int alwaysAggressive(TexasHand hand){
        return  hand.raiseAmount();
    }

    public int caller(TexasHand hand){
        return hand.callAmount();
    }

}
