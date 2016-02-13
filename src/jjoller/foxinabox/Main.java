package jjoller.foxinabox;

import jjoller.foxinabox.playermodels.SimplePlayerModels;
import jjoller.foxinabox.playermodels.montecarlo.TexasMonteCarloPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        SimplePlayerModels models = new SimplePlayerModels();

        PlayerModel model = models::random;
        PlayerModel smart = new TexasMonteCarloPlanner(models::random,100,10);

        int stack = 1000000;
        List<TexasHand.Player> players = new ArrayList<>();
        players.add(new TexasHand.Player("John", model, stack));
        players.add(new TexasHand.Player("Ringo", smart, stack));
//        players.add(new TexasHand.Player("George", model, stack));
//        players.add(new TexasHand.Player("John", model, stack));
//        players.add(new TexasHand.Player("Ringo", model, stack));
//        players.add(new TexasHand.Player("George", model, stack));
//        players.add(new TexasHand.Player("John", model, stack));
//        players.add(new TexasHand.Player("Ringo", model, stack));
//        players.add(new TexasHand.Player("George", model, stack));

        TexasHand hand = new LimitTexasHand(new RandomDealer(), players);

        long t = System.currentTimeMillis();
        int count = 0;
        long duration = 20000;
        while (System.currentTimeMillis() - t < duration) {
            count++;
            TexasHand oldHand = hand;
            hand.playHand();
            hand = hand.payOutPot();
            System.out.println(oldHand.toString());
        }

        System.out.println("played " + count + " hands in "
                + (System.currentTimeMillis() - t) + " ms");

    }

}
