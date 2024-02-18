package com.course.allocator.api;

import java.util.Random;
import java.util.concurrent.atomic.LongAdder;

public class CalculationThread implements Runnable {
    private final Random rng;
    private final LongAdder calculationsPerformed;
    private boolean stopped;
    private double store;

    public CalculationThread(LongAdder calculationsPerformed) {
        this.calculationsPerformed = calculationsPerformed;
        this.stopped = false;
        this.rng = new Random();
        this.store = 1;
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        while (!this.stopped) {
            this.store = Math.pow(this.store, 1.0000001);
            this.calculationsPerformed.add(1);
        }
    }

}
