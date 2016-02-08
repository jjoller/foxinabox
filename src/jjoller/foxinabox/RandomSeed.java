package jjoller.foxinabox;

import java.util.Random;

/**
 * All the instances of Random use this interface to access seed. Allows to make
 * random algorithms deterministic.
 */
public class RandomSeed {


    private static Random seedGenerator = new Random();

    public static long getSeed() {
        return seedGenerator.nextLong();
    }

    public static void setSeed(long seed) {
        seedGenerator.setSeed(seed);
        random.setSeed(getSeed());
    }

    /**
     * Shared instance of random (allows static methods to use random without
     * breaking determinism).
     */
    public static final Random random = new Random(getSeed());

}

