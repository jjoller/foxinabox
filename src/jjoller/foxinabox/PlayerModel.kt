package jjoller.foxinabox


/**
 * Provides a players actions
 */
public interface PlayerModel {
    public fun action(hand: TexasHand): Int
}
