package com.example.justjoinparser;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public final class Sleeper {

    private static final Random random = new Random();

    private Sleeper() {

    }

    private static void sleepMs(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(final int ms) {
        sleepMs(ms);
    }

    /**
     * Pauses thread for a specified amount of seconds.
     *
     * @param seconds amount of seconds to pause a thread execution
     */
    public static void sleepExactly(final int seconds) {
        sleepMs(toMilliSeconds(seconds));
    }

    /**
     * Pauses thread for a random amount of seconds in range from 0 to maximum provided.
     *
     * @param maxSeconds maximum amount of seconds to pause a thread execution
     */
    public static void sleepRandom(final int maxSeconds) {
        sleepRandom(0, maxSeconds);
    }

    /**
     * Pauses thread for a random amount of seconds in range from minimum to maximum provided.
     *
     * @param minSeconds minimum amount of seconds to pause a thread execution
     * @param maxSeconds maximum amount of seconds to pause a thread execution
     */
    public static void sleepRandom(final int minSeconds, final int maxSeconds) {
        if (minSeconds < 0) {
            throw new IllegalArgumentException("min needs to be greater or equal to 0");
        }
        if (minSeconds >= maxSeconds) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        int sleepTime = random.nextInt((maxSeconds - minSeconds) + 1) + minSeconds;
        sleepMs(toMilliSeconds(sleepTime) + random.nextInt(1000));
    }

    private static long toMilliSeconds(int s) {
        return s * 1000L;
    }
}