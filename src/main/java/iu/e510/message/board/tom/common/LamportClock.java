package iu.e510.message.board.tom.common;

import java.util.concurrent.atomic.AtomicInteger;

public class LamportClock {

    private static volatile AtomicInteger clock;
    private static LamportClock lamportClock = null;

    private LamportClock() {
        clock = new AtomicInteger();
    }

    public static synchronized LamportClock getClock() {
        if (lamportClock == null) {
            lamportClock = new LamportClock();
        }
        return lamportClock;
    }

    public int incrementAndGet() {
        return clock.incrementAndGet();
    }

    public void set(int newValue) {
        clock.set(newValue);
    }

    public int get() {
        return clock.get();
    }
}
