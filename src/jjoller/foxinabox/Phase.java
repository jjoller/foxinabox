package jjoller.foxinabox;

/**
 * Phases of a poker hand
 */
public enum Phase {

    PRE_FLOP(-2), FLOP(-3), TURN(-4), RIVER(-5);

    Phase(int action) {
        this.action = action;
    }

    private final int action;

    public int getAction() {
        return action;
    }
}
