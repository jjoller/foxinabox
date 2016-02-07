package jjoller.foxinabox;

/**
 * Helper class for Action encoding
 */
public class Action {

    public static final int FOLD = -1;
    public static final int CHECK = 0;
    public static final int DEAL_PREFLOP = -2;
    public static final int DEAL_FLOP = -3;
    public static final int DEAL_TURN = -4;
    public static final int DEAL_RIVER = -5;

    public static String toString(int action) {

        switch (action) {
            case FOLD:
                return "f";
            case DEAL_PREFLOP:
                return "";
            case DEAL_FLOP:
                return "F";
            case DEAL_TURN:
                return "T";
            case DEAL_RIVER:
                return "R";
            default:
                return action+"";
        }
    }
}